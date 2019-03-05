package designAnalyzer.abstractedTimingGraph;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import designAnalyzer.structures.Net;
import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.LogicBlock;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

public class AbstractedTimingGraph {

	
	/**
	 * all terminals which act as signal sources, represent input nodes into the timing graph
	 */
	private Collection<SourceTerminal> sourceTerminals;
	
	/**
	 * all terminals which act as signal sinks, represent output nodes into the timing graph
	 */
	private Collection<SinkTerminal> sinkTerminals;
	
	
	/**
	 * constructor also generates the complete timing graph from the given list of nets (excluding clock generating nets)
	 * @param nets collection of the nets which make up the timing graph
	 */
	public AbstractedTimingGraph(Collection<Net> nets){
		
		sourceTerminals= new LinkedList<SourceTerminal>();
		sinkTerminals= new LinkedList<SinkTerminal>();
		for(Net n : nets) {
			if(!n.getIsClocknNet()) {
				extendGraph(n);
			}
		}
		
	}
	

	/**
	 * extends the already existing graph by all edges stored inside the given net, creating new source and sink terminals, and retrieving/creating pass terminals
	 * @param n the net by whichs content to expand the graph
	 */
	private void extendGraph(Net n) {
		
		AbstractTerminal immediateSourceTerminal;
		AbstractTerminal[] immediateSinkTerminals= new AbstractTerminal[n.getSinks().size()];
		int j= 0;
		
		//process source
		if( ! (n.getSource() instanceof LogicBlock && !((LogicBlock) n.getSource()).isClocked())) { //not a combinatorial logic block...
			SourceTerminal tmp= createSourceTerminal(n.getSource());
			sourceTerminals.add(tmp);
			immediateSourceTerminal= tmp;
		}
		else {
			immediateSourceTerminal= getPassTerminal((LogicBlock) n.getSource());
		}
		
		//process all sinks
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
		
		//link source to all sinks (establish Edges)
		for(AbstractTerminal t : immediateSinkTerminals) {
			immediateSourceTerminal.addSuccessor(t);
			t.addPredecessor(immediateSourceTerminal);
		}
	}

	/**
	 * try to retrieve an existing pass terminal from a given combinatorial block, create and assign a new one if none present
	 * @param combBlock the block for which to retrieve/create a pass terminal
	 * @return the retrieved/created pass terminal associated with the given block
	 */
	private PassTerminal getPassTerminal(LogicBlock combBlock) {
		PassTerminal passTerminal= combBlock.getPassTerminal();
		if(passTerminal == null) {
			passTerminal= new PassTerminal(combBlock);
			combBlock.setPassTerminal(passTerminal);
		}
		return passTerminal;
	}

	/**
	 * creates a new sink terminal and assigns it to the given block
	 * @param sink the block associated with the terminal
	 * @return the new terminal associated with the given block
	 */
	private SinkTerminal createSinkTerminal(NetlistBlock sink) {
		SinkTerminal sinkTerminal= new SinkTerminal(sink);
		sink.addSinkTerminal(sinkTerminal);
		return sinkTerminal;
	}

	/**
	 * creates a new source terminal and assigns it to the given block
	 * @param source the block associated with the terminal
	 * @return the new terminal associated with the given block
	 */
	private SourceTerminal createSourceTerminal(NetlistBlock source) {
		SourceTerminal sourceTerminal= new SourceTerminal(source);
		source.setSourceTerminal(sourceTerminal);
		return sourceTerminal;
	}
	
	/**
	 * analyzes tA, tR and slack and computes and returns the timing cost for the placer (weighted sum of edge delays)
	 * @param critExp the current criticality exponent
	 * @param temperature the current temperature
	 * @return the total timing cost
	 */
	public double analyzeTiming(double critExp, double temperature) {
		
		int dMax= -1;
		int tmp;
		for(SinkTerminal t : sinkTerminals) { //annotate tA
			tmp= t.annotataTA(null);
			if(tmp > dMax) dMax= tmp;
		}
		
		System.out.println("dMax: " + dMax);
		
		for(SourceTerminal t : sourceTerminals) { //annotate tR and slack (saved already converted into exponentiated criticality)
			t.annotataTRAndSlack(critExp, dMax);
		}
		
		double totalCost= 0.0;
		for(SinkTerminal t : sinkTerminals) { //compute sum of weighted timing costs
			totalCost+= t.computeWeightedSumOfDelays(temperature, -1, null);
		}
		
		return totalCost;
		
	}
	
