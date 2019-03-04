package designAnalyzer.abstractedTimingGraph;

import java.util.List;

import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

public abstract class AbstractTerminal {

	protected NetlistBlock block;
	
	protected int tA;
	protected int tR;
	
	protected int xCoordBuffer;
	protected int yCoordBuffer;
	
	public AbstractTerminal(NetlistBlock newBlock) {
		block= newBlock;
		xCoordBuffer= block.getX();
		yCoordBuffer= block.getY();
		tA= -1;
		tR= Integer.MAX_VALUE;
	}
	protected abstract int annotataTA(AbstractTerminal specificSuccessor);
	protected abstract int annotataTRAndSlack(double critExp, int dMax);
	
	public int getX() {
		return xCoordBuffer;
	}
	
	public int getY() {
		return yCoordBuffer;
	}
	
	public NetlistBlock getBlock() {
		return block;
	}
	
	public abstract int getTAPlusDelay(AbstractTerminal specificSuccessor);
	
	protected abstract double updateDelayOutgoing(double exponentiatedCriticalityOfSuccessor, AbstractTerminal specificSuccessor, int newX, int newY);
	public abstract double updateDelayIncoming(double exponentiatedCriticalityOfSuccessor, AbstractTerminal specificSuccessor, int newX, int newY);
	
	public abstract double computeWeightedSumOfDelays(double temperature, double exponentiatedCriticalityOfSuccessor, AbstractTerminal specificSuccessor);
	public abstract void addSuccessor(AbstractTerminal newSuccessor);
	public abstract void addPredecessor(AbstractTerminal newPredecessor);

	public abstract void confirmSwap();
	public abstract void confirmSwapDelay();
	
	public abstract void rollback();
	public abstract void rollbackDelay();

	public abstract double computeDeltaCost(int newX, int newY);
	public abstract double getWeightedCost(double expCrit, AbstractTerminal specificSuccessor);
	public abstract double getExpCrit(AbstractTerminal specificPredecessor);
	


	public abstract List<AbstractTerminal> traceCriticalPath(AbstractTerminal specificSuccessor);
	public abstract void generateOutput(StringBuilder output, AbstractTerminal specificSuccessor);
}
