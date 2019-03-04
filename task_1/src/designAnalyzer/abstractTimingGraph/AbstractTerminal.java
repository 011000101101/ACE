package designAnalyzer.abstractTimingGraph;

import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

public abstract class AbstractTerminal {

	protected NetlistBlock block;
	
	protected double tA;
	protected double tR;
	
	public AbstractTerminal(NetlistBlock newBlock) {
		block= newBlock;
	}
	protected abstract double annotataTA(AbstractTerminal specificSuccessor);
	protected abstract double annotataTRAndSlack(int dMax);
	protected abstract void resetTA();
	protected void resetTRAndSlack() {
		tR= Integer.MAX_VALUE;
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
	
}
