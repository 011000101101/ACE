package designAnalyzer.structures;

import java.util.HashMap;

import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.structures.blocks.NetlistBlock;
import designAnalyzer.structures.nets.Net;

public class StructureManager {


	
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
	
	/**
	 * counter keeping track of the number of the number of nets in existence <br>
	 * -used for setting unique id for each new net
	 */
	private int numberOfBlocks;
	
	private static StructureManager instance= null;
	
	
	private StructureManager() {

		netMap= new HashMap<String, Net>();
		numberOfNets= 0;
		
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
			numberOfBlocks++;
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


	// TODO check if correct style
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
	
	
}
