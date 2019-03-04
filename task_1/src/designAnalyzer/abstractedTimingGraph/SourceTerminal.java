package designAnalyzer.abstractedTimingGraph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import designAnalyzer.timingAnalyzer.TimingAnalyzer;

public class SourceTerminal extends AbstractTerminal {

	/**
	 * maps successors to delay of segment to that successor
	 */
	Map<AbstractTerminal, Integer> successors;
	/**
	 * caches the old values of delay
	 */
	Map<AbstractTerminal, Integer> successorsCache;
	
	public SourceTerminal(NetlistBlock newBlock) {
		super(newBlock);
		tA= block.getSignalEntryDelay();
		successors= new HashMap<AbstractTerminal, Integer>(10);
		successorsCache= new HashMap<AbstractTerminal, Integer>(10);
	}
	
	@Override
	protected int annotataTA(AbstractTerminal specificSuccessor) {
		tR= Integer.MAX_VALUE; //may be reset here! (saves one method call...)
		
		return tA + successors.get(specificSuccessor);
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
		}
		//tA constant, no need to reset
		return tR;
	}

	@Override
	public int getTAPlusDelay(AbstractTerminal specificSuccessor) {
		return tA + successors.get(specificSuccessor);
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
	public double computeWeightedSumOfDelays(double temperature, double exponentiatedCriticalityOfSuccessor, AbstractTerminal specificSuccessor) {
		return successors.get(specificSuccessor) * exponentiatedCriticalityOfSuccessor;
	}

	@Override
	public void addSuccessor(AbstractTerminal newSuccessor) {
		successors.put(newSuccessor, TimingAnalyzer.getInstance().lookUpSinglePathNoEndpoints(block, newSuccessor.getBlock(), block.getX(), block.getY(), newSuccessor.getX(), newSuccessor.getY()));
		successorsCache.put(newSuccessor, -1);
	}

	@Override
	public void addPredecessor(AbstractTerminal newPredecessor) {
		//won't be called
	}

	@Override
	public void rollback() {
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
	public double computeDeltaCost(int newX, int newY) {
		double delta= 0.0;
		
		for(AbstractTerminal t : successors.keySet()) {
			double critExp= t.getExpCrit(this);
			delta-= successors.get(t) * critExp;
			delta+= updateDelayOutgoing(critExp, t, newX, newY);
		}
		
		return delta;
	}

	@Override
	public double getWeightedCost(double expCrit, AbstractTerminal specificSuccessor) {
		return successors.get(specificSuccessor) * expCrit;
	}

	@Override
	public double getExpCrit(AbstractTerminal passTerminal) {
		return -1;
	}

	@Override
	public List<AbstractTerminal> traceCriticalPath(AbstractTerminal specificSuccessor) {
		List<AbstractTerminal> output= new LinkedList<AbstractTerminal>();
		output.add(this);
		return output;
	}

	@Override
	public void generateOutput(StringBuilder output, AbstractTerminal specificSuccessor) {
		
		output.append(
				(block instanceof IOBlock) ? "I_Block\t" : "CLB(seq)\t"
		);
		output.append(block.getName());
		output.append("(");
		output.append(block.getX());
		output.append(",");
		output.append(block.getY());
		output.append(")");
		output.append(
				(block instanceof IOBlock) ? (((IOBlock) block).getSubblk_1() ? "1\t" : "0\t" ) : "\t"
		);
		output.append(successors.get(specificSuccessor));
		output.append("\t");
		output.append(tA);
		output.append("\n");
	}

}
