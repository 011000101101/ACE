package placer;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import designAnalyzer.ParameterManager;
import designAnalyzer.consistencyChecker.ConsistencyChecker;
import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.inputParser.ArchitectureParser;
import designAnalyzer.inputParser.NetlistParser;
import designAnalyzer.inputParser.PlacementParser;
import designAnalyzer.inputParser.RoutingParser;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.SimplePath;
import designAnalyzer.structures.StructureManager;
import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.LogicBlock;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import designAnalyzer.timingAnalyzer.TimingAnalyzer;
import placer.outputWriter.PlacementWriter;

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

	private static int biasX;

	private static int biasY;

	/**
	 * HashMap stores Ui and Vi values for each net
	 * value contains five values: Ui x, Ui y, Vi x, Vi y, wiring cost for this net
	 */
	private static HashMap<String, double[]> netWithValues;
	
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
	 * main method
	 * @param args
	 */
	public static void main(String[] args) {

		double lambda= 0.5; //TODO add optional command line passing of custom value 
		int stepCountFactor= 10; //TODO add optional command line passing of custom value 
		
		String netlistFilePath= args[0];
		String architectureFilePath= args[1];
		String placementFilePath= args[2];
		
		structureManager= StructureManager.getInstance();
		parameterManager= ParameterManager.getInstance();
		
		int[] commandLineInput = parseCommandlineArguments(args); //array in form from int to initialize architectureParser with
		long seed= commandLineInput[999]; //TODO insert correct index
		if(seed != 0) {
			rand= new Random(seed);
		}
		else {
			seed= new Random().nextLong(); //create random seed and store for debugging
			rand= new Random(seed);
		}
		
		
		
		
		
		try {
			architectureParser= new ArchitectureParser(architectureFilePath, commandLineInput);

			architectureParser.parseAll();
			ParameterManager.initialize(netlistFilePath, architectureFilePath, placementFilePath, architectureParser.getAllParameters());

			netlistParser= new NetlistParser(netlistFilePath);
			//TODO reactivate once implemented
			//placementWriter= new PlacementWriter();
			
			parse();

			timingAnalyzer= TimingAnalyzer.getInstance();
			
			place(stepCountFactor, lambda);
			
			
			String[] netlistFilePathSplit= netlistFilePath.split("/");
			String[] architectureFilePathSplit= architectureFilePath.split("/");
			placementWriter= new PlacementWriter(netlistFilePathSplit[netlistFilePathSplit.length - 1], architectureFilePathSplit[architectureFilePathSplit.length + 1]);
			placementWriter.write(/*filepath*/ null); //TODO insert output file path
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ErrorReporter.reportFileNotFoundError(e.toString());
		} catch (Exception e) {
			System.err.println("Execution aborted as a result of previously detected errors.");
		}
	}
	
	/**
	 * stores every argument beside args[0,1,2,3] (path arguments) for the initialization of the arch file in an array
	 * @param args 
	 * @return
	 */
	private static int[] parseCommandlineArguments(String[] args) {
		/*
		int[] parameterInitialized = new int[] {-1,-1,-1,-1,-1,-1,-1,-1,-1};
		for(int i = (routingFileProvided ? 4 : 3); i< args.length; i++) {
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
			
			}
		}
		return parameterInitialized;*/
		return null; //TODO check / adapt
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
			if(b instanceof IOBlock) {
				iOBlocksTemp.add((IOBlock) b);
			}
			else {
				logicBlocksTemp.add((LogicBlock) b);
			}
		}
		iOBlocks= iOBlocksTemp.toArray(iOBlocks);
		logicBlocks= logicBlocksTemp.toArray(logicBlocks);
		iOBlockCount= iOBlocks.length;
		blockCount= iOBlockCount + logicBlocks.length;
		
	}
	
	private static void place(int stepCountFactor, double newLambda) {
		
		netWithValues = new HashMap<String, double[]>();
		int stepCount= (int) Math.floor( stepCountFactor * Math.pow(blockCount, ( (double) 4 / (double) 3 ) ) );
		double avgCostPerNet= getAvgCostPerNet();
		double lambda= newLambda;
		
		NetlistBlock[][][] sBlocks = randomBlockPlacement() ; 
		int[] logicBlockSwap = null;
		double rA= 1;
		int acceptedTurns= 0;
		int rejectedTurns= 0;
		double rLimit = computeInitialrLimit() ; 
		double rLimitInitial= rLimit;
		double critExp = computeNewExponent(rLimit, rLimitInitial); 
		double temp = computeInitialTemperature(sBlocks, rLimit, critExp, lambda) ; 
		for(Net n : structureManager.getNetCollection()) { //generate all paths
			if(! n.getIsClocknNet()) paths.addAll(n.generateSimplePaths());
		}
		for(SimplePath p : paths) { 
			p.registerAtBlocks();
		}
		timingAnalyzer.initializeDelayLUT();
		
		while(temp > (0.005 * avgCostPerNet)) { 
			/* compute Ta, Tr and slack() */ 
			analyzeTiming() ; 
			
			/* für Normalisierung der Kostenterme */ 
			double oldWiringCost = wiringCost();
			double oldTimingCost = TimingCost(critExp) ; 
			for(int j = 0; j < stepCount; j++) { 
				
				double swapAnywaysFactor= -1; //TODO create random(0,1) and assign
				/*Snew = GenerateSwap(S, Rlimit) ; */
				if(rand.nextInt(blockCount) < iOBlockCount) { //swap IO blocks
					swapIOBlocks(sBlocks, rLimit, logicBlockSwap);
					double newTimingCost= newTimingCostSwap(critExp, logicBlockSwap); //only recompute changed values
					double deltaWiringCost = calcDeltaWiringCost(logicBlockSwap, sBlocks);//calculates delta wiring cost with hashmap and logicBlockSwap
					//TODO pls double check, newTimingCost - oldTimingCost?
					double deltaTimingCost = oldTimingCost - newTimingCost ; //TODO improve, cache valid old value, only compute change in logicBlocks, etc
					double newWiringCost = oldWiringCost + deltaWiringCost; 
					double deltaCost = lambda * (deltaTimingCost/oldTimingCost) + (1 - lambda) * (deltaWiringCost/oldWiringCost); 
					if (deltaCost <= 0) { 
						applySwap(sBlocks, logicBlockSwap);
						oldTimingCost= newTimingCost; //update buffer
						oldWiringCost= newWiringCost;
						acceptedTurns++;
					}
					else if(swapAnywaysFactor < (Math.exp((-1 * deltaCost / temp)))/*exp(-∆C/T)*/) { //TODO translate to java 
						applySwap(sBlocks, logicBlockSwap);
						oldTimingCost= newTimingCost; //update buffer
						oldWiringCost= newWiringCost;
						acceptedTurns++;
					}
					else rejectedTurns++;
				}
				else { //swap logic blocks
					swapLogicBlocks(sBlocks, rLimit, logicBlockSwap);
					double newTimingCost= newTimingCostSwap(critExp, logicBlockSwap); //only recompute changed values
					double newWiringCost= newWiringCostSwap(sBlocks, logicBlockSwap);
					double deltaTimingCost = oldTimingCost - newTimingCost ; //TODO improve, cache valid old value, only compute change in logicBlocks, etc
					double deltaWiringCost = oldWiringCost - newWiringCost ; 
					double deltaCost = lambda * (deltaTimingCost/oldTimingCost) + (1 - lambda) * (deltaWiringCost/oldWiringCost); 
					if (deltaCost <= 0) { 
						applySwap(sBlocks, logicBlockSwap);
						oldTimingCost= newTimingCost; //update buffer
						oldWiringCost= newWiringCost;
						acceptedTurns++;
					}
					else if(swapAnywaysFactor < (Math.exp((-1 * deltaCost / temp)))/*exp(-∆C/T)*/) { //TODO translate to java 
						applySwap(sBlocks, logicBlockSwap);
						oldTimingCost= newTimingCost; //update buffer
						oldWiringCost= newWiringCost;
						acceptedTurns++;
					}
					else rejectedTurns++;
				}

			}
			rA= acceptedTurns / (acceptedTurns + rejectedTurns); //compute new rA
			acceptedTurns= 0; //reset counters for rA computation
			rejectedTurns= 0;
			temp = UpdateTemp(temp, rA) ; 
			rLimit = UpdateRlimit(rLimit, rA) ; //TODO verify that "new" rA is used
			critExp = computeNewExponent(rLimit, rLimitInitial) ; 
		}
		
	}
	
	/**
	 * side effect: updates netWithValues accordingly
	 * @param logicBlockSwap
	 * @param sBlocks
	 * @return delta wiring cost before swap and after swap: oldCost minus newCost: for good swaps, the result is positiv
	 */
	private static double calcDeltaWiringCost(int[] logicBlockSwap, NetlistBlock[][][] sBlocks) {
		NetlistBlock block1= sBlocks[logicBlockSwap[0]][logicBlockSwap[1]][logicBlockSwap[2]];
		NetlistBlock block2= sBlocks[logicBlockSwap[3]][logicBlockSwap[4]][logicBlockSwap[5]];
		Net[] affectedNets = block1.getNet();
		Net[] affectedNets2 = block2.getNet();
		double returnVal = 0;
		for(Net netToAppend: affectedNets2) {
			if(	!Arrays.asList(affectedNets).contains(netToAppend)) {//TODO @Vincenz pls check if contains works for net entities
				Arrays.asList(affectedNets).add(netToAppend);
			}
		}
		for(Net net: affectedNets) { //vielleicht ganz am anfang netWithValues.get aufrufen, abespeichern: bessere Performanz?
			double deltaUix = Math.pow(block2.getX(), 2) - Math.pow(block1.getX(), 2);
			double deltaUiy = Math.pow(block2.getY(), 2) - Math.pow(block1.getY(), 2);
			double deltaVix = block2.getX() - block1.getX();
			double deltaViy = block2.getY() - block1.getY();
			double uix = netWithValues.get(net.getName())[0] + deltaUix;
			double uiy = netWithValues.get(net.getName())[1] + deltaUiy;
			double vix = netWithValues.get(net.getName())[2] + deltaVix;
			double viy = netWithValues.get(net.getName())[3] + deltaViy;
			double wiringCostX = GAMMA * Math.sqrt(uix - Math.pow(vix, 2)/netWithValues.get(net.getName())[4] + PHI);
			double wiringCostY = GAMMA * Math.sqrt(uiy - Math.pow(viy, 2)/netWithValues.get(net.getName())[4] + PHI);
			double wiringCost = wiringCostX + wiringCostY;
			double[] newArray = {uix, uiy, vix, viy, wiringCost, netWithValues.get(net.getName())[4]};
			returnVal += wiringCost - netWithValues.get(net.getName())[4];
			netWithValues.put(net.getName(), newArray);
			
		}
		
		return returnVal;
	}


	private static double newWiringCostSwap(NetlistBlock[][][] sBlocks, int[] logicBlockSwap) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static double newTimingCostSwap(double ce, int[] logicBlockSwap) {
		double sum= 0;
		for(SimplePath p : paths) {
			sum += p.timingCostSwap(ce, logicBlockSwap);
		}
		return sum;
	}

	/**
	 * computes the coordinates of a random logic block and it's random swapping partner (empty slot or other logic block)
	 * @param sBlocks current placement of blocks
	 * @param rLimit distance limit for swap
	 * @param blockSwap array for storing the computed values: {block1_x, block1_y, block1_subblk (0 for logic blocks), block2_x, block2_y, block2_subblk (0 for logic blocks)}
	 */
	private static void swapLogicBlocks(NetlistBlock[][][] sBlocks, double rLimit, int[] blockSwap) {
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
		int index= rand.nextInt(iOBlocks.length); //get a random logic block for swap (swapping two empty places wouldn't gain anything)
		LogicBlock block1= logicBlocks[index];
		
		int top= (int) Math.floor(rLimit); //compute boundaries to swappable area (limited by boundaries of placable area and by rLimit, checking rLimit with infinity norm for good performance while retaining equally distributed probability)
		if(block1.getY() - top < biasY) top = block1.getY() - biasY;
		int bottom= (int) Math.floor(rLimit);
		if(block1.getY() + bottom > biasY + (placingAreaSize - 1)) bottom = biasY + (placingAreaSize - 1) - block1.getY();
		int left= (int) Math.floor(rLimit);
		if(block1.getX() - left < biasX) left = block1.getX() - biasX;
		int right= (int) Math.floor(rLimit);
		if(block1.getX() + right > biasX + (placingAreaSize - 1)) right = biasX + (placingAreaSize - 1) - block1.getX();
		
		int x= rand.nextInt(left + right + 1); //coordinates of block2 relative to block1
		x -= left;
		int y= rand.nextInt(top + bottom + 1);
		y-= top; 
		
		while(x == 0 && y == 0) { //avoid swapping a block with itself
			x= rand.nextInt(left + right + 1);
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
	private static void swapIOBlocks(NetlistBlock[][][] sBlocks, double rLimit, int[] blockSwap) {
		int index= rand.nextInt(iOBlocks.length); //get a random IO block for swap (swapping two empty places wouldn't gain anything)
		IOBlock block1= iOBlocks[index];
		int swapDistance= rand.nextInt((int) Math.floor(rLimit)) + 1; //get distance of swap (not 0, as swapping pad 0 with pad 1 at same coordinates wouldn't change cost)
		boolean ccw= rand.nextBoolean(); //get swap direction (clockwise or counterclockwise)
		boolean subblk_1= rand.nextBoolean(); //get pad number of swap partner
		int xCoord= block1.getX(); //get starting point position
		int yCoord= block1.getY();
		while(swapDistance > 0) { //walk around the rim until the target position is reached
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
			if(yCoord == parameterManager.Y_GRID_SIZE + 1) { //bottom IO
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
			if(xCoord == parameterManager.X_GRID_SIZE + 1) { //right IO
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
			if(xCoord == parameterManager.X_GRID_SIZE + 1) { //left IO
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
	private static void applySwap(NetlistBlock[][][] sBlocks, int[] logicBlockSwap) {
		NetlistBlock block1= sBlocks[logicBlockSwap[0]][logicBlockSwap[1]][logicBlockSwap[2]];
		NetlistBlock block2= sBlocks[logicBlockSwap[3]][logicBlockSwap[4]][logicBlockSwap[5]];
		sBlocks[logicBlockSwap[0]][logicBlockSwap[1]][logicBlockSwap[2]]= block2;
		sBlocks[logicBlockSwap[3]][logicBlockSwap[4]][logicBlockSwap[5]]= block1;
		block2.setCoordinates(logicBlockSwap[0], logicBlockSwap[1]); //set new coordinates
		block1.setCoordinates(logicBlockSwap[3], logicBlockSwap[4]);
		if(block1 instanceof IOBlock) { //set subblk number
			block1.setSubblk_1(logicBlockSwap[5] == 1);
			block1.setSubblk_1(logicBlockSwap[2] == 1);
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
		return rLimitOld * ( ( 1 + rAOld) - 0.44 );
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

	private static double TimingCost(double ce) {
		double sum= 0;
		for(SimplePath p : paths) {
			sum += p.timingCost(ce);
		}
		return sum;
	}

	private static double getAvgCostPerNet() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * compute new criticalityExponent
	 * @param rLimit current rLimit
	 * @param rLimitInitial initial rLimit
	 * @return the new critExp
	 */
	private static double computeNewExponent(double rLimit, double rLimitInitial) {
		return ( ( 1 - ( ( rLimit - 1 ) / ( rLimitInitial - 1 ) ) ) * 7 ) + 1;
	}

	/**
	 * computes the initial maximum range for swaps
	 * @return the initial rLimit
	 */
	private static double computeInitialrLimit() {
		return parameterManager.X_GRID_SIZE + parameterManager.Y_GRID_SIZE;
	}

	/**
	 * computes the initial temperature according to the formula on the slides
	 * @param sBlocks initial random placement (will be changed, but still be random, therefore no need to save old placement)
	 * @param rLimit maximum range for swaps
	 * @param critExp criticality exponent
	 * @param lambda lambda for weighting wiring and timing cost
	 * @return initial temperature value
	 */
	private static double computeInitialTemperature(NetlistBlock[][][] sBlocks, double rLimit, double critExp, double lambda) {
		
		double n= blockCount;
		double cQuer= 0;
		int sumCSquare= 0;
		for(int i= 1; i <= n; i++) {
			double cI= applySwapAndGetC(sBlocks, rLimit, critExp, lambda);
			sumCSquare+= cI * cI;
			cQuer+= cI / n;
		}
		return 20 * Math.sqrt( ( (double) 1 / ( n - (double) 1) ) * ( sumCSquare - ( n * ( cQuer * cQuer ) ) ) ); 
	
	}

	/**
	 * computes and applies a random swap, then computes the new value of the cost function and returns it
	 * @param sBlocks current placement
	 * @param rLimit maximum range for swaps
	 * @param critExp criticality exponent
	 * @param lambda lambda for weighting wiring and timing cost
	 * @return cost after the swap
	 */
	private static double applySwapAndGetC(NetlistBlock[][][] sBlocks, double rLimit, double critExp, double lambda) {

		int[] blockSwap= new int[6];
		if(rand.nextInt(blockCount) < iOBlockCount) { //swap IO blocks
			swapIOBlocks(sBlocks, rLimit, blockSwap);
		}
		else {
			swapLogicBlocks(sBlocks, rLimit, blockSwap);
		}
		
		applySwap(sBlocks, blockSwap);
		
		double newTimingCost= newTimingCostSwap(critExp, blockSwap); //only recompute changed values
		double newWiringCost= newWiringCostSwap(sBlocks, blockSwap);
		
		return lambda * newTimingCost + (1 - lambda) * newWiringCost;  //TODO verify cost function
		
	}

	/**
	 * generates records in Hashmap netWithValues where values as Uix, Uiy, Vix, Viy and estimated Cost are listed
	 * @return wiringCosts of each net added together
	 */
	private static double wiringCost() {
		Collection<Net> allNets = structureManager.getNetCollection();
		//saves blocks of currentNet
		NetlistBlock[] currentBlocks;
		double returnVal = 0 ;
		for(Net currentNet: allNets) {
			if(!currentNet.getIsClocknNet()) {
				currentBlocks = currentNet.getBlocks(); //TODO check if getBlocks is implemented
				int uix = 0; //TODO verify initialization with 0
				int uiy = 0;
				int vix = 0;
				int viy = 0;
				for(NetlistBlock currentSingleBlock: currentBlocks) {
					uix += Math.pow(currentSingleBlock.getX(),2);
					uiy += Math.pow(currentSingleBlock.getY(),2);
					vix += currentSingleBlock.getX();
					viy += currentSingleBlock.getY();
				}
				double wiringCostX = GAMMA * Math.sqrt(uix - Math.pow(vix, 2)/currentBlocks.length + PHI);
				double wiringCostY = GAMMA * Math.sqrt(uiy - Math.pow(viy, 2)/currentBlocks.length + PHI);
				double wiringCost = wiringCostX + wiringCostY;
				double[] valueArray = {uix, uiy, vix, viy, wiringCost, currentBlocks.length};
				returnVal += wiringCost;
				netWithValues.put(currentNet.getName(), valueArray);
			}
		}
		return returnVal;
	}

	/**
	 * creates a random initial placement for all IOBlocks
	 * @return the new random placement as an array of possible placements, containing the IOBlocks at positions corresponding to their placement
	 */
	private static IOBlock[] randomIOBlockPlacement() {
		
		int numberOfSlotsLeft= parameterManager.X_GRID_SIZE * 2 + parameterManager.Y_GRID_SIZE * 2;
		IOBlock[] output= new IOBlock[numberOfSlotsLeft];
		for(IOBlock b : iOBlocks) {
			int index= rand.nextInt(numberOfSlotsLeft * 2); //get random free slot and pad number
			boolean subBlk1= false;
			if(index >= numberOfSlotsLeft) { //extract subblock number information from generated random number
				index = index - numberOfSlotsLeft;
				subBlk1= true;
			}
			for(int i= 0; i < index; i++) {
				if(output[i] != null) {
					index++; //skip slots already in use
				}
			}
			output[index]= b;
			setIOBlockCoordinates(b, index, subBlk1);
		}
		return output;
				
	}

	/**
	 * sets the new coordinates of an IOBlock, given its new index in sIOBlock
	 * @param b the IOBlock
	 * @param index its new index in sIOBlock
	 * @param subBlk1 
	 */
	private static void setIOBlockCoordinates(IOBlock b, int index, boolean subBlk1) {

		if(index < parameterManager.X_GRID_SIZE) {
			b.setCoordinates(index + 1, 0);
		}
		else if(index < parameterManager.X_GRID_SIZE + parameterManager.Y_GRID_SIZE) {
			b.setCoordinates(0, (index - parameterManager.X_GRID_SIZE) + 1);
		}
		else if(index < parameterManager.X_GRID_SIZE * 2 + parameterManager.Y_GRID_SIZE) {
			b.setCoordinates((index - (parameterManager.X_GRID_SIZE + parameterManager.Y_GRID_SIZE)) + 1, parameterManager.Y_GRID_SIZE + 1);
		}
		else{
			b.setCoordinates(parameterManager.X_GRID_SIZE + 1, (index - (parameterManager.X_GRID_SIZE * 2 + parameterManager.Y_GRID_SIZE)) + 1);
		}
		b.setSubblk_1(subBlk1);
		
	}

	/**
	 * creates a random initial placement for all LogicBlocks
	 * @return the new random placement as a matrix of all possible block coordinates, containing the blocks in the centered square area of minimum size as required by the task description
	 */
	private static NetlistBlock[][][] randomBlockPlacement() {
		
		int numberOfSlotsLeft= placingAreaSize * placingAreaSize;
		biasX= (parameterManager.X_GRID_SIZE - placingAreaSize) / 2 + 1;
		biasY= (parameterManager.Y_GRID_SIZE - placingAreaSize) / 2 + 1;
		NetlistBlock[][][] output= new NetlistBlock[parameterManager.X_GRID_SIZE + 2][parameterManager.Y_GRID_SIZE + 2][2];
		for(LogicBlock b : logicBlocks) {
			int index= rand.nextInt(numberOfSlotsLeft); //get random free slot
			for(int i= 0; i < index; i++) {
				if(output[biasX + (index / placingAreaSize)][biasY + (index % placingAreaSize)][0] != null) {
					index++; //skip slots already in use
				}
			}
			output[biasX + (index / placingAreaSize)][biasY + (index % placingAreaSize)][0]= b; //TODO verify
			b.setCoordinates(biasX + (index / placingAreaSize), biasY + (index % placingAreaSize));
		}
		
		
		numberOfSlotsLeft= parameterManager.X_GRID_SIZE * 2 + parameterManager.Y_GRID_SIZE * 2;
		for(IOBlock b : iOBlocks) {
			int index= rand.nextInt(numberOfSlotsLeft * 2); //get random free slot and pad number
			boolean subBlk1= false;
			if(index >= numberOfSlotsLeft) { //extract subblock number information from generated random number
				index = index - numberOfSlotsLeft;
				subBlk1= true;
			}
			int xCoord= 0;
			int yCoord= 0;
			if(index < parameterManager.X_GRID_SIZE) {
				xCoord= index + 1;
				yCoord= 0;
			}
			else if(index < parameterManager.X_GRID_SIZE + parameterManager.Y_GRID_SIZE) {
				xCoord= 0;
				yCoord= (index - parameterManager.X_GRID_SIZE) + 1;
			}
			else if(index < parameterManager.X_GRID_SIZE * 2 + parameterManager.Y_GRID_SIZE) {
				xCoord= (index - (parameterManager.X_GRID_SIZE + parameterManager.Y_GRID_SIZE)) + 1;
				yCoord= parameterManager.Y_GRID_SIZE + 1;
			}
			else{
				xCoord= parameterManager.X_GRID_SIZE + 1;
				yCoord= (index - (parameterManager.X_GRID_SIZE * 2 + parameterManager.Y_GRID_SIZE)) + 1;
			}
			for(int i= 0; i < index; i++) {
				if(output[xCoord][yCoord][subBlk1 ? 1 : 0] != null) {
					index++; //skip slots already in use
				}
			}
			output[xCoord][yCoord][subBlk1 ? 1 : 0]= b;
			b.setCoordinates(xCoord, yCoord);
			b.setSubblk_1(subBlk1);
		}
		
		return output;
		
	}
}
