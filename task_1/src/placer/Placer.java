package placer;

import java.io.FileNotFoundException;
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
import designAnalyzer.structures.StructureManager;
import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.LogicBlock;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import designAnalyzer.timingAnalyzer.TimingAnalyzer;

public class Placer {
	
	/**
	 * reference to instance of input parser
	 */
	private static NetlistParser netlistParser;
	
	/**
	 * reference to instance of input parser
	 */
	private static ArchitectureParser architectureParser;
	
	//TODO implement placement writer
	/**
	 * reference to instance of placement writer
	 */
	//private static PlacementWriter placementWriter;
	
	
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
	
	private static Random rand;

	private static int placingAreaSize;
	
	private static IOBlock[] iOBlocks;
	
	private static LogicBlock[] logicBlocks;

	private static int iOBlockCount;

	private static int blockCount;
	
	
	/**
	 * main method
	 * @param args
	 */
	public static void main(String[] args) {

		
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

			timingAnalyzer= new TimingAnalyzer();
			
			place();
			
			//TODO reactivate once implemented
			//placementWriter.write(/*filepath*/);
			
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
	
	private static void place() {
		
		int stepCount= getStepCount();
		double avgCostPerNet= getAvgCostPerNet();
		double lambda= getLambda();
		
		LogicBlock[][] sLogicBlock = randomLogicBlockPlacement() ; 
		int[] logicBlockSwap = null;
		IOBlock[] sIOBlock = randomIOBlockPlacement() ; 
		int[] iOBlockSwap = null;
		double temp = computeInitialTemperature() ; 
		double rLimit = computeInitialrLimit() ; 
		double CritExp = computeNewExponent(rLimit); 
		while(temp > (0.005 * avgCostPerNet)) { 
			/* compute Ta, Tr and slack() */ 
			analyzeTiming() ; 
			/* für Normalisierung der Kostenterme */ 
			double oldWiringCost = WiringCost(sLogicBlock, sIOBlock) ; //TODO investigate
			double oldTimingCost = TimingCost(sLogicBlock, sIOBlock) ; 
			for(int j = 0; j < stepCount; j++) { 
				
				double swapAnywaysFactor= -1; //TODO create random(0,1) and assign
				/*Snew = GenerateSwap(S, Rlimit) ; */
				if(rand.nextInt(blockCount) < iOBlockCount) { //swap IO blocks
					swapIOBlocks(sIOBlock, rLimit, iOBlockSwap);
					double newTimingCost= newTimingCostIOSwap(sLogicBlock, sIOBlock, iOBlockSwap); //only recompute changed values
					double newWiringCost= newWiringCostIOSwap(sLogicBlock, sIOBlock, iOBlockSwap);
					double deltaTimingCost = oldTimingCost - newTimingCost ; //TODO improve, cache valid old value, only compute change in logicBlocks, etc
					double deltaWiringCost = oldWiringCost - newWiringCost ; 
					double deltaCost = lambda * (deltaTimingCost/oldTimingCost) + (1 - lambda) * (deltaWiringCost/oldWiringCost); 
					if (deltaCost <= 0) { 
						applySwapIO(sIOBlock, iOBlockSwap);
						oldTimingCost= newTimingCost; //update buffer
						oldWiringCost= newWiringCost;
					}
					else if(swapAnywaysFactor < (Math.exp((-1 * deltaCost / temp)))/*exp(-∆C/T)*/) { //TODO translate to java 
						applySwapIO(sIOBlock, iOBlockSwap);
						oldTimingCost= newTimingCost; //update buffer
						oldWiringCost= newWiringCost;
					}
				}
				else { //swap logic blocks
					swapLogicBlocks(sLogicBlock, rLimit, logicBlockSwap);
					double newTimingCost= newTimingCostLogicSwap(sLogicBlock, sIOBlock, logicBlockSwap); //only recompute changed values
					double newWiringCost= newWiringCostLogicSwap(sLogicBlock, sIOBlock, logicBlockSwap);
					double deltaTimingCost = oldTimingCost - newTimingCost ; //TODO improve, cache valid old value, only compute change in logicBlocks, etc
					double deltaWiringCost = oldWiringCost - newWiringCost ; 
					double deltaCost = lambda * (deltaTimingCost/oldTimingCost) + (1 - lambda) * (deltaWiringCost/oldWiringCost); 
					if (deltaCost <= 0) { 
						applySwapLogic(sLogicBlock, logicBlockSwap);
						oldTimingCost= newTimingCost; //update buffer
						oldWiringCost= newWiringCost;
					}
					else if(swapAnywaysFactor < (Math.exp((-1 * deltaCost / temp)))/*exp(-∆C/T)*/) { //TODO translate to java 
						applySwapLogic(sLogicBlock, logicBlockSwap);
						oldTimingCost= newTimingCost; //update buffer
						oldWiringCost= newWiringCost;
					}
				}

			}
			temp = UpdateTemp() ; 
			rLimit = UpdateRlimit() ; 
			CritExp = computeNewExponent(rLimit) ; 
		}
		
	}

	private static double newWiringCostLogicSwap(LogicBlock[][] sLogicBlock, IOBlock[] sIOBlock, int[] logicBlockSwap) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static double newWiringCostIOSwap(LogicBlock[][] sLogicBlock, IOBlock[] sIOBlock, int[] iOBlockSwap) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static double newTimingCostLogicSwap(LogicBlock[][] sLogicBlock, IOBlock[] sIOBlock, int[] logicBlockSwap) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static double newTimingCostIOSwap(LogicBlock[][] sLogicBlock, IOBlock[] sIOBlock, int[] iOBlockSwap) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static void swapLogicBlocks(LogicBlock[][] sLogicBlock, double rLimit, int[] logicBlockSwap) {
		// TODO Auto-generated method stub
		
	}

	private static void swapIOBlocks(IOBlock[] sIOBlock, double rLimit, int[] iOBlockSwap) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * applies an IO block swap by swapping the entries at the given coordinates
	 * @param sIOBlock IO block placement
	 * @param iOBlockSwap coordinates of the slots to be swapped: {index_block1, index_block2}
	 */
	private static void applySwapIO(IOBlock[] sIOBlock, int[] iOBlockSwap) {
		IOBlock buffer= sIOBlock[iOBlockSwap[0]];
		sIOBlock[iOBlockSwap[0]]= sIOBlock[iOBlockSwap[1]];
		sIOBlock[iOBlockSwap[1]]= buffer;
	}

	/**
	 * applies a logic block swap by swapping the entries at the given coordinates
	 * @param sLogicBlock logic block placement
	 * @param logicBlockSwap coordinates of the slots to be swapped: {block1_x, block1_y, block2_x, block2_y}
	 */
	private static void applySwapLogic(LogicBlock[][] sLogicBlock, int[] logicBlockSwap) {
		LogicBlock buffer= sLogicBlock[logicBlockSwap[0]][logicBlockSwap[1]];
		sLogicBlock[logicBlockSwap[0]][logicBlockSwap[1]]= sLogicBlock[logicBlockSwap[2]][logicBlockSwap[3]];
		sLogicBlock[logicBlockSwap[2]][logicBlockSwap[3]]= buffer;
	}

	private static double getLambda() {
		// TODO Auto-generated method stub
		return 0;
	}

	private static void analyzeTiming() {
		// TODO Auto-generated method stub
		
	}

	private static double UpdateRlimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	private static double UpdateTemp() {
		// TODO Auto-generated method stub
		return 0;
	}

	private static double TimingCost(NetlistBlock[][] sLogicBlock, IOBlock[] sIOBlock) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static double WiringCost(NetlistBlock[][] sLogicBlock, IOBlock[] sIOBlock) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static double getAvgCostPerNet() {
		// TODO Auto-generated method stub
		return 0;
	}

	private static int getStepCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	private static double estimateTiming() {
		// TODO Auto-generated method stub
		return 0;
	}

	private static double computeNewExponent(double rLimit) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static double computeInitialrLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	private static double computeInitialTemperature() {
		// TODO Auto-generated method stub
		return 0;
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
	private static LogicBlock[][] randomLogicBlockPlacement() {
		
		int numberOfSlotsLeft= placingAreaSize * placingAreaSize;
		int biasX= (parameterManager.X_GRID_SIZE - placingAreaSize) / 2;
		int biasY= (parameterManager.Y_GRID_SIZE - placingAreaSize) / 2;
		LogicBlock[][] output= new LogicBlock[parameterManager.X_GRID_SIZE][parameterManager.Y_GRID_SIZE];
		for(LogicBlock b : logicBlocks) {
			int index= rand.nextInt(numberOfSlotsLeft); //get random free slot
			for(int i= 0; i < index; i++) {
				if(output[biasX + (index / placingAreaSize)][biasY + (index % placingAreaSize)] != null) {
					index++; //skip slots already in use
				}
			}
			output[biasX + (index / placingAreaSize)][biasY + (index % placingAreaSize)]= b; //TODO verify
			b.setCoordinates(biasX + (index / placingAreaSize), biasY + (index % placingAreaSize));
		}
		return output;
		
	}
}
