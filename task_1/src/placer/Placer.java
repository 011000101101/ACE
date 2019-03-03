package placer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import designAnalyzer.ParameterManager;
import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.inputParser.ArchitectureParser;
import designAnalyzer.inputParser.NetlistParser;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.SimplePath;
import designAnalyzer.structures.StructureManager;
import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.LogicBlock;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import designAnalyzer.timingAnalyzer.TimingAnalyzer;
import placer.outputWriter.PlacementWriter;
import org.jfree.data.xy.*;
import org.jfree.ui.ApplicationFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;

/**
 * 
 * @author Dennis Grotz, Rumei Ma, Vincenz Mechler
 *
 */
public class Placer {
	
	/**
	 * reference to instance of input parser
	 */
	private static NetlistParser netlistParser;
	
	/**
	 * reference to instance of input parser
	 */
	private static ArchitectureParser architectureParser;
	
	/**
	 * reference to instance of placement writer
	 */
	private static PlacementWriter placementWriter;
	
	
	/**
	 * reference to instance of timing analyzer
	 */
	private static TimingAnalyzer timingAnalyzer;
	//public static int[] parameterInitialized;
	
	/**
	 * object managing all datastructure instances <br>
	 * -handling insertion, retrieval and others
	 */
	private static StructureManager structureManager;
	
	/**
	 * object managing all structural parameters 
	 */
	private static ParameterManager parameterManager;
	
	/**
	 * seeded random number generator
	 */
	private static Random rand;

	/**
	 * length and height of the quadratic placement area size in the middle of the chip
	 */
	private static int placingAreaSize;
	
	/**
	 * array holding all io blocks to be placed
	 */
	private static IOBlock[] iOBlocks;
	
	/**
	 * array holding all logic blocks to be placed
	 */
	private static LogicBlock[] logicBlocks;

	/**
	 * number of io blocks to be placed
	 */
	private static int iOBlockCount;

	/**
	 * number of all blocks to be placed
	 */
	private static int blockCount;

	/**
	 * used when FPGA size is bigger than required number of blocks
	 */
	private static int biasX;

	/**
	 * used when FPGA size is bigger than required number of blocks
	 */
	private static int biasY;
	
	/**
	 * List of total costs after each step
	 */
	private static List<Double> outputTotalCost= new ArrayList<Double>();
	
	/**
	 * List of wiring costs after each step
	 */
	private static List<Double> outputTotalWiringCost= new ArrayList<Double>();
	
	/**
	 * List of timing costs after each step
	 */
	private static List<Double> outputTotalTimingCost= new ArrayList<Double>();

	/**
	 * List of acceptance rate after each step
	 */
	private static List<Double> outputAcceptanceRate= new ArrayList<Double>();

	/**
	 * List of r Limit after each step
	 */
	private static List<Double> outputRLimit= new ArrayList<Double>();

	/**
	 * List of r Limit Logic Blocks after each step
	 */
	private static List<Double> outputRLimitLogicBlock= new ArrayList<Double>();
	
	/**
	 * for wiring cost
	 */
	public final static double GAMMA = 1.59;
	
	/**
	 * for wiring cost
	 */
	public final static int PHI = 1;
	
	private static List<SimplePath> paths= new LinkedList<SimplePath>();

	/**
	 * cache for wiring cost
	 */
	private static double oldWiringCost;

	/**
	 * cache for timing cost
	 */
	private static double oldTimingCost;

	/**
	 * cache for total cost
	 */
	private static double cost;

	/**
	 * for whether output of variables is needed
	 */
	private static boolean diagnoseDataFlag = false;
	
	private static List<NetlistBlock> clockGeneratingBlocks= new LinkedList<NetlistBlock>();

