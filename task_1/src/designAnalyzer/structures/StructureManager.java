package designAnalyzer.structures;

import java.util.Collection;
import java.util.HashMap;

import designAnalyzer.ParameterManager;
import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.inputParser.AbstractInputParser;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

public class StructureManager {

	
	/**
	 * Singleton instance of ParameterManager
	 */
	ParameterManager parameterManager;

	
	/**
	 * HashMap containing all Nets, with name as key
	 */
	private HashMap<String, Net> netMap;
	
	/**
	 * counter keeping track of the number of the number of nets in existence <br>
	 * -used for setting unique id for each new net
	 */
	private int numberOfNets;
	
	/**
	 * HashMap containing all Blocks, with name as key
	 */
	private HashMap<String, NetlistBlock> blockMap;
	
	private NetlistBlock[][] blockIndex;
	
	private boolean[][][] chanXUsed= new boolean[parameterManager.X_GRID_SIZE][parameterManager.Y_GRID_SIZE + 1][parameterManager.CHANNEL_WIDTH];
	private boolean[][][] chanYUsed= new boolean[parameterManager.X_GRID_SIZE + 1][parameterManager.Y_GRID_SIZE][parameterManager.CHANNEL_WIDTH];
	
	
	/**
	 * Singleton instance of StructureManager
	 */
	private static StructureManager instance= null;
	
	
	private StructureManager() {

		parameterManager= ParameterManager.getInstance();
		
		netMap= new HashMap<String, Net>();
		numberOfNets= 0;
		
		blockMap= new HashMap<String, NetlistBlock>();
		blockIndex= new NetlistBlock[parameterManager.X_GRID_SIZE + 2][parameterManager.Y_GRID_SIZE + 2];
		
		
	}


	/**
	 * statically returns a single instance of this class
	 * @return the single StructureManager instance
	 */
	public static StructureManager getInstance() {
		if(instance == null) {
			instance= new StructureManager();
		}
		return instance;
	}
	

	
	/**
	 * -warning: not a standard getter- <br>
	 * get Net by name reference if it already exists, else create new Net, insert into netMap and return it
	 * @param name name of the Net to get the instance of
	 * @return instance of Net belonging to the given name
	 */
	public Net retrieveNet(String name, boolean isClockNet) {
		if(netMap.containsKey(name)) {
			Net temp= netMap.get(name);
			if(isClockNet != temp.getIsClocknNet()) {
				ErrorReporter.reportClockNetConnectionError(temp, isClockNet);
			}
			return temp;
		}
		else {
			numberOfNets++;
			Net newNet= new Net(name, numberOfNets, isClockNet);
			netMap.put(name,  newNet);
			return newNet;
		}
	}


	/**
	 * inserts the given block into the block HashMap with name as key, <br>
	 * report an error if a block with the same name already exists
	 * @param currentBlock the block to be inserted
	 */
	public void insertBlock(NetlistBlock currentBlock) {

		if(blockMap.containsKey(currentBlock.getName())) {
			ErrorReporter.reportDuplicateBlockError(currentBlock);
		}
		else {
			blockMap.put(currentBlock.getName(),  currentBlock);
		}
		
	}
	
	/**
	 * standard getter
	 * @return the HashMap containing all blocks with names as keys
	 */
	public HashMap<String, NetlistBlock> getBlockMap(){
		return blockMap;
	}

	/**
	 * 
	 */
	public NetlistBlock retrieveBlockByName(String name) {
		if(blockMap.containsKey(name)) {
			return blockMap.get(name);
		} else {
			return null;
		}
	}

	/**
	 * inserts given block into the block indexing structure with coordinates as key if coordinates are valid and free <br>
	 * reports appropriate errors if coordinates of the block are not initialized, out of bounds or already occupied <br>
	 * @param xCoordinate X coordinate of the block (part of key)
	 * @param yCoordinate Y coordinate of the block (part of key)
	 * @param currentBlock the block to be inserted (value)
	 */
	public void insertIntoBlockIndexingStructure(int xCoordinate, int yCoordinate, NetlistBlock currentBlock) {

		if(xCoordinate < 0 || xCoordinate > (parameterManager.X_GRID_SIZE + 1) || yCoordinate < 0 || yCoordinate > (parameterManager.Y_GRID_SIZE + 1) ) {
			ErrorReporter.reportBlockPlacedOutOfBoundsError(currentBlock);
		}
		if(blockIndex[xCoordinate][yCoordinate] != null) {
			ErrorReporter.reportDuplicateBlockPlacementError(currentBlock);
		}
		else {
			blockIndex[xCoordinate][yCoordinate]= currentBlock;
		}
			
	}


