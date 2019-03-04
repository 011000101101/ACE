package designAnalyzer.abstractedTimingGraph;

import java.util.Map;

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
	public void updateDelay(AbstractTerminal specificSuccessor) {
		//do nothing
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

}