	/**
	 * computes the changed cost due to swapping block1 with block2 or moving block1 to (block2XCoord, block2YCoord) only (if block2 == null), without reanalyzing tA, tR or slack
	 * @param block1 the selected block to swap / move
	 * @param block2 the second block to swap if not null
	 * @param block2XCoord x coordinate of the second block, or of the empty slot to move block1 to
	 * @param block2YCoord y coordinate of the second block, or of the empty slot to move block1 to
	 * @return the total change in the cost
	 */
	public double computeDeltaCost(NetlistBlock block1, NetlistBlock block2, int block2XCoord, int block2YCoord) {
		
		double delta= 0.0;
		
		delta+= computeDeltaCostForOneBlock(block1, block2XCoord, block2YCoord);
		if(block2 != null) delta+= computeDeltaCostForOneBlock(block2, block1.getX(), block1.getY());
		
		return delta;
		
	}
	
	/**
	 * computes the changed cost due to moving block to (newX, newY)
	 * @param block the block that has moved
	 * @param newX x coordinate of the slot to move block to
	 * @param newY y coordinate of the slot to move block to
	 * @return the change in the costcaused by this block move
	 */
	private double computeDeltaCostForOneBlock(NetlistBlock block, int newX, int newY) {
		double delta= 0.0;
		
		
		if(block instanceof LogicBlock && !((LogicBlock) block).isClocked()) { //is combinatorial block
			PassTerminal tmp0= ((LogicBlock) block).getPassTerminal();
			if(tmp0 != null) {
				delta+= tmp0.computeDeltaCost(newX, newY);
			}
		}
		else {
			
			SourceTerminal tmp= block.getSourceTerminal();
			if(tmp != null) {
				delta+= tmp.computeDeltaCost(newX, newY);
			}
			
			if(block instanceof LogicBlock) {
				SinkTerminal[] tmp1= ((LogicBlock) block).getSinkTerminals();
				for(int j= 0; j < 4; j++) {
					if(tmp1[j] == null) break;
					delta+= tmp1[j].computeDeltaCost(newX, newY);
				}
			}
			
			else {
				SinkTerminal tmp2= ((IOBlock) block).getSinkTerminal();
				if(tmp2 != null) {
					delta+= tmp2.computeDeltaCost(newX, newY);
				}
			}
		}
		
		return delta;
	}

	/**
	 * makes the cost change permanent for all moved blocks
	 * @param block1 block that was swapped/moved
	 * @param block2 second block that was swapped or null
	 */
	public void confirmSwap(NetlistBlock block1, NetlistBlock block2) {
		confirmSwap(block1);
		if(block2 != null) confirmSwap(block2);
	}
	
	/**
	 * makes the cost change permanent for the given blocks
	 * @param block1 block that was swapped/moved
	 */
	private void confirmSwap(NetlistBlock block) {
		if(block instanceof LogicBlock && !((LogicBlock) block).isClocked()) { //is combinatorial block
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

	/**
	 * reverts the cost change for all moved blocks
	 * @param block1 block that was swapped/moved
	 * @param block2 second block that was swapped or null
	 */
	public void abortSwap(NetlistBlock block1, NetlistBlock block2) {
		rollback(block1);
		if(block2 != null) rollback(block2);
	}

	/**
	 * reverts the cost change for the given blocks
	 * @param block1 block that was swapped/moved
	 */
	private void rollback(NetlistBlock block) {
		if(block instanceof LogicBlock && !((LogicBlock) block).isClocked()) { //is combinatorial block
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

	/**
	 * traces the critical path backwards from the given sink and returns a list of all terminals on the path
	 * @param criticalSink
	 * @return
	 */
	public List<AbstractTerminal> traceCriticalPath(AbstractTerminal criticalSink) {
		
		if(criticalSink != null) return criticalSink.traceCriticalPath(null);
		else {
			System.err.println("could not print critical path: no sink terminals present");
			return null;
		}
	}

	/**
	 * retrieves the sink terminal that lies at the end of the critical path
	 * @return the sink terminal that lies at the end of the critical path
	 */
	public AbstractTerminal getCriticalSink() {
		SinkTerminal criticalSink= null;
		int dMax= -1;
		for(SinkTerminal t : sinkTerminals) {
			if(t.getTAPlusDelay(null) > dMax) {
				dMax= t.getTAPlusDelay(null);
				criticalSink= t;
			}
		}
		return criticalSink;
	}
	
	/**
	 * annotates tA without annotating tR and slack
	 */
	public void annotateTAOnly() {
		for(SinkTerminal t : sinkTerminals) {
			t.annotataTA(null);
		}
	}
	
}