	public NetlistBlock retrieveBlockByCoordinates(int xCoordinate, int yCoordinate) {
		return blockIndex[xCoordinate][yCoordinate];
	}


	/**
	 * retrieves a net from the net map, returning null if no net with the given name exists
	 * @param name name of the net to retrieve
	 * @return the net that was requested, or null if no such net exists
	 */
	public Net retrieveNetNoInsert(String name) {
		if(netMap.containsKey(name)) {
			return netMap.get(name);
		}
		else{
			return null;
		}
	}
	
	public Collection<Net> getNetCollection(){
		return netMap.values();
	}


	/**
	 * checks if a certain channel track is available for use<br>
	 * if so, marks it as used<br>
	 * if not, reports appropriate error
	 * @param xCoordinate X coordinate of the channel
	 * @param yCoordinate Y coordinate of the channel
	 * @param trackNum track number of the track to be used
	 * @param isChanX if channel is chanX or chanY
	 * @param parser parser instance for reference in error case
	 */
	public void useChan(int xCoordinate, int yCoordinate, int trackNum, boolean isChanX, AbstractInputParser parser) {

		if(trackNum > parameterManager.CHANNEL_WIDTH - 1) {
			ErrorReporter.reportParameterOutOfBoundsError(parameterManager.CHANNEL_WIDTH - 1, trackNum, "track number of channel - upper boundary", parser);
		}
		if(isChanX) {
			
			if(xCoordinate < 1 ) {
				ErrorReporter.reportParameterOutOfBoundsError(parameterManager.X_GRID_SIZE , xCoordinate, "X coordinate of ChanX - lower boundary", parser);
			}
			else if( xCoordinate > parameterManager.X_GRID_SIZE) {
				ErrorReporter.reportParameterOutOfBoundsError(parameterManager.X_GRID_SIZE , xCoordinate, "X coordinate of ChanX - upper boundary", parser);
			}
			else if(yCoordinate < 0  ) {
				ErrorReporter.reportParameterOutOfBoundsError(parameterManager.Y_GRID_SIZE , yCoordinate, "Y coordinate of ChanX - lower boundary", parser);
			}
			else if( yCoordinate > parameterManager.Y_GRID_SIZE) {
				ErrorReporter.reportParameterOutOfBoundsError(parameterManager.Y_GRID_SIZE , yCoordinate, "Y coordinate of ChanX - upper boundary", parser);
			}
			else if(chanXUsed[xCoordinate - 1][yCoordinate][trackNum]) {
				ErrorReporter.reportDuplicateChannelUsageError(xCoordinate, yCoordinate, trackNum, isChanX, parser);
			}
			else{
				chanXUsed[xCoordinate - 1][yCoordinate][trackNum]= true; //mark as used
			}
			
		}
		else {
			
			if(xCoordinate < 0 ) {
				ErrorReporter.reportParameterOutOfBoundsError(parameterManager.X_GRID_SIZE , xCoordinate, "X coordinate of ChanY - lower boundary", parser);
			}
			else if(xCoordinate > parameterManager.X_GRID_SIZE) {
				ErrorReporter.reportParameterOutOfBoundsError(parameterManager.X_GRID_SIZE , xCoordinate, "X coordinate of ChanY - upper boundary", parser);
			}
			else if(yCoordinate < 1) {
				ErrorReporter.reportParameterOutOfBoundsError(1 , yCoordinate, "Y coordinate of ChanY - lower boundary", parser);
			}
			else if(yCoordinate > parameterManager.Y_GRID_SIZE ) {
				ErrorReporter.reportParameterOutOfBoundsError(parameterManager.Y_GRID_SIZE , yCoordinate, "Y coordinate of ChanY - upper boundary", parser);
			}
			else if(chanYUsed[xCoordinate - 1][yCoordinate][trackNum]) {
				ErrorReporter.reportDuplicateChannelUsageError(xCoordinate, yCoordinate, trackNum, isChanX, parser);
			}
			else{
				chanYUsed[xCoordinate][yCoordinate - 1][trackNum]= true; //mark as used
			}
			
		}
		
	}
	
	
}
