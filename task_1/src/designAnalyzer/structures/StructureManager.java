package designAnalyzer.structures;

import java.util.HashMap;

import designAnalyzer.ParameterManager;
import designAnalyzer.errorReporter.ErrorReporter;
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
	
	
	/**
	 * Singleton instance of StructureManager
	 */
	private static StructureManager instance= null;
	
	
	private StructureManager() {

		parameterManager= ParameterManager.getInstance();
		
		netMap= new HashMap<String, Net>();
		numberOfNets= 0;
		
		blockMap= new HashMap<String, NetlistBlock>();
		blockIndex= new NetlistBlock[parameterManager.getXGridSize() + 2][parameterManager.getYGridSize() + 2];
		
		
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
	 * inserts given block into the block indexing structure with coordinates as key if coordinates are valid and free <br>
	 * reports appropriate errors if coordinates of the block are not initialized, out of bounds or already occupied <br>
	 * @param xCoordinate X coordinate of the block (part of key)
	 * @param yCoordinate Y coordinate of the block (part of key)
	 * @param currentBlock the block to be inserted (value)
	 */
	public void insertIntoBlockIndexingStructure(int xCoordinate, int yCoordinate, NetlistBlock currentBlock) {

		if(xCoordinate == -1 || yCoordinate == -1) {
			ErrorReporter.reportBlockNotPlacedError(currentBlock);
		}
		else if(xCoordinate < 0 || xCoordinate > (parameterManager.getXGridSize() + 1) || yCoordinate < 0 || yCoordinate > (parameterManager.getYGridSize() + 1) ) {
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
	
	
}
