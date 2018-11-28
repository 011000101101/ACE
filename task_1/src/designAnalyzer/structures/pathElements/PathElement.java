package designAnalyzer.structures.pathElements;

import designAnalyzer.ParameterManager;

public abstract class PathElement {
	
	/**
	 * if already analyzed, holds the length of the critical path of the subtree represented by this node <br>
	 * every node acts as root of a subtree with arbitrary number (at least 1) of sinks
	 * @see designAnalyzer.structures.pathElements.PathElement#analyzed
	 */
	protected int t= 0;
	
	/**
	 * if already analyzed, holds tA of this node, if not, holds '-1'
	 */
	protected int tA= -1;
	
	/**
	 * if already analyzed, holds tR of this node, if not, holds '-1'
	 */
	protected int tR= -1;
	
	protected int xCoordinate= -1;
	protected int yCoordinate= -1;
	
	protected ParameterManager parameterManager;
	
	public PathElement() {

		parameterManager= ParameterManager.getInstance();
		
	}
	
	
	
	/* alternative critical path computation, not needed
	public abstract int analyzeTiming();
	*/

	/**
	 * prints the critical path of the subnet represented by this PathElement
	 */
	public abstract void printCriticalPath (StringBuilder output, int lastTA);

	/**
	 * prints this node as part of the critical path
	 */
	protected void printThisNode(StringBuilder output, int lastTA) {
		getInfo(output);
		output.append("\t");
		output.append(tA - lastTA);
		output.append("\t");
		output.append(tA);
		output.append(System.getProperty("line.separator"));
	}


	public abstract void getInfo(StringBuilder output);

	/**
	 * returns value of parameter constant stored in the ParameterManager
	 * @return the requested value
	 */
	/* not needed
	public abstract int getTConnectToChannel();
	*/
	
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

	/**
	 * recursively retrieves the Channel matching the given paramenters if present
	 * @param checkXCoordinate x coordinate
	 * @param checkYCoordinate y coordinate
	 * @param checkTrack track number
	 * @param isChanX boolean to indicate if the wanted channel is instanceof ChannelX or ChannelY
	 * @return the wanted Channel or null, if not found
	 */
	public PathElement getBranchingElement(int checkXCoordinate, int checkYCoordinate, int checkTrack, boolean isChanX, boolean isPin, boolean init) {
		
		if(checkIfBranchingPoint(checkXCoordinate, checkYCoordinate, checkTrack, isChanX, isPin)) {
			return this;
		}
		else {
			return searchAllNext(checkXCoordinate, checkYCoordinate, checkTrack, isChanX, isPin, init);
		}
		
	}

	protected abstract PathElement searchAllNext(int checkXCoordinate, int checkYCoordinate, int checkTrack, boolean isChanX, boolean isPin, boolean init);



	/**
	 * checks if this is the Channel defined by coordinates and track number
	 * @param checkXCoordinate xCoordinate
	 * @param checkYCoordinate yCoordinate
	 * @param checkTrack track number
	 * @param isChanX 
	 * @return true if this instanceof Channel and params match with own ones, false else
	 */
	protected abstract boolean checkIfBranchingPoint(int checkXCoordinate, int checkYCoordinate, int checkTrack, boolean isChanX, boolean isPin);

	/**
	 * returns the single signal source of this PathElement, if present, else returns null
	 * @return the PathElement that acts as source to this PathElement
	 */
	/*
	protected abstract PathElement getSingleSource();
	*/

	public int analyzeTA() {
		if(tA != -1) {
			return tA;
		}
		else {
			return annotateTA();
		}
	}
	
	public int analyzeTRAndSlack(int criticalPathLength) {
		if(tR != -1) {
			return tR;
		}
		else {
			return annotateTRAndSlack(criticalPathLength);
		}
	}
	
	/**
	 * annotates tA first for all previous nodes, then for this node
	 * @return
	 */
	protected abstract int annotateTA();
	
	/**
	 * annotates tA first for all next nodes, then for this node
	 * @return
	 */
	protected abstract int annotateTRAndSlack(int criticalPathLength);
	
	/**
	 * inserts directed edge into routing graph by linking a previous node to this node
	 * @param newPrevoius
	 */
	public abstract void addPrevious(PathElement newPrevoius);
	
	/**
	 * inserts directed edge into routing graph by linking a next node to this node
	 * @param newPrevoius
	 */
	public abstract void addNext(PathElement newNext);

	public abstract String getName();

	public abstract PathElement getOrigin();
}
