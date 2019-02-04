package router.structures.blockPinCost;

import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

public abstract class BlockPinCost {
	
	private NetlistBlock block;

	public abstract boolean limitExceeded(int iterationCounter);
	
	public BlockPinCost(NetlistBlock newBlock) {
		block= newBlock;
	}
	
	public int getX() {
		return block.getX();
	}

	public int getY() {
		return block.getY();
	}

}
