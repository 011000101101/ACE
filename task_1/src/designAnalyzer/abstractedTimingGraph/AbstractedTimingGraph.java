package designAnalyzer.abstractedTimingGraph;

import java.util.Collection;
import java.util.LinkedList;

import designAnalyzer.structures.Net;
import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.LogicBlock;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

public class AbstractedTimingGraph {

	private Collection<SourceTerminal> sourceTerminals;
	private Collection<SinkTerminal> sinkTerminals;
	
	public AbstractedTimingGraph(Collection<Net> nets){
		sourceTerminals= new LinkedList<SourceTerminal>();
		sinkTerminals= new LinkedList<SinkTerminal>();
		for(Net n : nets) {
			if(!n.getIsClocknNet()) {
				extendGraph(n);
			}
		}
	}

	private void extendGraph(Net n) {
		AbstractTerminal immediateSourceTerminal;
		AbstractTerminal[] immediateSinkTerminals= new AbstractTerminal[n.getSinks().size()];
		int j= 0;
		
		if( ! (n.getSource() instanceof LogicBlock && !((LogicBlock) n.getSource()).isClocked())) { //not a combinatorial logic block...
			SourceTerminal tmp= createSourceTerminal(n.getSource());
			sourceTerminals.add(tmp);
			immediateSourceTerminal= tmp;
		}
		else {
			immediateSourceTerminal= getPassTerminal((LogicBlock) n.getSource());
		}
		
		for(NetlistBlock b : n.getSinks()) {
			if( ! (b instanceof LogicBlock && ! ((LogicBlock) b).isClocked() ) ) { //not a combinatorial logic block...
				SinkTerminal tmp= createSinkTerminal(b);
				sinkTerminals.add(tmp);
				immediateSinkTerminals[j]= tmp;
				j++;
			}
			else {
				immediateSinkTerminals[j]= getPassTerminal((LogicBlock) b);
				j++;
			}
		}
		
		for(AbstractTerminal t : immediateSinkTerminals) {
			immediateSourceTerminal.addSuccessor(t);
			t.addPredecessor(immediateSourceTerminal);
		}
	}

	private PassTerminal getPassTerminal(LogicBlock combBlock) {
		PassTerminal passTerminal= combBlock.getPassTerminal();
		if(passTerminal == null) {
			passTerminal= new PassTerminal(combBlock);
			combBlock.setPassTerminal(passTerminal);
		}
		return passTerminal;
	}

	private SinkTerminal createSinkTerminal(NetlistBlock sink) {
		SinkTerminal sinkTerminal= new SinkTerminal(sink);
		sink.addSinkTerminal(sinkTerminal);
		return sinkTerminal;
	}

	private SourceTerminal createSourceTerminal(NetlistBlock source) {
		SourceTerminal sourceTerminal= new SourceTerminal(source);
		source.setSourceTerminal(sourceTerminal);
		return sourceTerminal;
	}
	
	public double analyzeTiming(double critExp, double temperature) {
		
		int dMax= -1;
		int tmp;
		for(SinkTerminal t : sinkTerminals) { //annotate tA
			tmp= t.annotataTA(null);
			if(tmp > dMax) dMax= tmp;
		}
		
		for(SourceTerminal t : sourceTerminals) { //annotate tR and slack (saved already converted into exponentiated criticality)
			t.annotataTRAndSlack(critExp, dMax);
		}
		
		double totalCost= 0.0;
		for(SinkTerminal t : sinkTerminals) { //compute sum of weighted timing costs
			totalCost+= t.computeWeightedSumOfDelays(temperature, -1, null);
		}
		
		return totalCost;
		
	}
	
	public double computeDeltaCost(NetlistBlock block1, NetlistBlock block2, int block2XCoord, int block2YCoord) {
		double delta= 0.0;
		
		delta+= computeDeltaCostForOneBlock(block1, block2XCoord, block2YCoord);
		if(block2 != null) delta+= computeDeltaCostForOneBlock(block2, block1.getX(), block1.getY());
		
		return delta;
	}
	
	private double computeDeltaCostForOneBlock(NetlistBlock block, int newX, int newY) {
		double delta= 0.0;
		
		if(block instanceof LogicBlock && ((LogicBlock) block).isClocked()) { //is combinatorial block
			PassTerminal tmp0= ((LogicBlock) block).getPassTerminal();
			if(tmp0 != null) delta+= tmp0.computeDeltaCost(newX, newY);
		}
		else {
			
			SourceTerminal tmp= block.getSourceTerminal();
			if(tmp != null) delta+= tmp.computeDeltaCost(newX, newY);
			
			if(block instanceof LogicBlock) {
				SinkTerminal[] tmp1= ((LogicBlock) block).getSinkTerminals();
				for(int j= 0; j < 4; j++) {
					if(tmp1[j] == null) break;
					delta+= tmp1[j].computeDeltaCost(newX, newY);
				}
			}
			
			else {
				SinkTerminal tmp2= ((IOBlock) block).getSinkTerminal();
				if(tmp2 != null) delta+= tmp2.computeDeltaCost(newX, newY);
			}
		}
		
		return delta;
	}

	public void confirmSwap(NetlistBlock block1, NetlistBlock block2) {
		confirmSwap(block1);
		if(block2 != null) confirmSwap(block2);
	}
	
	private void confirmSwap(NetlistBlock block) {
		if(block instanceof LogicBlock && ((LogicBlock) block).isClocked()) { //is combinatorial block
			PassTerminal tmp0= ((LogicBlock) block).getPassTerminal();
			if(tmp0 != null) tmp0.confirmSwap();
		}
		else {
			SourceTerminal tmp1= block.getSourceTerminal();
			if(tmp1 != null) tmp1.confirmSwap();
			if(block instanceof LogicBlock) {
				SinkTerminal[] tmp= ((LogicBlock) block).getSinkTerminals();
				if(tmp != null) {
					for(int j= 0; j < 4; j++) {
						if(tmp[j] == null) break;
						tmp[j].confirmSwap();
					}
				}
			}
			else {
				SinkTerminal tmp2= ((IOBlock) block).getSinkTerminal();
				if(tmp2 != null) tmp2.confirmSwap();
			}
		}
	}

	public void abortSwap(NetlistBlock block1, NetlistBlock block2) {
		rollback(block1);
		if(block2 != null) rollback(block2);
	}

	private void rollback(NetlistBlock block) {
		if(block instanceof LogicBlock && ((LogicBlock) block).isClocked()) { //is combinatorial block
			PassTerminal tmp0= ((LogicBlock) block).getPassTerminal();
			if(tmp0 != null) tmp0.rollback();
		}
		else {
			SourceTerminal tmp1= block.getSourceTerminal();
			if(tmp1 != null) tmp1.rollback();
			if(block instanceof LogicBlock) {
				SinkTerminal[] tmp= ((LogicBlock) block).getSinkTerminals();
				if(tmp != null) {
					for(int j= 0; j < 4; j++) {
						if(tmp[j] == null) break;
						tmp[j].rollback();
					}
				}
			}
			else {
				SinkTerminal tmp2= ((IOBlock) block).getSinkTerminal();
				if(tmp2 != null) tmp2.rollback();
			}
		}
	}
	
}
