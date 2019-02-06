package router.structures.blockPinCost;

import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.LogicBlock;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

public abstract class BlockPinCost {
	
	private NetlistBlock block;

	public abstract boolean limitExceeded(int globalIterationCounter);
	
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

	public abstract void resetCounters();

	public abstract String getUsedCounters(int globalIterationCounter);
	
}
