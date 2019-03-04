package designAnalyzer.abstractedTimingGraph;

import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import designAnalyzer.timingAnalyzer.TimingAnalyzer;

public abstract class AbstractTerminal {

	protected NetlistBlock block;
	
	protected int tA;
	protected int tR;
	
	public AbstractTerminal(NetlistBlock newBlock) {
		block= newBlock;
	}
	protected abstract int annotataTA(AbstractTerminal specificSuccessor);
	protected abstract int annotataTRAndSlack(double critExp, int dMax);
	
	public int getX() {
		return block.getX();
	}
	
	public int getY() {
		return block.getY();
	}
	
	public NetlistBlock getBlock() {
		return block;
	}
	
	public abstract int getTAMinusDelay(AbstractTerminal specificSuccessor);
	
	public abstract void updateDelay(AbstractTerminal specificSuccessor);
	
	public abstract double computeWeightedSumOfDelays(double temperature, double exponentiatedCriticalityOfSuccessor, AbstractTerminal specificSuccessor);
	public abstract void addSuccessor(AbstractTerminal newSuccessor);
	public abstract void addPredecessor(AbstractTerminal newPredecessor);
}
