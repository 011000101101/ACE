package designAnalyzer.abstractedTimingGraph;

import java.util.Map;

import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import designAnalyzer.timingAnalyzer.TimingAnalyzer;

public class SourceTerminal extends AbstractTerminal {

	/**
	 * maps successors to delay of segment to that successor
	 */
	Map<AbstractTerminal, Integer> successors;
	
	public SourceTerminal(NetlistBlock newBlock) {
		super(newBlock);
		tA= block.getSignalEntryDelay();
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
	public int getTAMinusDelay(AbstractTerminal specificSuccessor) {
		return tA - successors.get(specificSuccessor);
	}

	@Override
	public void updateDelay(AbstractTerminal specificSuccessor) {
		int tmp= TimingAnalyzer.getInstance().lookUpSinglePathNoEndpoints(block, specificSuccessor.getBlock(), block.getX(), block.getY(), specificSuccessor.getX(), specificSuccessor.getY());
		successors.replace(specificSuccessor, tmp);
	}

	@Override
	public double computeWeightedSumOfDelays(double temperature, double exponentiatedCriticalityOfSuccessor, AbstractTerminal specificSuccessor) {
		return successors.get(specificSuccessor) * exponentiatedCriticalityOfSuccessor;
	}

	@Override
	public void addSuccessor(AbstractTerminal newSuccessor) {
		successors.put(newSuccessor, TimingAnalyzer.getInstance().lookUpSinglePathNoEndpoints(block, newSuccessor.getBlock(), block.getX(), block.getY(), newSuccessor.getX(), newSuccessor.getY()));
	}

	@Override
	public void addPredecessor(AbstractTerminal newPredecessor) {
		//won't be called
	}

}
