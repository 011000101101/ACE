package designAnalyzer.abstractedTimingGraph;

import java.util.Collection;
import java.util.LinkedList;

import designAnalyzer.structures.Net;
import designAnalyzer.structures.pathElements.blocks.LogicBlock;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

public class AbstractedTimingGraph {

	private Collection<SourceTerminal> sourceTerminals;
	private Collection<SinkTerminal> sinkTerminals;
	
	public AbstractedTimingGraph(Collection<Net> nets){
		for(Net n : nets) {
			extendGraph(n);
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
				SinkTerminal tmp= createSinkTerminal(b);createSinkTerminal(b);
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
	
}
