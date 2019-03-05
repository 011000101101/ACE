package designAnalyzer.abstractedTimingGraph;

import java.util.List;

import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

public abstract class AbstractTerminal {

	
	/**
	 * the block owning this terminal
	 */
	protected NetlistBlock block;
	
	/**
	 * stored value for tA, or -1 if invalid
	 */
	protected int tA;
	/**
	 * stored value for tR, or Integer.MAX_VALUE if invalid
	 */
	protected int tR;
	
	/**
	 * buffered x coordinate of owning block, so computing delta for two swapped blocks sharing a direct common edge in the timing graph will still work (block coordinates are only updated after the swap has been accepted...)
	 */
	protected int xCoordBuffer;
	/**
	 * buffered y coordinate of owning block, so computing delta for two swapped blocks sharing a direct common edge in the timing graph will still work (block coordinates are only updated after the swap has been accepted...)
	 */
	protected int yCoordBuffer;
	
	
	/**
	 * constructor...
	 * @param newBlock the block owning this new terminal
	 */
	public AbstractTerminal(NetlistBlock newBlock) {
		block= newBlock;
		xCoordBuffer= block.getX();
		yCoordBuffer= block.getY();
		tA= -1;
		tR= Integer.MAX_VALUE;
	}
	
	
	/**
	 * recursively annotates tA throughout the timing graph (DFS from sinks to sources, postorder computation)
	 * @param specificSuccessor the terminal that called this method, or null if it was called externally
	 * @return tA of this terminal plus delay from this terminal to specificSuccessor
	 */
	protected abstract int annotataTA(AbstractTerminal specificSuccessor);
	
	/**
	 * recursively annotates tR and exponentiated Criticality (instead of not directly needed slack) throughout the timing graph (DFS from sources to sinks, postorder computation)
	 * @param critExp current criticality exponent
	 * @param dMax maximum delay in the timing graph
	 * @return tR of this terminal
	 */
	protected abstract int annotataTRAndSlack(double critExp, int dMax);
	
	/**
	 * gets buffered x Coordinate of this terminal
	 * @return buffered x Coordinate of this terminal
	 */
	public int getX() {
		return xCoordBuffer;
	}
	
	/**
	 * gets buffered y Coordinate of this terminal
	 * @return buffered y Coordinate of this terminal
	 */
	public int getY() {
		return yCoordBuffer;
	}
	
	public NetlistBlock getBlock() {
		return block;
	}
	
	/**
	 * returns tA of this terminal plus delay from this terminal to specificSuccessor
	 * @param specificSuccessor the terminal that called this method
	 * @return tA of this terminal plus delay from this terminal to specificSuccessor
	 */
	public abstract int getTAPlusDelay(AbstractTerminal specificSuccessor);
	
	/**
	 * locally updates delay of outgoing edge to specificSuccessor, returns new delay times exponentiatedCriticalityOfSuccessor (new weighted cost of this edge)
	 * @param exponentiatedCriticalityOfSuccessor
	 * @param specificSuccessor the terminal at the other end of teh edge to update
	 * @param newX new x coordinate of this terminal
	 * @param newY new y coordinate of this terminal
	 * @return new weighted cost of this edge
	 */
	protected abstract double updateDelayOutgoing(double exponentiatedCriticalityOfSuccessor, AbstractTerminal specificSuccessor, int newX, int newY);
	
	/**
	 * updates delay of outgoing edge to specificSuccessor, returns new delay times exponentiatedCriticalityOfSuccessor (new weighted cost of this edge)<br>
	 * (this method is called at the preceding terminals of the one that changed)
	 * @param exponentiatedCriticalityOfSuccessor
	 * @param specificSuccessor the terminal that called this method
	 * @param newX new x coordinate of specificSuccessor
	 * @param newY new y coordinate of specificSuccessor
	 * @return new weighted cost of this edge
	 */
	public abstract double updateDelayIncoming(double exponentiatedCriticalityOfSuccessor, AbstractTerminal specificSuccessor, int newX, int newY);
	
	/**
	 * recursively compute total timing cost (weighted sum of delays over all edges)
	 * @param temperature current temperature
	 * @param exponentiatedCriticalityOfSuccessor
	 * @param specificSuccessor the terminal that called this method, or null if it was called externally
	 * @return the total timing cost of all not yet computed edges between all sources feeding this terminal and this terminal
	 */
	public abstract double computeWeightedSumOfDelays(double temperature, double exponentiatedCriticalityOfSuccessor, AbstractTerminal specificSuccessor);
	
	/**
	 * adds a new successing terminal to this terminal (establishes new edge)
	 * @param newSuccessor
	 */
	public abstract void addSuccessor(AbstractTerminal newSuccessor);
	
	/**
	 * adds a new preceding terminal to this terminal (establishes new edge)
	 * @param newPredecessor
	 */
	public abstract void addPredecessor(AbstractTerminal newPredecessor);

	/**
	 * makes the cost change permanent for the block connected to this terminal
	 */
	public abstract void confirmSwap();
	/**
	 * makes the cost change permanent for all outgoing edges (called at predecessors of moved/swapped terminal)
	 */
	public abstract void confirmSwapDelay();
	
	/**
	 * aborts the cost change for the block connected to this terminal
	 */
	public abstract void rollback();
	/**
	 * aborts the cost change for all outgoing edges (called at predecessors of moved/swapped terminal)
	 */
	public abstract void rollbackDelay();

	/**
	 * computes the cost change when moving the block owning this terminal to (newX, newY)
	 * @param newX new x Coordinate of the owning block
	 * @param newY new y Coordinate of the owning block
	 * @return deltaCost for this move
	 */
	public abstract double computeDeltaCost(int newX, int newY);
	/**
	 * retrieves the weighted cost of the edge from this terminal to specificSuccessor
	 * @param expCrit exponentiated criticality of that edge
	 * @param specificSuccessor the terminal that called this method
	 * @return the weighted cost of the edge from this terminal to specificSuccessor
	 */
	public abstract double getWeightedCost(double expCrit, AbstractTerminal specificSuccessor);
	
	/**
	 * gets the exponentiated criticality of that edge from specificPredecessor to this terminal
	 * @param specificPredecessor the terminal that called this method
	 * @return the exponentiated criticality of that edge from specificPredecessor to this terminal
	 */
	public abstract double getExpCrit(AbstractTerminal specificPredecessor);
	

	/**
	 * recursively traces the critical path , accumulating all terminals on the critical path in an ordered list (first element is signal source)
	 * @param specificSuccessor the terminal that called this method
	 * @return ordered list containing all terminals on the critical path from critical source up to this terminal
	 */
	public abstract List<AbstractTerminal> traceCriticalPath(AbstractTerminal specificSuccessor);
	
	/**
	 * adds textual information such as name, position, tA and delay to specificSuccessor about this terminal to the given StringBuilder
	 * @param output the StringBuilder to which to append the information
	 * @param specificSuccessor the terminal that called this method, or null if it was called externally
	 */
	public abstract void generateOutput(StringBuilder output, AbstractTerminal specificSuccessor);
}
