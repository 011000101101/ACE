package designAnalyzer.abstractedTimingGraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import designAnalyzer.timingAnalyzer.TimingAnalyzer;

public class PassTerminal extends AbstractTerminal {

	
	/**
	 * maps successors to exponentiated criticality of segment to that predecessors
	 */
	Map<AbstractTerminal, Double> predecessors;
	
	/**
	 * maps successors to delay of segment to that successor
	 */
	Map<AbstractTerminal, Integer> successors;
	
	/**
	 * caches the old values of delay
	 */
	Map<AbstractTerminal, Integer> successorsCache;
	
	/**
	 * temperature of current iteration, if computeWeightedSumOfDelays(...) has already been called in this iteration, or a larger value (or -1) if not
	 */
	double tempOfCurrentIteration;
	

	public PassTerminal(NetlistBlock newBlock) {
		super(newBlock);
		predecessors= new HashMap<AbstractTerminal, Double>(10);
		successors= new HashMap<AbstractTerminal, Integer>(10);
		successorsCache= new HashMap<AbstractTerminal, Integer>(10);
	}

	
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
		return tA + block.getSignalPassDelay() + successors.get(specificSuccessor);
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
											(double) (tR - t.getTAPlusDelay(this)) //slack
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
	public int getTAPlusDelay(AbstractTerminal specificSuccessor) {
		return tA + successors.get(specificSuccessor);
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
		successorsCache.put(newSuccessor, -1);
	}

	@Override
	public void addPredecessor(AbstractTerminal newPredecessor) {
		predecessors.put(newPredecessor, -1.0);
	}

	@Override
	public void rollback() {
		for(AbstractTerminal t : predecessors.keySet()) {
			t.rollbackDelay();
		}
		for(AbstractTerminal t : successors.keySet()) {
			successors.replace(t, successorsCache.get(t));
		}
	}

	@Override
	public void rollbackDelay() {
		for(AbstractTerminal t : successors.keySet()) {
			successors.replace(t, successorsCache.get(t));
		}
	}

	@Override
	public void confirmSwap() {
		for(AbstractTerminal t : predecessors.keySet()) {
			t.confirmSwapDelay();
		}
		for(AbstractTerminal t : successors.keySet()) {
			successorsCache.replace(t, successors.get(t));
		}
	}

	@Override
	public void confirmSwapDelay() {
		for(AbstractTerminal t : successors.keySet()) {
			successorsCache.replace(t, successors.get(t));
		}
	}

	@Override
	protected double updateDelayOutgoing(double exponentiatedCriticalityOfSuccessor, AbstractTerminal specificSuccessor,
			int newX, int newY) {
		int tmp= TimingAnalyzer.getInstance().lookUpSinglePathNoEndpoints(block, specificSuccessor.getBlock(), newX, newY, specificSuccessor.getX(), specificSuccessor.getY());
		successors.replace(specificSuccessor, tmp);
		return tmp * exponentiatedCriticalityOfSuccessor;
	}

	@Override
	public double updateDelayIncoming(double exponentiatedCriticalityOfSuccessor, AbstractTerminal specificSuccessor,
			int newX, int newY) {
		int tmp= TimingAnalyzer.getInstance().lookUpSinglePathNoEndpoints(block, specificSuccessor.getBlock(), xCoordBuffer, yCoordBuffer, newX, newY);
		successors.replace(specificSuccessor, tmp);
		return tmp * exponentiatedCriticalityOfSuccessor;
	}

	@Override
	public double computeDeltaCost(int newX, int newY) {
		xCoordBuffer= newX;
		yCoordBuffer= newY;
		double delta= 0.0;
		
		for(AbstractTerminal t : successors.keySet()) {
			double critExp= t.getExpCrit(this);
			delta-= successors.get(t) * critExp;
			delta+= updateDelayOutgoing(critExp, t, newX, newY);
		}
		
		for(AbstractTerminal t : predecessors.keySet()) {
			delta-= t.getWeightedCost(predecessors.get(t), this);
			delta+= t.updateDelayIncoming(predecessors.get(t), this, newX, newY);
		}
		
		return delta;
	}

	@Override
	public double getWeightedCost(double expCrit, AbstractTerminal specificSuccessor) {
		return successors.get(specificSuccessor) * expCrit;
	}

	@Override
	public double getExpCrit(AbstractTerminal specificPredecessor) {
		return predecessors.get(specificPredecessor);
	}

	@Override
	public List<AbstractTerminal> traceCriticalPath(AbstractTerminal specificSuccessor) {
		List<AbstractTerminal> output= null;
		AbstractTerminal criticalPred= null;
		double criticality= 0;
		for(AbstractTerminal t : predecessors.keySet()) {
			if(predecessors.get(t) > criticality) {
				criticality= predecessors.get(t);
				criticalPred= t;
			}
		}
		output= criticalPred.traceCriticalPath(this);
		output.add(this);
		return output;
	}

	@Override
	public void generateOutput(StringBuilder output, AbstractTerminal specificSuccessor) {
		output.append("CLB(comb)\t");
		output.append(block.getName());
		output.append("(");
		output.append(block.getX());
		output.append(",");
		output.append(block.getY());
		output.append(")");
		output.append("\t");
		output.append(successors.get(specificSuccessor));
		output.append("\t");
		output.append(tA);
		output.append("\n");
	}

}
