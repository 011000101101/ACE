package designAnalyzer.abstractTimingGraph;

import java.util.Map;

import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import designAnalyzer.timingAnalyzer.TimingAnalyzer;

public class SinkTerminal extends AbstractTerminal {

	public SinkTerminal(NetlistBlock newBlock) {
		super(newBlock);
	}

	/**
	 * maps successors to slack of segment to that successor
	 */
	Map<AbstractTerminal, Integer> predecessors;
	
	@Override
	protected double annotataTA(AbstractTerminal specificSuccessor) {
		if(tA != -1) {
			return tA + block.getSignalExitDelay();
		}
		else {
			double tmp;
			for(AbstractTerminal t : predecessors.keySet()) {
				tmp= t.annotataTA(this);
				if(tmp < tA) {
					tA= tmp;
				}
			}
			return tA + block.getSignalExitDelay();
		}
	}

	@Override
	protected double annotataTRAndSlack(int dMax) {
		if(tR != Integer.MAX_VALUE) {
			tR= dMax;
		}
		//TODO do slack
		return tR;
	}

	@Override
	protected void resetTA() {
		tA= -1;
	}

}
