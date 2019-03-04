package designAnalyzer.abstractedTimingGraph;

import java.util.Map;

import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import designAnalyzer.timingAnalyzer.TimingAnalyzer;

public class PassTerminal extends AbstractTerminal {

	public PassTerminal(NetlistBlock newBlock) {
		super(newBlock);
		// TODO Auto-generated constructor stub
	}

	/**
	 * maps successors to exponentiated criticality of segment to that predecessors
	 */
	Map<AbstractTerminal, Double> predecessors;
	/**
	 * maps successors to delay of segment to that successor
	 */
	Map<AbstractTerminal, Integer> successors;
	
	double tempOfCurrentIteration;

	@Override
	protected int annotataTA(AbstractTerminal specificSuccessor) {
		tR= Integer.MAX_VALUE; //may be reset here! (saves one method call...)
		
		if(tA == -1) {
			int tmp;
			for(AbstractTerminal t : predecessors.keySet()) {
				tmp= t.annotataTA(this);
				if(tmp > tA) {
					tA= tmp;
				}
			}
		}
		return tA + block.getSignalPassDelay();
	}

	@Override
	protected int annotataTRAndSlack(double critExp, int dMax) {
		if(tR == Integer.MAX_VALUE) {
			int tmp;
			for(AbstractTerminal t : successors.keySet()) {
				tmp= t.annotataTRAndSlack(critExp, dMax) - successors.get(t);//TimingAnalyzer.getInstance().lookUpSinglePathNoEndpoints(block, t.getBlock(), block.getX(), block.getY(), t.getX(), t.getY());
				if(tmp < tR) {
					tR= tmp;
				}
			}
			for(AbstractTerminal t : predecessors.keySet()) { //annotate slack
				predecessors.replace(t, 
						(
								Math.pow(
									(double) 1
									- (
											(double) (tR - t.getTAMinusDelay(this)) //slack
											/ (double) dMax
									),
									critExp
								)
						)
				);
			}
		}
		tA= -1; //may be reset here! (saves one method call...)
		return tR;
	}

	@Override
	public int getTAMinusDelay(AbstractTerminal specificSuccessor) {
		return tA - successors.get(specificSuccessor);
	}

	@Override
	public void updateDelay(AbstractTerminal specificSuccessor) {
		int tmp= TimingAnalyzer.getInstance().lookUpSinglePathNoEndpoints(block, specificSuccessor.getBlock(), block.getX(), block.getY(), specificSuccessor.getX(), specificSuccessor.getY());
		successors.replace(specificSuccessor, tmp);
	}

	@Override
	public double computeWeightedSumOfDelays(double temperature, double exponentiatedCriticalityOfSuccessor,
			AbstractTerminal specificSuccessor) {
		if(tempOfCurrentIteration != temperature) { //sum of incoming edges not yet computed
			tempOfCurrentIteration= temperature; //temperature monotonically decreasing -> can be used as 'validity date'
			double sum= 0;
			for(AbstractTerminal t : predecessors.keySet()) { //compute weighted sum of all incoming edges
				sum+= t.computeWeightedSumOfDelays(temperature, predecessors.get(t), this);
			}
			return sum + successors.get(specificSuccessor) * exponentiatedCriticalityOfSuccessor; //add weighted delay of specific outgoing edge
		}
		else { //sum of incoming edges already computed, every edge must only be computed once
			return successors.get(specificSuccessor) * exponentiatedCriticalityOfSuccessor; //return only weighted delay of specific outgoing edge
		}
	}

	@Override
	public void addSuccessor(AbstractTerminal newSuccessor) {
		successors.put(newSuccessor, TimingAnalyzer.getInstance().lookUpSinglePathNoEndpoints(block, newSuccessor.getBlock(), block.getX(), block.getY(), newSuccessor.getX(), newSuccessor.getY()));
	}

	@Override
	public void addPredecessor(AbstractTerminal newPredecessor) {
		predecessors.put(newPredecessor, -1.0);
	}

}