	/**
	 * current placement
	 */
	private static NetlistBlock[][][] sBlocks;

	
	/**
	 * main method
	 * @param args
	 */
	public static void main(String[] args) {

		long startTime = System.currentTimeMillis();
		String netlistFilePath= args[0];
		String architectureFilePath= args[1];
		String placementFilePath= args[2];
		
		int[] commandLineInput = parseCommandlineArguments(args); //array in form from int to initialize architectureParser with
		double[] commandLineDoubleInput = parseCommandlineDoubleArguments(args); //array in form from double to initialize architectureParser with
		double lambda;
		if(commandLineDoubleInput[0] == -1) lambda= 0.5; //default value
		else lambda= commandLineDoubleInput[0]; //value passed via command line
		int stepCountFactor;
//		System.out.println("stepCountFACTOR: "+ commandLineInput[10]);
		if(commandLineInput[10] == -1) stepCountFactor= 10; //default value
		else stepCountFactor= (int)commandLineInput[10]; //value passed via command line
		
		if(commandLineInput[11] == 0) diagnoseDataFlag= true; //set flag to true
		
		long seed;
		if(commandLineInput[9] == -1) seed= 0; //default value
		else seed= commandLineInput[9]; //value passed via command line
		if(seed != 0) {
			rand= new Random(seed);
		}
		else {
			seed= new Random().nextLong(); //create random seed and store for debugging
			rand= new Random(seed);
		}
//		System.out.println("seed: " + seed);
		
		
		
		
		
		try {
			architectureParser= new ArchitectureParser(architectureFilePath, commandLineInput);

			architectureParser.parseAll();
			ParameterManager.initialize(netlistFilePath, architectureFilePath, placementFilePath, architectureParser.getAllParameters());

			netlistParser= new NetlistParser(netlistFilePath);

			String[] netlistFilePathSplit= netlistFilePath.split("/");
			String[] architectureFilePathSplit= architectureFilePath.split("/");
			placementWriter= new PlacementWriter(netlistFilePathSplit[netlistFilePathSplit.length - 1], architectureFilePathSplit[architectureFilePathSplit.length - 1]);
			
			timingAnalyzer= TimingAnalyzer.getInstance();
			
			structureManager= StructureManager.getInstance();
			parameterManager= ParameterManager.getInstance();
			
			parse();
			
			placingAreaSize= (int) Math.ceil(Math.sqrt(logicBlocks.length));
			
			place(stepCountFactor, lambda);
			
			long stopTime = System.currentTimeMillis();
		    long elapsedTime = stopTime - startTime;
		    System.out.println("Execution time (without File IO) in s: "+elapsedTime/1000);
		    System.out.println("Execution time (without File IO) in ms: "+elapsedTime);
	
			
		    placeClockGeneratingBlocks();
		    normalizeIOBlockPadPlacement();
		    
			placementWriter.write(placementFilePath);
		   
			if(diagnoseDataFlag) {
				//printDiagnoseData(placementFilePath);
				plotDiagnoseData();
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ErrorReporter.reportFileNotFoundError(e.toString());
		} catch (Exception e) {
			System.err.println("Execution aborted as a result of previously detected errors.");
			e.printStackTrace();
		}
	}


	private static void normalizeIOBlockPadPlacement() {
		// TODO Auto-generated method stub
		
	}


	/**
	 * stores every argument beside args[0,1,2,3] (path arguments) for the initialization of the arch file in an array
	 * @param args 
	 * @return
	 */
	private static int[] parseCommandlineArguments(String[] args) {
		int[] parameterInitialized = new int[] {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
		for(int i = 3; i< args.length; i++) {
			switch(args[i]) {
			case "-X":
				i++;
				parameterInitialized[0]= Integer.valueOf(args[i]);
			break;
			
			case "-Y":
				i++;
				parameterInitialized[1]= Integer.valueOf(args[i]);
			break;
			
			case "-W":
				i++;
				parameterInitialized[2]= Integer.valueOf(args[i]);
			break;
			
			case "-Tipad":
				i++;
				parameterInitialized[3]= Integer.valueOf(args[i]);
			break;
			
			case "-Topad":
				i++;
				parameterInitialized[4]= Integer.valueOf(args[i]);
			break;
			
			case "-Tswitch":
				i++;
				parameterInitialized[5]= Integer.valueOf(args[i]);
			break;
			
			case "-Tcomb":
				i++;
				parameterInitialized[6]= Integer.valueOf(args[i]);
			break;
			
			case "-TFFin":
				i++;
				parameterInitialized[7]= Integer.valueOf(args[i]);
			break;
			
			case "-TFFout":
				i++;
				parameterInitialized[8]= Integer.valueOf(args[i]);
			break;
			
			case "-seed":
				i++;
				parameterInitialized[9]= Integer.valueOf(args[i]);
			break;
			

			case "-stepCountFactor":
				i++;
				parameterInitialized[10]= Integer.valueOf(args[i]);
//				System.out.println("stepCountF.: "+ parameterInitialized[1]);
			break;
			
			case "-diagnoseData":
				parameterInitialized[11]= 0;
			break;
			}
		}
		return parameterInitialized;
	}
	
	/**
	 * stores argument of type double (only lambda)
	 * @param args 
	 * @return
	 */
	private static double[] parseCommandlineDoubleArguments(String[] args) {
		
		double[] parameterInitialized = new double[] {-1};
		for(int i = 3; i< args.length; i++) {
			
			switch(args[i]) {
			
			case "-lambda":
				i++;
				parameterInitialized[0]= Double.valueOf(args[i]);
			break;
			
			}
			
		}
		return parameterInitialized;
	}
	

	/**
	 * parses netlist file and initialize the block lists<br>
	 * <b>post-state:</b> <br>
	 * all provided input files have been parsed into consistent data structures managed by the StructureManager, or errors have been printed, <br>
	 * iOBlocks and logicBlocks contain all parsed blocks of their respective type
	 */
	private static void parse() {
		
		netlistParser.parseAll();
		List<IOBlock> iOBlocksTemp= new LinkedList<IOBlock>();
		List<LogicBlock> logicBlocksTemp= new LinkedList<LogicBlock>();
		for(NetlistBlock b : structureManager.getBlockMap().values()) {
			if(b.isNoClockGeneratingBlock()) {
				if(b instanceof IOBlock) {
					iOBlocksTemp.add((IOBlock) b);
				}
				else if(b instanceof LogicBlock){
					logicBlocksTemp.add((LogicBlock) b);
				}
			}
			else {
				clockGeneratingBlocks.add(b);
			}
		}
//		System.out.println(iOBlocksTemp);
		iOBlocks= iOBlocksTemp.toArray(new IOBlock[0]);
		logicBlocks= logicBlocksTemp.toArray(new LogicBlock[0]);
		iOBlockCount= iOBlocks.length;
		blockCount= iOBlockCount + logicBlocks.length;
//		System.out.println(iOBlocks);
//		System.out.println(logicBlocks);
//		System.out.println(logicBlocks[0]);
//		System.out.println(logicBlocks[3]);
//		System.out.println(logicBlocks[6]);
//		System.out.println(logicBlocks[9]);
	}
	
	/**
	 * the main placing function, simulated annealing algorithm similar to VPR
	 * @param stepCountFactor
	 * @param newLambda
	 */
	private static void place(int stepCountFactor, double newLambda) {
		
		int stepCount= (int) Math.floor( stepCountFactor * Math.pow((double) blockCount, ( (double) 4 / (double) 3 ) ) );
//		System.out.println("stepCountfac: "+ stepCountFactor);
//		System.out.println("stepCountMultiplier: "+ Math.pow((double) blockCount, ( (double) 4 / (double) 3 ))) ;
//		System.out.println("stepCount: "+ stepCount);
//		System.out.println("stepCount as double: "+ stepCountFactor * Math.pow((double) blockCount, ( (double) 4 / (double) 3 )));
		double lambda= newLambda;
		
		sBlocks = randomBlockPlacement() ; 
		int[] logicBlockSwap = new int[6];
		double rA= 1;
		int acceptedTurns= 0;
		int rejectedTurns= 0;
		double rLimit = computeInitialrLimit() ; //absolute norm
		double rLimitInitial= rLimit;
		double rLimitLogicBlocks = computeInitialrLimitLogicBlocks();  //infinity norm (for efficient random slot selection)

		double critExp = computeNewExponent(rLimit, rLimitInitial); 
		
		int numberOfNets= 0;
		
		for(Net n : structureManager.getNetCollection()) { //generate all paths
			if(! n.getIsClocknNet()) {
				paths.addAll(n.generateSimplePaths());
				numberOfNets++;
			}
		}
		for(SimplePath p : paths) { 
			p.registerAtBlocks();
		}
		timingAnalyzer.initializeDelayLUT();


//		Collection<Net> allNets = structureManager.getNetCollection();
//		double returnVal = 0 ;
//		for(Net currentNet: allNets) {
//			if(!currentNet.getIsClocknNet()) {
//				currentNet.initializeWiringCost();
//				returnVal += currentNet.getWiringCost();
//			}
//		}
		oldWiringCost = totalWiringCost();//returnVal;
		analyzeTiming(); 
		oldTimingCost = TimingCost(critExp) ; 
//		System.out.println("init timing cost: " + oldTimingCost);
//		System.out.println("INIT wiring cost: " + oldWiringCost);
//		
		double temp = computeInitialTemperature(rLimit, rLimitLogicBlocks, critExp, lambda) ; 
		
		//double avgCostPerNet= getAvgCostPerNet();
		//experimental: use avg timing cost per path instead of complete cost and per net
		double avgTimingCostPerPath= getAvgTimingCostPerPath(critExp);
		double avgPathsPerNet= paths.size() / (double) numberOfNets;
		
		
//		System.out.println("initial Temp: " + temp);
//		System.out.println("Temp limit: " + (0.005 * avgPathsPerNet * avgTimingCostPerPath));
//		System.out.println("avgPathsPerNet " +avgPathsPerNet);
//		System.out.println("avgTimingCostPerPath " + avgTimingCostPerPath);
//
//		System.out.println("initial timing Cost: "+ oldTimingCost);
//		System.out.println("initial wiring Cost: "+ oldWiringCost);
//		System.out.println("initial total Cost: "+ cost);
		double outerLoopBound= (0.005 * cost / structureManager.getNetCollection().size());
		System.out.println("outerLoopBound: " + outerLoopBound);
		while(temp > outerLoopBound) { //0.005 * avgPathsPerNet * avgTimingCostPerPath) {//experimental: use avg timing cost per path instead of complete cost and per net
//			System.out.println(0.005 * avgPathsPerNet * avgTimingCostPerPath);
			System.out.println("Temp: " + temp);
//			System.out.println("test");
			/* compute Ta, Tr and slack() */ 
			analyzeTiming() ; 
			
			/* für Normalisierung der Kostenterme */ 
//			allNets = structureManager.getNetCollection();
//			returnVal = 0 ;
//			for(Net currentNet: allNets) {
//				if(!currentNet.getIsClocknNet()) {
//					currentNet.initializeWiringCost();
//					returnVal += currentNet.getWiringCost();
//				}
//			}
			oldWiringCost = totalWiringCost();//returnVal;
			//TODO
			System.out.println("total wiring cost: " + oldWiringCost);
			oldTimingCost = TimingCost(critExp) ; 
			System.out.println("total timing cost: " + oldTimingCost);
//			System.out.println("ce: " + critExp);
			for(int j = 0; j < stepCount; j++) {
				
				double swapAnywaysFactor= rand.nextDouble();
				/*Snew = GenerateSwap(S, Rlimit) ; */
				double newTimingCost;
				double deltaWiringCost;
				double deltaTimingCost;
				double newWiringCost;
				double deltaCost;
				
				
				if(rand.nextInt(blockCount) < iOBlockCount) { //swap IO blocks
					swapIOBlocks(rLimit, logicBlockSwap);
					
//					System.out.println("swap io block");
//					System.out.println(logicBlockSwap[0]);
//					System.out.println(logicBlockSwap[1]);
//					System.out.println(logicBlockSwap[2]);
					
					newTimingCost= newTimingCostSwapBetter(critExp, logicBlockSwap, oldTimingCost); //only recompute changed values
					deltaWiringCost = calcDeltaTotalWiringCost(logicBlockSwap);//calculates delta wiring cost with hashmap and logicBlockSwap
					newWiringCost= oldWiringCost + deltaWiringCost;
					
					deltaTimingCost = newTimingCost - oldTimingCost ; 
					//System.out.println("delta timing cost: " + deltaTimingCost);
//					newWiringCost = totalWiringCost();
//					deltaWiringCost = newWiringCost - oldWiringCost;
//					System.out.println("!delta wiring cost: " + deltaWiringCost);
					//System.out.println("total timing cost: " + newTimingCost);
					deltaCost = lambda * (deltaTimingCost/oldTimingCost) + (1 - lambda) * (deltaWiringCost/oldWiringCost); 
					//System.out.println("delta abs cost: " + deltaCost);
					
					if (deltaCost <= 0) { 
						//System.out.println("swapped io block " + sBlocks[logicBlockSwap[0]][logicBlockSwap[1]][logicBlockSwap[2]].getName() + " at (" + logicBlockSwap[0] + "," + logicBlockSwap[1] + ") to ("  + logicBlockSwap[3] + "," + logicBlockSwap[4] + ")");
						applySwap(logicBlockSwap);
						
						oldTimingCost= newTimingCost; //update buffer
						oldWiringCost= newWiringCost;
						acceptedTurns++;
						cost += deltaCost;
					}
					else if(swapAnywaysFactor < (Math.exp((-1 * deltaCost / temp)))) { 
						//System.out.println("swapped io block " + sBlocks[logicBlockSwap[0]][logicBlockSwap[1]][logicBlockSwap[2]].getName() + " at (" + logicBlockSwap[0] + "," + logicBlockSwap[1] + ") to ("  + logicBlockSwap[3] + "," + logicBlockSwap[4] + ")");
						applySwap(logicBlockSwap);
						oldTimingCost= newTimingCost; //update buffer
						oldWiringCost= newWiringCost;
						acceptedTurns++;
						cost += deltaCost;
					}
					else {
						rejectedTurns++;
						for(SimplePath p : sBlocks[logicBlockSwap[0]][logicBlockSwap[1]][logicBlockSwap[2]].getConnectedPaths()) {
							p.resetCostCache(); //reset cost cache to valid value
						}
						if(sBlocks[logicBlockSwap[3]][logicBlockSwap[4]][logicBlockSwap[5]] != null) {
							for(SimplePath p : sBlocks[logicBlockSwap[3]][logicBlockSwap[4]][logicBlockSwap[5]].getConnectedPaths()) {
								p.resetCostCache(); //reset cost cache to valid value
							}
						}
						resetWiringCost(sBlocks[logicBlockSwap[0]][logicBlockSwap[1]][logicBlockSwap[2]]);
						if(sBlocks[logicBlockSwap[3]][logicBlockSwap[4]][logicBlockSwap[5]] != null) {
							resetWiringCost(sBlocks[logicBlockSwap[3]][logicBlockSwap[4]][logicBlockSwap[5]]);
						}
					}
				}
				else { //swap logic blocks

					swapLogicBlocks(rLimitLogicBlocks, logicBlockSwap);
					
//					System.out.println("swap logic block");
//					System.out.println(logicBlockSwap[0]);
//					System.out.println(logicBlockSwap[1]);
//					System.out.println(logicBlockSwap[2]);
					
					newTimingCost= newTimingCostSwapBetter(critExp, logicBlockSwap, oldTimingCost); //only recompute changed values
					//
					//deltaWiringCost = oldWiringCost - totalWiringCost(sBlocks);
					deltaWiringCost = calcDeltaTotalWiringCost(logicBlockSwap);//calculates delta wiring cost with hashmap and logicBlockSwap

					newWiringCost= oldWiringCost + deltaWiringCost;
					//double newWiringCost= newWiringCostSwap(sBlocks, logicBlockSwap);
					deltaTimingCost = newTimingCost - oldTimingCost ; 
					//
					//System.out.println("delta timing cost: " + deltaTimingCost);
//					newWiringCost = totalWiringCost();
//					deltaWiringCost = newWiringCost - oldWiringCost;
//					System.out.println("!delta wiring cost: " + deltaWiringCost);
					//System.out.println("total timing cost: " + newTimingCost);
					//double deltaWiringCost = oldWiringCost - newWiringCost ; 
					deltaCost = lambda * (deltaTimingCost/oldTimingCost) + (1 - lambda) * (deltaWiringCost/oldWiringCost); 
					//System.out.println("delta abs cost: " + deltaCost);
					if (deltaCost <= 0) { 
//						System.out.println("swapped logic block " + sBlocks[logicBlockSwap[0]][logicBlockSwap[1]][logicBlockSwap[2]].getName() + " at (" + logicBlockSwap[0] + "," + logicBlockSwap[1] + ") to ("  + logicBlockSwap[3] + "," + logicBlockSwap[4] + ")");
						applySwap(logicBlockSwap);
						oldTimingCost= newTimingCost; //update buffer
						oldWiringCost= newWiringCost;
						acceptedTurns++;
						cost += deltaCost;
					}
					else if(swapAnywaysFactor < (Math.exp((-1 * deltaCost / temp)))) { 
//						System.out.println("deltaCost" + deltaCost);
//						System.out.println("(Math.exp((-1 * deltaCost / temp))): " + (Math.exp((-1 * deltaCost / temp))));
//						System.out.println("swapped logic block " + sBlocks[logicBlockSwap[0]][logicBlockSwap[1]][logicBlockSwap[2]].getName() + " at (" + logicBlockSwap[0] + "," + logicBlockSwap[1] + ") to ("  + logicBlockSwap[3] + "," + logicBlockSwap[4] + ")");
						applySwap(logicBlockSwap);
						oldTimingCost= newTimingCost; //update buffer
						oldWiringCost= newWiringCost;
						acceptedTurns++;
						cost += deltaCost;
					}
					else {
						rejectedTurns++;
						for(SimplePath p : sBlocks[logicBlockSwap[0]][logicBlockSwap[1]][logicBlockSwap[2]].getConnectedPaths()) {
							p.resetCostCache(); //reset cost cache to valid value
						}
						if(sBlocks[logicBlockSwap[3]][logicBlockSwap[4]][logicBlockSwap[5]] != null) {
							for(SimplePath p : sBlocks[logicBlockSwap[3]][logicBlockSwap[4]][logicBlockSwap[5]].getConnectedPaths()) {
								p.resetCostCache(); //reset cost cache to valid value
							}
						}
						resetWiringCost(sBlocks[logicBlockSwap[0]][logicBlockSwap[1]][logicBlockSwap[2]]);
						if(sBlocks[logicBlockSwap[3]][logicBlockSwap[4]][logicBlockSwap[5]] != null) {
							resetWiringCost(sBlocks[logicBlockSwap[3]][logicBlockSwap[4]][logicBlockSwap[5]]);
						}
					}
				}


				
			}
			rA= (double) acceptedTurns / ((double) acceptedTurns + (double) rejectedTurns); //compute new rA

			//to track totalWiringCost, AcceptanceRate, RLimit and RLimitLogicBlock right after each step#
			if(diagnoseDataFlag) {
				outputTotalCost.add(cost);
				outputTotalWiringCost.add(oldWiringCost);
				outputTotalTimingCost.add(oldTimingCost);
				outputAcceptanceRate.add(rA);
				outputRLimit.add(rLimit);
				outputRLimitLogicBlock.add(rLimitLogicBlocks);
			}
			
//			System.out.println("rA was: "+ rA);
//			System.out.println("acceptedTurns: "+ acceptedTurns);
//			System.out.println("rejectedTurns: "+ rejectedTurns);
			acceptedTurns= 0; //reset counters for rA computation
			rejectedTurns= 0;
			temp = UpdateTemp(temp, rA) ; 
			rLimit = UpdateRlimit(rLimit, rA) ;
			rLimitLogicBlocks = UpdateRlimitLogicBlocks(rLimitLogicBlocks, rA) ;
			critExp = computeNewExponent(rLimit, rLimitInitial) ; 
			
		}
//		System.out.println("final timing Cost: "+ oldTimingCost);
		System.out.println("final wiring Cost: "+ oldWiringCost);
//		System.out.println("final timing cost exact: "+ TimingCost(critExp));
//		System.out.println("final wiring Cost exact: "+ totalWiringCost(sBlocks));
//		System.out.println("final total Cost: "+ cost);
		
	}

	/**
	 * adds up all wiring costs from every existing net
	 * @param sBlocks
	 * @return
	 */
	private static double totalWiringCost() {
		Collection<Net> allNets = structureManager.getNetCollection();
		double returnVal = 0 ;
		for(Net currentNet: allNets) {
			if(!currentNet.getIsClocknNet()) {
				currentNet.initializeWiringCost();
				returnVal += currentNet.getWiringCost();
			}
		}
		oldWiringCost = returnVal;
//		System.out.println("new wiring cost: " + oldWiringCost);
		return returnVal;
	}

/**
 * gets average timing cost per path
 * @param ce
 * @return
 */
	private static double getAvgTimingCostPerPath(double ce) {
		double sum= 0;
		for(SimplePath p : paths) {
			sum+= p.timingCost(ce);
		}
		return sum / (double) paths.size();
	}

	/**
	 * reset changed wiring cost values if swap was rejected
	 * @param block
	 */
	private static void resetWiringCost(NetlistBlock block) {
		Net[] affectedNets = block.getNet();
		for(Net net: affectedNets) { 
			if(net != null) {
				net.resetWiringCost();
			}
		}
	}

	/**
	 * side effect: updates netWithValues accordingly
	 * @param logicBlockSwap
	 * @param sBlocks
	 * @return delta wiring cost before swap and after swap: oldCost minus newCost: for good swaps, the result is positiv
	 */
	private static double calcDeltaTotalWiringCost(int[] logicBlockSwap) {
		//System.out.println(logicBlockSwap[0] + " ; " + logicBlockSwap[1]);
		NetlistBlock block1= sBlocks[logicBlockSwap[0]][logicBlockSwap[1]][logicBlockSwap[2]];
		//System.out.println(sBlocks[logicBlockSwap[0]][logicBlockSwap[1]][logicBlockSwap[2]]);
		NetlistBlock block2= sBlocks[logicBlockSwap[3]][logicBlockSwap[4]][logicBlockSwap[5]];
		//System.out.println(logicBlockSwap[3] + " ; " + logicBlockSwap[4]);
		Net[] affectedNets = block1.getNet();
		double returnVal = 0;
		for(Net net: affectedNets) { 
			if(net != null) {
				returnVal += net.update(block1, block2, logicBlockSwap);
				//System.out.println("returnVal "+ returnVal);
			}
		}
		Net[] affectedNets2= new Net[0];
		if(block2 instanceof NetlistBlock) {
			affectedNets2 = block2.getNet();
			for(Net net: affectedNets2) { 
				if(net != null) {
					returnVal += net.update(block1, block2, logicBlockSwap);
				}
			}
		}
		for(Net net: affectedNets) { 
			if(net != null) {
				net.resetUpdatedFlag();
			}
		}
		for(Net net: affectedNets2) { 
			if(net != null) {
				net.resetUpdatedFlag();
			}
		}
//		System.out.println("delta wiring cost: " + returnVal);
		/*
		if(! (returnVal == returnVal)) {
			System.out.println("block1: " + block1.getName() + " (" + block1.getX() + "," + block1.getY() + "|" + (block1.getSubblk_1() ? 1 : 0) + ")");
			if(block2 instanceof NetlistBlock) {
				System.out.println("block2: " + block2.getName() + " (" + block2.getX() + "," + block2.getY() + "|" + (block2.getSubblk_1() ? 1 : 0) + ")");
			}
			else System.out.println("empty slot...");
		}
		else {
			System.out.println((block2 instanceof NetlistBlock) + " " + (block1 instanceof IOBlock) + " " + (block2 instanceof IOBlock));
		}
		*/
		return returnVal;
	}

	/**
	 * computes timing cost after swap
	 * @param ce
	 * @param logicBlockSwap
	 * @return
	 */
	private static double newTimingCostSwap(double ce, int[] logicBlockSwap) {
		double sum= 0;
		//System.out.println("size of paths list in placer: " + paths.size());
		for(SimplePath p : paths) {
			sum += p.timingCostSwap(ce, logicBlockSwap);
		}
		return sum;
	}

	/**
	 * computes timing cost after swap
	 * @param sBlocks
	 * @param ce
	 * @param logicBlockSwap
	 * @param oldCost
	 * @return
	 */
	private static double newTimingCostSwapBetter(double ce, int[] logicBlockSwap, double oldCost) {
//		System.out.println(sBlocks[logicBlockSwap[0]][logicBlockSwap[1]][logicBlockSwap[2]]);
//		System.out.println(logicBlockSwap[0]);
//		System.out.println(logicBlockSwap[1]);
//		System.out.println(logicBlockSwap[2]);
		for(SimplePath p : sBlocks[logicBlockSwap[0]][logicBlockSwap[1]][logicBlockSwap[2]].getConnectedPaths()) {
			if(! p.getUpdated()) {
				oldCost-= p.getCachedCost(ce);
				oldCost+= p.timingCostSwap(ce, logicBlockSwap);
				p.setUpdated(true); //set flag to avoid duplicate treatment
			}
		}
		if(sBlocks[logicBlockSwap[3]][logicBlockSwap[4]][logicBlockSwap[5]] != null) {
			for(SimplePath p : sBlocks[logicBlockSwap[3]][logicBlockSwap[4]][logicBlockSwap[5]].getConnectedPaths()) {
				if(! p.getUpdated()) {
					oldCost-= p.getCachedCost(ce);
					oldCost+= p.timingCostSwap(ce, logicBlockSwap);
					p.setUpdated(true); //set flag to avoid duplicate treatment
				}
			}
		}
		for(SimplePath p : sBlocks[logicBlockSwap[0]][logicBlockSwap[1]][logicBlockSwap[2]].getConnectedPaths()) {
			p.setUpdated(false); //reset flag
		}
		if(sBlocks[logicBlockSwap[3]][logicBlockSwap[4]][logicBlockSwap[5]] != null) {
			for(SimplePath p : sBlocks[logicBlockSwap[3]][logicBlockSwap[4]][logicBlockSwap[5]].getConnectedPaths()) {
				p.setUpdated(false); //reset flag
			}
		}
		
		return oldCost;
	}

	/**
	 * computes the coordinates of a random logic block and it's random swapping partner (empty slot or other logic block)
	 * @param sBlocks current placement of blocks
	 * @param rLimit distance limit for swap
	 * @param blockSwap array for storing the computed values: {block1_x, block1_y, block1_subblk (0 for logic blocks), block2_x, block2_y, block2_subblk (0 for logic blocks)}
	 */
	private static void swapLogicBlocks(double rLimit, int[] blockSwap) {
		/*
		    1
		   234
		  56 78
		   9ab
		    c
		    
		  13ac ->mittlere spalte, ohne nullpunkt
		  b249
		  5678
		 */
		/*
		int x= rand.nextInt(2 * (int) Math.floor(rLimit)) + 1;
		int y= rand.nextInt((int) Math.floor(rLimit) + 2);
		*/
		int index= rand.nextInt(logicBlocks.length); //get a random logic block for swap (swapping two empty places wouldn't gain anything)
		LogicBlock block1= logicBlocks[index];
		
		int top= (int) Math.ceil(rLimit); //compute boundaries to swappable area (limited by boundaries of placable area and by rLimit, checking rLimit with infinity norm for good performance while retaining equally distributed probability)
		if(block1.getY() - top < biasY) top = block1.getY() - biasY;
		int bottom= (int) Math.ceil(rLimit);
		if(block1.getY() + bottom > biasY + (placingAreaSize - 1)) bottom = biasY + (placingAreaSize - 1) - block1.getY();
		int left= (int) Math.ceil(rLimit);
		if(block1.getX() - left < biasX) left = block1.getX() - biasX;
		int right= (int) Math.ceil(rLimit);
		if(block1.getX() + right > biasX + (placingAreaSize - 1)) right = biasX + (placingAreaSize - 1) - block1.getX();
		
		int x= rand.nextInt(left + right + 1); //coordinates of block2 relative to block1
		x -= left;
		int y= rand.nextInt(top + bottom + 1);
		y-= top; 
		
		while(x == 0 && y == 0) { //avoid swapping a block with itself
//			System.out.println("Logic Block Swap While");
			x= rand.nextInt(left + right + 1);
//			System.out.println(left + right + 1);
			x -= left;
			y= rand.nextInt(top + bottom + 1);
			y-= top; 
		}
		
		blockSwap[0] = block1.getX();
		blockSwap[1] = block1.getY();
		blockSwap[2] = 0;
		blockSwap[3] = block1.getX() + x;
		blockSwap[4] = block1.getY() + y;
		blockSwap[5] = 0;
		
		block1.setChanged();
		if(sBlocks[blockSwap[3]][blockSwap[4]][0] != null) {
			sBlocks[blockSwap[3]][blockSwap[4]][0].setChanged();
		}
	}

	/**
	 * computes the coordinates of a random io block and it's random swapping partner (empty slot or other io block)
	 * @param sBlocks current placement of blocks
	 * @param rLimit distance limit for swap
	 * @param blockSwap array for storing the computed values: {block1_x, block1_y, block1_subblk, block2_x, block2_y, block2_subblk}
	 */
	private static void swapIOBlocks(double rLimit, int[] blockSwap) {
		int index= rand.nextInt(iOBlocks.length); //get a random IO block for swap (swapping two empty places wouldn't gain anything)
		IOBlock block1= iOBlocks[index];
//		System.out.println(block1);
//		System.out.println(block1.getName());
//		System.out.println(block1.getX());
//		System.out.println(block1.getY());
//		System.out.println(block1.getSubblk_1());
		int swapDistance= rand.nextInt((int) Math.ceil(rLimit)) + 1; //get distance of swap (not 0, as swapping pad 0 with pad 1 at same coordinates wouldn't change cost)
		boolean ccw= rand.nextBoolean(); //get swap direction (clockwise or counterclockwise)
		boolean subblk_1= rand.nextBoolean(); //get pad number of swap partner
		int xCoord= block1.getX(); //get starting point position
		int yCoord= block1.getY();
		while(swapDistance > 0) { //walk around the rim until the target position is reached
//			System.out.println("limit: "+ swapDistance);
//			System.out.println("IOBlockSwapWhile");
			if(yCoord == parameterManager.Y_GRID_SIZE + 1) { //top IO
				if(ccw) {
					if(swapDistance > xCoord - 1) { //go to left IO
						swapDistance-= xCoord;
						xCoord= 0;
						yCoord= parameterManager.Y_GRID_SIZE;
					}
					else { //stay in top IO, end
						xCoord-= swapDistance;
						swapDistance= 0;
					}
				}
				else {
					if(swapDistance + xCoord > parameterManager.X_GRID_SIZE) { //go to right IO
						swapDistance-= (parameterManager.X_GRID_SIZE - xCoord) + 1;
						xCoord= parameterManager.X_GRID_SIZE + 1;
						yCoord= parameterManager.Y_GRID_SIZE;
					}
					else { //stay in top IO, end
						xCoord+= swapDistance;
						swapDistance= 0;
					}
				}
			}
			else if(yCoord == 0) { //bottom IO
				if(!ccw) {
					if(swapDistance > xCoord - 1) { //go to left IO
						swapDistance-= xCoord;
						xCoord= 0;
						yCoord= 1;
					}
					else { //stay in bottom IO, end
						xCoord-= swapDistance;
						swapDistance= 0;
					}
				}
				else {
					if(swapDistance + xCoord > parameterManager.X_GRID_SIZE) { //go to right IO
						swapDistance-= (parameterManager.X_GRID_SIZE - xCoord) + 1;
						xCoord= parameterManager.X_GRID_SIZE + 1;
						yCoord= 1;
					}
					else { //stay in bottom IO, end
						xCoord+= swapDistance;
						swapDistance= 0;
					}
				}
			}
			else if(xCoord == parameterManager.X_GRID_SIZE + 1) { //right IO
				if(!ccw) {
					if(swapDistance > yCoord - 1) { //go to bottom IO
						swapDistance-= yCoord;
						yCoord= 0;
						xCoord= parameterManager.X_GRID_SIZE;
					}
					else { //stay in right IO, end
						yCoord-= swapDistance;
						swapDistance= 0;
					}
				}
				else {
					if(swapDistance + yCoord > parameterManager.Y_GRID_SIZE) { //go to top IO
						swapDistance-= (parameterManager.Y_GRID_SIZE - xCoord) + 1;
						yCoord= parameterManager.Y_GRID_SIZE + 1;
						xCoord= parameterManager.X_GRID_SIZE;
					}
					else { //stay in right IO, end
						yCoord+= swapDistance;
						swapDistance= 0;
					}
				}
			}
			else if(xCoord == 0) { //left IO
				if(ccw) {
					if(swapDistance > yCoord - 1) { //go to bottom IO
						swapDistance-= yCoord;
						yCoord= 0;
						xCoord= 1;
					}
					else { //stay in right IO, end
						yCoord-= swapDistance;
						swapDistance= 0;
					}
				}
				else {
					if(swapDistance + yCoord > parameterManager.Y_GRID_SIZE) { //go to top IO
						swapDistance-= (parameterManager.Y_GRID_SIZE - xCoord) + 1;
						yCoord= parameterManager.Y_GRID_SIZE + 1;
						xCoord= 1;
					}
					else { //stay in right IO, end
						yCoord+= swapDistance;
						swapDistance= 0;
					}
				}
			}
		}
		/*
		System.out.println(block1.getX());
		System.out.println(block1.getY());
		System.out.println(block1.getSubblk_1() ? 1 : 0);
		System.out.println(xCoord);
		System.out.println(yCoord);
		System.out.println(subblk_1 ? 1 : 0);
		*/
		blockSwap[0] = block1.getX();
		blockSwap[1] = block1.getY();
		blockSwap[2] = block1.getSubblk_1() ? 1 : 0;
		blockSwap[3] = xCoord;
		blockSwap[4] = yCoord;
		blockSwap[5] = subblk_1 ? 1 : 0;
		
		block1.setChanged();
		if(sBlocks[blockSwap[3]][blockSwap[4]][blockSwap[5]] != null) {
			sBlocks[blockSwap[3]][blockSwap[4]][blockSwap[5]].setChanged();
		}
	}

	/**
	 * applies a block swap by swapping the entries at the given coordinates
	 * @param sBlocks block placement
	 * @param logicBlockSwap coordinates of the slots to be swapped: {block1_x, block1_y, block1_subblk (0 for logic blocks), block2_x, block2_y, block2_subblk (0 for logic blocks)}
	 */
	private static void applySwap(int[] logicBlockSwap) {
		NetlistBlock block1= sBlocks[logicBlockSwap[0]][logicBlockSwap[1]][logicBlockSwap[2]];
		NetlistBlock block2= sBlocks[logicBlockSwap[3]][logicBlockSwap[4]][logicBlockSwap[5]];
//		System.out.println("swap " + sBlocks[logicBlockSwap[0]][logicBlockSwap[1]][logicBlockSwap[2]] + " with " + sBlocks[logicBlockSwap[3]][logicBlockSwap[4]][logicBlockSwap[5]]);
		sBlocks[logicBlockSwap[0]][logicBlockSwap[1]][logicBlockSwap[2]]= block2;
		sBlocks[logicBlockSwap[3]][logicBlockSwap[4]][logicBlockSwap[5]]= block1;
//		System.out.println("swapped " + sBlocks[logicBlockSwap[0]][logicBlockSwap[1]][logicBlockSwap[2]] + " with " + sBlocks[logicBlockSwap[3]][logicBlockSwap[4]][logicBlockSwap[5]]);
		if(block2 instanceof NetlistBlock) block2.updateCoordinates(logicBlockSwap[0], logicBlockSwap[1]); //set new coordinates
		block1.updateCoordinates(logicBlockSwap[3], logicBlockSwap[4]);
		if(block1 instanceof IOBlock) { //set subblk number
			block1.setSubblk_1(logicBlockSwap[5] == 1);
		}
		if(block2 instanceof IOBlock) { //set subblk number
			block2.setSubblk_1(logicBlockSwap[2] == 1);
		}
	}

	/**
	 * compute delay (tA) and slack for all paths and annotate it
	 */
	private static void analyzeTiming() {
		int dMax= -1;
		for(SimplePath p : paths) { 
			int delay= p.computeDelay();
			if(delay > dMax) {
				dMax= delay;
			}
		}
		for(SimplePath p : paths) { 
			p.computeSlack(dMax);
		}
	}

	/**
	 * update rLimit
	 * @param rLimitOld old rLimit
	 * @param rAOld old rA
	 * @return new rLimit
	 */
	private static double UpdateRlimit(double rLimitOld, double rAOld) {
		//System.out.println("Acceptance rate was: " + rAOld);
		double temp= rLimitOld * ( ( 1 + rAOld) - 0.44 );
		if(temp < 1) temp= 1;
		if(temp > parameterManager.X_GRID_SIZE + parameterManager.Y_GRID_SIZE) temp= parameterManager.X_GRID_SIZE + parameterManager.Y_GRID_SIZE;
		return temp;
	}

	/**
	 * update rLimitLogicBlocks
	 * @param rLimitLogicBlocksOld old rLimitLogicBlocks
	 * @param rAOld old rA
	 * @return new rLimitLogicBlocks
	 */
	private static double UpdateRlimitLogicBlocks(double rLimitLogicBlocksOld, double rAOld) {
		double temp= rLimitLogicBlocksOld * ( ( 1 + rAOld) - 0.44 );
		if(temp < 1) temp= 1;
		if(temp > parameterManager.X_GRID_SIZE + parameterManager.Y_GRID_SIZE) {
			if(parameterManager.X_GRID_SIZE > parameterManager.Y_GRID_SIZE) temp= parameterManager.X_GRID_SIZE;
			else temp= parameterManager.Y_GRID_SIZE;
		}
		return temp;
	}

	/**
	 * update the temperature according to the cooling schedule
	 * @param tOld old temperature
	 * @param rA acceptance rate
	 * @return new temperature
	 */
	private static double UpdateTemp(double tOld, double rA) {
		if(rA > 0.8) {
			if(rA > 0.96) return 0.5 * tOld;
			else return 0.9 * tOld;
		}
		else {
			if(rA > 0.15) return 0.95 * tOld;
			else return 0.8 * tOld;
		}
	}

	/**
	 * computes total timing cost
	 * @param ce
	 * @return
	 */
	private static double TimingCost(double ce) {
		double sum= 0;
		for(SimplePath p : paths) {
			sum += p.timingCost(ce);
		}
		return sum;
	}


	/**
	 * compute new criticalityExponent
	 * @param rLimit current rLimit
	 * @param rLimitInitial initial rLimit
	 * @return the new critExp
	 */
	private static double computeNewExponent(double rLimit, double rLimitInitial) {
//		System.out.println("ce: " + rLimit + ", " + rLimitInitial + ", " + (( ( 1 - ( ( Math.ceil(rLimit) - 1 ) / ( Math.ceil(rLimitInitial) - 1 ) ) ) * 7 ) + 1));
		return ( ( 1 - ( ( Math.ceil(rLimit) - 1 ) / ( Math.ceil(rLimitInitial) - 1 ) ) ) * 7 ) + 1;
	}

	/**
	 * computes the initial maximum range for io block swaps (absolute norm, because grid)
	 * @return the initial rLimit
	 */
	private static double computeInitialrLimit() {
		return parameterManager.X_GRID_SIZE + parameterManager.Y_GRID_SIZE;
	}
	
	/**
	 * computes the initial maximum range for logic block swaps (infinity norm)
	 * @return the initial rLimit
	 */
	private static double computeInitialrLimitLogicBlocks() {
		if(parameterManager.X_GRID_SIZE > parameterManager.Y_GRID_SIZE) return parameterManager.X_GRID_SIZE;
		else return parameterManager.Y_GRID_SIZE;
	}

	/**
	 * computes the initial temperature according to the formula on the slides
	 * @param sBlocks initial random placement (will be changed, but still be random, therefore no need to save old placement)
	 * @param rLimit maximum range for swaps
	 * @param critExp criticality exponent
	 * @param lambda lambda for weighting wiring and timing cost
	 * @param lambda2 
	 * @return initial temperature value
	 */
	private static double computeInitialTemperature(double rLimit, double rLimitLogicBlocks, double critExp, double lambda) {
		
		System.out.println("computing initial temperature...");
		double n= blockCount;
		double cQuer= 0;
		int sumCSquare= 0;
		cost= 1;
		double[] cI= new double[(int) n];
		for(int i= 0; i < (int) n; i++) {
			cI[i]= cost + applySwapAndGetCDelta(rLimit, rLimitLogicBlocks, critExp, lambda);
			//System.out.println("cost after swap:" + cost);
			//cost += cI;
			//sumCSquare+= cost * cost;
			//cQuer+= cost / n;
//			System.out.println("ci: " + cI);
			cQuer += cI[i];
		}
		cQuer= cQuer / n;
		double sumDeltaCSquare= 0;
		for(int i= 0; i < (int) n; i++) {
			sumDeltaCSquare+= Math.pow(cI[i] - cQuer, 2);
		}
		System.out.println("initial temperature computed.");
//		System.out.println("sumCSqure: " +sumCSquare);
//		System.out.println("n " + n);
//		System.out.println("c quer " +cQuer);
//		System.out.println("under root: " + ( ( (double) 1 / ( n - (double) 1) ) * sumDeltaCSquare ) ) ;
		return 20 * Math.sqrt( ( (double) 1 / ( n - (double) 1) ) * sumDeltaCSquare  ) ; 
	
	}

	/**
	 * computes and applies a random swap, then computes the new value of the cost function and returns it
	 * @param sBlocks current placement
	 * @param rLimit maximum range for swaps
	 * @param critExp criticality exponent
	 * @param lambda lambda for weighting wiring and timing cost
	 * @return cost after the swap
	 */
	private static double applySwapAndGetCDelta(double rLimit, double rLimitLogicBlocks, double critExp, double lambda) {

		int[] blockSwap= new int[6];
		if(rand.nextInt(blockCount) < iOBlockCount) { //swap IO blocks
			//System.out.println("swap io block...");
			swapIOBlocks(rLimit, blockSwap);
		}
		else {
			//System.out.println("swapLogicBlock...");
			swapLogicBlocks(rLimitLogicBlocks, blockSwap);
		}
		
		//System.out.println("compute new cost...");
		double newTimingCost= newTimingCostSwapBetter(critExp, blockSwap, oldTimingCost); //only recompute changed values
		//System.out.println("new timing cost: " + newTimingCost);
		//System.out.println("delta wiring cost: " + calcDeltaTotalWiringCost(blockSwap, sBlocks));
		double newWiringCost= totalWiringCost(); //oldWiringCost + calcDeltaTotalWiringCost(blockSwap, sBlocks);//newWiringCostSwap(sBlocks, blockSwap); //TODO 
//		System.out.println("new wiring cost: " + newWiringCost);
		//System.out.println("new cost computed.");
		
		//System.out.println(blockSwap[0] + "," + blockSwap[1] + "," +blockSwap[2] + "-" + blockSwap[3] + "," + blockSwap[4] + "," +blockSwap[5]);
		//System.out.println("deltaWCOST " +calcDeltaTotalWiringCost(blockSwap, sBlocks));
		applySwap(blockSwap);

//		System.out.println("oldWCOST: "+ oldWiringCost);
//		System.out.println("timingCOST: "+ newTimingCost);
//		System.out.println("wiringCOST: "+ newWiringCost);
//		System.out.println("totalCOST: "+ (lambda * (newTimingCost - oldTimingCost) / oldTimingCost + (1 - lambda) * (newWiringCost - oldWiringCost) / oldWiringCost));
		return lambda * (newTimingCost - oldTimingCost) / oldTimingCost + (1 - lambda) * (newWiringCost - oldWiringCost) / oldWiringCost;  
		
	}


	/**
	 * creates a random initial placement for all LogicBlocks
	 * @return the new random placement as a matrix of all possible block coordinates, containing the blocks in the centered square area of minimum size as required by the task description
	 */
	private static NetlistBlock[][][] randomBlockPlacement() {
		
		int numberOfSlotsLeft= placingAreaSize * placingAreaSize;
		//System.out.println(placingAreaSize);
		biasX= (parameterManager.X_GRID_SIZE - placingAreaSize) / 2 + 1; //+1 to compensate for row/column of IO blocks
		biasY= (parameterManager.Y_GRID_SIZE - placingAreaSize) / 2 + 1;
		NetlistBlock[][][] output= new NetlistBlock[parameterManager.X_GRID_SIZE + 2][parameterManager.Y_GRID_SIZE + 2][2];
		for(LogicBlock b : logicBlocks) {
			
			placeSingleLogicBlock(b, numberOfSlotsLeft, output);
			numberOfSlotsLeft--;
//			System.out.println("set initial coordinates: placed " + "LogicBlock" + " [" + b.getName() + "] at (" + (biasX + (index / placingAreaSize)) + "," + (biasY + (index % placingAreaSize)) + ")" );
		}
		
		numberOfSlotsLeft= (parameterManager.X_GRID_SIZE * 2 + parameterManager.Y_GRID_SIZE * 2) * 2;
		for(IOBlock b : iOBlocks) {
			
			
			placeSingleIOBlock(b, numberOfSlotsLeft, output);
			
			/*
			boolean subBlk1= true;
			int xCoord= 1; //first slot, left of top io
			int yCoord= 0;
			for(int i= 0; i < index; i++) {
				
				if(subBlk1) { //go to next io pad (same coordinates)
					
					subBlk1= false;			
		
					
					int j= i / 2; //compute new coordinates
					if(j < parameterManager.X_GRID_SIZE) { //top io
						xCoord= j + 1;
						yCoord= 0;
					}
					else if(j < parameterManager.X_GRID_SIZE + parameterManager.Y_GRID_SIZE) {
						xCoord= 0;
						yCoord= (j - parameterManager.X_GRID_SIZE) + 1;
					}
					else if(j < parameterManager.X_GRID_SIZE * 2 + parameterManager.Y_GRID_SIZE) {
						xCoord= (j - (parameterManager.X_GRID_SIZE + parameterManager.Y_GRID_SIZE)) + 1;
						yCoord= parameterManager.Y_GRID_SIZE + 1;
					}
					else{
						xCoord= parameterManager.X_GRID_SIZE + 1;
						yCoord= (j - (parameterManager.X_GRID_SIZE * 2 + parameterManager.Y_GRID_SIZE)) + 1;
					}
					
					if(output[xCoord][yCoord][subBlk1 ? 1 : 0] != null) {
						index++; //skip slots already in use
					}
					
				}
				else { //go to next io pad (next coordinate) (clockwise)
					
					subBlk1= true;

					if(output[xCoord][yCoord][subBlk1 ? 1 : 0] != null) {
							index++; //skip slots already in use
					}
					
				}
				
			}
			
			subBlk1= !subBlk1;

			
			int j= (index - (subBlk1 ? 1 : 0)) / 2; //compute new coordinates
			if(j < parameterManager.X_GRID_SIZE) { //top io
				xCoord= j + 1;
				yCoord= 0;
			}
			else if(j < parameterManager.X_GRID_SIZE + parameterManager.Y_GRID_SIZE) {
				xCoord= 0;
				yCoord= (j - parameterManager.X_GRID_SIZE) + 1;
			}
			else if(j < parameterManager.X_GRID_SIZE * 2 + parameterManager.Y_GRID_SIZE) {
				xCoord= (j - (parameterManager.X_GRID_SIZE + parameterManager.Y_GRID_SIZE)) + 1;
				yCoord= parameterManager.Y_GRID_SIZE + 1;
			}
			else{
				xCoord= parameterManager.X_GRID_SIZE + 1;
				yCoord= (j - (parameterManager.X_GRID_SIZE * 2 + parameterManager.Y_GRID_SIZE)) + 1;
			}

			while(output[xCoord][yCoord][(subBlk1 ? 1 : 0)] != null) {
				index++; //skip slots already in use
				subBlk1= !subBlk1;

				j= (index - (subBlk1 ? 1 : 0)) / 2; //compute new coordinates
				if(j < parameterManager.X_GRID_SIZE) { //top io
					xCoord= j + 1;
					yCoord= 0;
				}
				else if(j < parameterManager.X_GRID_SIZE + parameterManager.Y_GRID_SIZE) {
					xCoord= 0;
					yCoord= (j - parameterManager.X_GRID_SIZE) + 1;
				}
				else if(j < parameterManager.X_GRID_SIZE * 2 + parameterManager.Y_GRID_SIZE) {
					xCoord= (j - (parameterManager.X_GRID_SIZE + parameterManager.Y_GRID_SIZE)) + 1;
					yCoord= parameterManager.Y_GRID_SIZE + 1;
				}
				else{
					xCoord= parameterManager.X_GRID_SIZE + 1;
					yCoord= (j - (parameterManager.X_GRID_SIZE * 2 + parameterManager.Y_GRID_SIZE)) + 1;
				}
			}
			
			output[xCoord][yCoord][subBlk1 ? 1 : 0]= b;
			b.setSubblk_1(subBlk1);
			b.setCoordinates(xCoord, yCoord);
			
			*/
			
			numberOfSlotsLeft--;
//			System.out.println("set initial coordinates: placed " + ((b instanceof IOBlock) ? "IOBlock" : "LogicBlock") + " [" + b.getName() + "] at (" + xCoord + "," + yCoord + ")" );
		}
		
		return output;
		
	}
	
	private static void placeSingleLogicBlock(LogicBlock b, int numberOfSlotsLeft, NetlistBlock[][][] output) {
		int index= rand.nextInt(numberOfSlotsLeft); //get random free slot
		for(int i= 0; i < index; i++) {
			if(output[biasX + (i / placingAreaSize)][biasY + (i % placingAreaSize)][0] != null) {
				index++; //skip slots already in use
			}
		}
		while(output[biasX + (index / placingAreaSize)][biasY + (index % placingAreaSize)][0] != null) {
			index++; //skip slots already in use
		}
		output[biasX + (index / placingAreaSize)][biasY + (index % placingAreaSize)][0]= b; 
		b.setCoordinates(biasX + (index / placingAreaSize), biasY + (index % placingAreaSize));
	}

	private static void placeSingleIOBlock(IOBlock b, int numberOfSlotsLeft, NetlistBlock[][][] output) {
		
		int index= rand.nextInt(numberOfSlotsLeft); //get random free slot and pad number
		
		//start at lower side of left IO
		boolean subBlk1= false;
		int xCoord= 0;
		int yCoord= 1;
		while(index > 0 || output[xCoord][yCoord][subBlk1 ? 1 : 0] != null){
			
			if(output[xCoord][yCoord][subBlk1 ? 1 : 0] == null) {
				index--; //count free slots since starting to walk
			}
			
			if(subBlk1) { //go to next io pad (next coordinate) (clockwise)
				
				subBlk1= false;			
	
				
				if(xCoord == 0) { //left IO
					yCoord++;
					if(yCoord == parameterManager.Y_GRID_SIZE + 1) { // reached top left corner
						xCoord= 1; //skip corner
					}
				}
				else if(yCoord == parameterManager.Y_GRID_SIZE + 1) { //top IO
					xCoord++;
					if(xCoord == parameterManager.X_GRID_SIZE + 1) { // reached top right corner
						yCoord= parameterManager.Y_GRID_SIZE; //skip corner
					}
				}
				else if(xCoord == parameterManager.X_GRID_SIZE + 1) { //right IO
					yCoord--;
					if(yCoord == 0) { // reached bottom right corner
						xCoord= parameterManager.X_GRID_SIZE; //skip corner
					}
				}
				else if(yCoord == 0) { //bottom IO
					xCoord--;
					if(xCoord == 0) { // reached top left corner
						yCoord= 1; //skip corner
					}
				}
				
			}
			else { //go to next io pad (same coordinates)
				
				subBlk1= true;
				
			}
			
		}
		
		output[xCoord][yCoord][subBlk1 ? 1 : 0]= b;
		b.setSubblk_1(subBlk1);
		b.setCoordinates(xCoord, yCoord);
	}


	private static void placeClockGeneratingBlocks() {
		int numberOfSlotsLeft;
		for(NetlistBlock b : clockGeneratingBlocks) {
			if(b instanceof LogicBlock) {
				numberOfSlotsLeft= placingAreaSize * placingAreaSize;
				numberOfSlotsLeft-= logicBlocks.length;
				
				placeSingleLogicBlock(((LogicBlock) b), numberOfSlotsLeft, sBlocks);
			}
			else {
				numberOfSlotsLeft= (parameterManager.X_GRID_SIZE * 2 + parameterManager.Y_GRID_SIZE * 2) * 2;
				numberOfSlotsLeft-= iOBlocks.length;
				
				placeSingleIOBlock(((IOBlock) b), numberOfSlotsLeft, sBlocks);
			}
		}
	}


	/**
	 * prints total wiring cost, acceptance rate, rLimit and rLimit logic block into files (as lines of 'x,y' pairs where x = datapoint, y= value
	 * @param placementFilePath 
	 */
	private static void printDiagnoseData(String newFilePath) {
		String filePath= newFilePath + ".costData";
		File outputFile= new File(filePath);
		try {
			BufferedWriter writer= new BufferedWriter(new FileWriter(outputFile));
			
			for(int i = 0; i < outputTotalCost.size()-1; i++) {
				writer.write(i + "," + outputTotalCost.get(i) + "\n");
			}
			
			writer.close();
		} catch (IOException e) {
			System.err.println("Error occured trying to create a new file at path '" + filePath + "'");
			e.printStackTrace();
		}
		
		filePath= newFilePath + ".rAData";
		outputFile= new File(filePath);
		try {
			BufferedWriter writer= new BufferedWriter(new FileWriter(outputFile));
			
			for(int i = 0; i < outputAcceptanceRate.size()-1; i++) {
				writer.write(i + "," + outputAcceptanceRate.get(i) + "\n");
			}
			
			writer.close();
		} catch (IOException e) {
			System.err.println("Error occured trying to create a new file at path '" + filePath + "'");
			e.printStackTrace();
		}
		
		filePath= newFilePath + ".rLimitData";
		outputFile= new File(filePath);
		try {
			BufferedWriter writer= new BufferedWriter(new FileWriter(outputFile));
			
			for(int i = 0; i < outputRLimit.size()-1; i++) {
				writer.write(i + "," + outputRLimit.get(i) + "\n");
			}
			
			writer.close();
		} catch (IOException e) {
			System.err.println("Error occured trying to create a new file at path '" + filePath + "'");
			e.printStackTrace();
		}
		
		filePath= newFilePath + ".rLimitLBData";
		outputFile= new File(filePath);
		try {
			BufferedWriter writer= new BufferedWriter(new FileWriter(outputFile));
			
			for(int i = 0; i < outputRLimitLogicBlock.size()-1; i++) {
				writer.write(i + "," + outputRLimitLogicBlock.get(i) + "\n");
			}
			
			writer.close();
		} catch (IOException e) {
			System.err.println("Error occured trying to create a new file at path '" + filePath + "'");
			e.printStackTrace();
		}
		
	}

	/**
	 * plots total wiring cost, acceptance rate, rLimit and rLimit logic block
	 */
	private static void plotDiagnoseData() {
		XYSeries totalCostSerie = new XYSeries("Total Cost");	
		XYSeries totalWiringCostSerie = new XYSeries("Total Wiring Cost");
		XYSeries totalTimingCostSerie = new XYSeries("Total Timing Cost");
		XYSeries acceptanceRateSerie = new XYSeries("Acceptance Rate");	
		XYSeries rLimitSerie = new XYSeries("R Limit");	
		XYSeries rLimitLogicBlocksSerie = new XYSeries("R Limit Logic Blocks");	 //TODO better fitting name
		
//		System.out.println("Number of data points: " + outputTotalCost.size());
		for(int i = 0; i < outputAcceptanceRate.size()-1; i++) {
			totalCostSerie.add(i, outputTotalCost.get(i));
			totalWiringCostSerie.add(i, outputTotalWiringCost.get(i));
			totalTimingCostSerie.add(i, outputTotalTimingCost.get(i));
			acceptanceRateSerie.add(i, outputAcceptanceRate.get(i));
			rLimitSerie.add(i, outputRLimit.get(i));
			rLimitLogicBlocksSerie.add(i, outputRLimitLogicBlock.get(i));
		}
		XYSeriesCollection datasetC = new XYSeriesCollection();
		XYSeriesCollection datasetWC = new XYSeriesCollection();
		XYSeriesCollection datasetTC = new XYSeriesCollection();
		XYSeriesCollection datasetAR = new XYSeriesCollection();
		XYSeriesCollection datasetRL = new XYSeriesCollection();
		XYSeriesCollection datasetRLL = new XYSeriesCollection();
		
		datasetC.addSeries(totalCostSerie);
		datasetWC.addSeries(totalWiringCostSerie);
		datasetTC.addSeries(totalTimingCostSerie);
		datasetAR.addSeries(acceptanceRateSerie);
		datasetRL.addSeries(rLimitSerie);
		datasetRLL.addSeries(rLimitLogicBlocksSerie);
		
		XYSplineRenderer spline= new XYSplineRenderer();
		XYLineAndShapeRenderer line= new XYLineAndShapeRenderer();
		XYSplineRenderer spline2= new XYSplineRenderer();
		XYSplineRenderer spline3= new XYSplineRenderer();
		
		
		NumberAxis xAchse = new NumberAxis("Steps");
		NumberAxis yCost = new NumberAxis("Cost");
		NumberAxis yWiringCost = new NumberAxis("Segments");
		NumberAxis yTimingCost = new NumberAxis("Weighted Sum of Delay over all Paths");
		NumberAxis yRL = new NumberAxis("Distance (1-Norm");
		NumberAxis yRLL = new NumberAxis("Distance (Infinity Norm)");
		NumberAxis yAR = new NumberAxis("Percent");
		
		XYPlot plotC = new XYPlot(datasetC, xAchse, yCost, spline);
		XYPlot plotWC = new XYPlot(datasetWC, xAchse, yWiringCost, spline);
		XYPlot plotTC = new XYPlot(datasetTC, xAchse, yTimingCost, spline);
		XYPlot plotAR = new XYPlot(datasetAR, xAchse, yAR, line);
		XYPlot plotRL = new XYPlot(datasetRL, xAchse, yRL, spline2);
		XYPlot plotRLL = new XYPlot(datasetRLL, xAchse, yRLL, spline3);
		
		JFreeChart chartC = new JFreeChart(plotC);
		JFreeChart chartWC = new JFreeChart(plotWC);
		JFreeChart chartTC = new JFreeChart(plotTC);
		JFreeChart chartAR = new JFreeChart(plotAR);
		JFreeChart chartRL = new JFreeChart(plotRL);
		JFreeChart chartRLL = new JFreeChart(plotRLL);
		
		ApplicationFrame frameC = new ApplicationFrame("Total Cost");
		ApplicationFrame frameWC = new ApplicationFrame("Wiring Cost");
		ApplicationFrame frameTC = new ApplicationFrame("Timing Cost");
		ApplicationFrame frameAR = new ApplicationFrame("Acceptance rate");
		ApplicationFrame frameRL = new ApplicationFrame("RLimit");
		ApplicationFrame frameRLL = new ApplicationFrame("RLimit Logic Blocks");
		
		ChartPanel chartPanelC = new ChartPanel(chartC);
		ChartPanel chartPanelWC = new ChartPanel(chartWC);
		ChartPanel chartPanelTC = new ChartPanel(chartTC);
		ChartPanel chartPanelAR = new ChartPanel(chartAR);
		ChartPanel chartPanelRL = new ChartPanel(chartRL);
		ChartPanel chartPanelRLL = new ChartPanel(chartRLL);
	
		frameC.setContentPane(chartPanelC);
		frameWC.setContentPane(chartPanelWC);
		frameTC.setContentPane(chartPanelTC);
		frameAR.setContentPane(chartPanelAR);
		frameRL.setContentPane(chartPanelRL);
		frameRLL.setContentPane(chartPanelRLL);
		
		frameC.pack();
		frameWC.pack();
		frameTC.pack();
		frameAR.pack();
		frameRL.pack();
		frameRLL.pack(); 
		
		frameC.setVisible(true);
		frameWC.setVisible(true);
		frameTC.setVisible(true);
		frameAR.setVisible(true);
		frameRL.setVisible(true);
		frameRLL.setVisible(true);

	}		
}
