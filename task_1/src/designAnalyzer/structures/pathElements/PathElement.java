package designAnalyzer.structures.pathElements;

public abstract class PathElement {
	
	/**
	 * if already analyzed, holds the length of the critical path of the subtree represented by this node <br>
	 * every node acts as root of a subtree with arbitrary number (at least 1) of sinks
	 * @see designAnalyzer.structures.pathElements.PathElement#analyzed
	 */
	protected int t= 0;
	
	protected int xCoordinate= -1;
	protected int yCoordinate= -1;
	
	
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
	
	public abstract boolean isNeighbour(PathElement neighbour);


	/**
	 * standard getter
	 * @return the X coordinate of this block if it has been placed, '-1' if not
	 */
	public int getX() {
		return xCoordinate;
	}



	/**
	 * standard getter
	 * @return the Y coordinate of this block if it has been placed, '-1' if not
	 */
	public int getY() {
		return yCoordinate;
	}
	
	public abstract void setCoordinates(int xCoordinate, int yCoordinate);

}
