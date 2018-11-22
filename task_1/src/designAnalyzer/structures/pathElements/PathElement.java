package designAnalyzer.structures.pathElements;

public abstract class PathElement {
	
	/**
	 * if already analyzed, holds the length of the critical path of the subtree represented by this node <br>
	 * every node acts as root of a subtree with arbitrary number (at least 1) of sinks
	 * @see designAnalyzer.structures.pathElements.PathElement#analyzed
	 */
	protected int t= 0;
	
	
	public abstract int analyzeTiming();

	/**
	 * prints the critical path of the subnet represented by this PathElement
	 */
	public abstract void printCriticalPath();


	/**
	 * returns value of parameter constant stored in the ParameterManager
	 * @return the requested value
	 */
	public abstract int getTConnectToChannel();

}
