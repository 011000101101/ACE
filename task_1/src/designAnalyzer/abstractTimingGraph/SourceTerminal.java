package designAnalyzer.abstractTimingGraph;

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
	protected double annotataTA(AbstractTerminal specificSuccessor) {
		int tmp= TimingAnalyzer.getInstance().lookUpSinglePathNoEndpoints(block, specificSuccessor.getBlock(), block.getX(), block.getY(), specificSuccessor.getX(), specificSuccessor.getY());
		successors.replace(specificSuccessor, tmp);
		return tA + tmp;
	}

	@Override
	protected double annotataTRAndSlack(int dMax) {
		if(tR != Integer.MAX_VALUE) {
			double tmp;
			for(AbstractTerminal t : successors.keySet()) {
				tmp= t.annotataTRAndSlack(dMax) - successors.get(t);//TimingAnalyzer.getInstance().lookUpSinglePathNoEndpoints(block, t.getBlock(), block.getX(), block.getY(), t.getX(), t.getY());
				if(tmp < tR) {
					tR= tmp;
				}
			}
		}
		return tR;
	}

	@Override
	protected void resetTA() {
		//do nothing, tA constant
	}

}
