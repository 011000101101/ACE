package router.structures.blockPinCost;

import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.LogicBlock;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

public abstract class BlockPinCost {
	
	/**
	 * block belonging to this object
	 */
	private NetlistBlock block;
	
	/**
	 * validity date for used count
	 */
	int usedCounterValidityDate= -1;
	
	/**
	 * validity date for used count
	 */
	int usedCounterValidityDate2= -1;

	/**
	 * check if limit is exceeded
	 * @param iterationCounter
	 * @param globalIterationCounter
	 * @return
	 */
	public abstract boolean limitExceeded(int iterationCounter, int globalIterationCounter);
	
	public BlockPinCost(NetlistBlock newBlock) {
		block= newBlock;
	}
	
	public int getX() {
		return block.getX();
	}

	public int getY() {
		return block.getY();
	}


	public NetlistBlock getBlock() {
		return block;
	}

	/**
	 * reset counters
	 */
	public abstract void resetCounters();

	public abstract String getUsedCounters(int globalIterationCounter);

	public int getUsedCounterValidityDate() {
		return usedCounterValidityDate;
	}


	public int getUsedCounterValidityDate2() {
		return usedCounterValidityDate2;
	}
}
