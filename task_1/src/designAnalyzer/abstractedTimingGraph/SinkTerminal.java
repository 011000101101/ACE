package designAnalyzer.abstractedTimingGraph;

import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

public class SinkTerminal extends AbstractTerminal {

	public SinkTerminal(NetlistBlock newBlock) {
		super(newBlock);
	}

//	/**
//	 * maps successors to exponentiated criticality of segment to that predecessors
//	 */
//	Map<AbstractTerminal, Double> predecessors;
	
	/**
	 * predecessor (immediate signal source)
	 */
	AbstractTerminal predecessor;
	/**
	 * exponentiated criticality of segment to predecessor
	 */
	double expCrit;
	
	@Override
	protected int annotataTA(AbstractTerminal specificSuccessor) {
		tR= Integer.MAX_VALUE; //may be reset here! (saves one method call...)
		
		if(tA == -1) {
//			int tmp;
//			for(AbstractTerminal t : predecessors.keySet()) {
//				tmp= t.annotataTA(this);
//				if(tmp > tA) {
//					tA= tmp;
//				}
//			}
			tA= predecessor.annotataTA(this);
		}
		return tA + block.getSignalExitDelay();
	}

	@Override
	protected int annotataTRAndSlack(double critExp, int dMax) {
		if(tR == Integer.MAX_VALUE) {
			tR= dMax;
//			for(AbstractTerminal t : predecessors.keySet()) { //annotate criticality
//				predecessors.replace(t, 
//						(
//								Math.pow(
//									(double) 1
//									- (
//											(double) (tR - t.getTAMinusDelay(this)) //slack
//											/ (double) dMax
//									),
//									critExp
//								)
//						)
//				);
//			}
			expCrit= (
					Math.pow(
							(double) 1
							- (
									(double) (tR - predecessor.getTAMinusDelay(this)) //slack
											/ (double) dMax
									),
									critExp
							)
					);
		}
		tA= -1; //may be reset here! (saves one method call...)
		return tR;
	}

	@Override
	public int getTAMinusDelay(AbstractTerminal specificSuccessor) {
		return -1;
	}

	@Override
	public double computeWeightedSumOfDelays(double temperature, double exponentiatedCriticalityOfSuccessor,
			AbstractTerminal specificSuccessor) {
//		double sum= 0;
//		for(AbstractTerminal t : predecessors.keySet()) {
//			sum+= t.computeWeightedSumOfDelays(temperature, predecessors.get(t), this);
//		}
//		return sum;
		return predecessor.computeWeightedSumOfDelays(temperature, expCrit, this);
	}

	@Override
	public void addSuccessor(AbstractTerminal newSuccessor) {
		//won't be called
	}

	@Override
	public void addPredecessor(AbstractTerminal newPredecessor) {
		predecessor= newPredecessor;
	}

	@Override
	public void rollback() {
		predecessor.rollbackDelay();
		xCoordBuffer= block.getX();
		yCoordBuffer= block.getY();
	}

	@Override
	public void rollbackDelay() {
		//do nothing
	}

	@Override
	public void confirmSwap() {
		predecessor.confirmSwapDelay();
	}

	@Override
	public void confirmSwapDelay() {
		//do nothing
	}

	@Override
	public double computeDeltaCost(int newX, int newY) {
		xCoordBuffer= newX;
		yCoordBuffer= newY;
		double delta= -1 * predecessor.getWeightedCost(expCrit, this);
		delta+= predecessor.updateDelayIncoming(expCrit, this, newX, newY);
		return delta;
	}

	@Override
	public double getWeightedCost(double expCrit, AbstractTerminal specificSuccessor) {
		//do nothing
		return -1;
	}

	@Override
	protected double updateDelayOutgoing(double exponentiatedCriticalityOfSuccessor, AbstractTerminal specificSuccessor,
			int newX, int newY) {
		return -1;
	}

	@Override
	public double updateDelayIncoming(double exponentiatedCriticalityOfSuccessor, AbstractTerminal specificSuccessor,
			int newX, int newY) {
		return -1;
	}

	@Override
	public double getExpCrit(AbstractTerminal specificPredecessor) {
		return expCrit;
	}

}
