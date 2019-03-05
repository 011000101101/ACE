package designAnalyzer.structures.pathElements;

import designAnalyzer.ParameterManager;

/**
 * 
 * @author Dennis Grotz, Rumei Ma, Vincenz Mechler
 *
 */
public abstract class PathElement {
	
	/**
	 * if already analyzed, holds the length of the critical path of the subtree represented by this node <br>
	 * every node acts as root of a subtree with arbitrary number (at least 1) of sinks
	 * @see designAnalyzer.structures.pathElements.PathElement#analyzed
	 */
	protected int t;
	
	/**
	 * if already analyzed, holds tA of this node, if not, holds '-1'
	 */
	protected int tA;
	
	/**
	 * if already analyzed, holds tR of this node, if not, holds '-1'
	 */
	protected int tR;
	
	protected int xCoordinate;
	protected int yCoordinate;
	
	protected ParameterManager parameterManager;
	
	public PathElement() {

		t= 0;
		tA= -1;
		tR= -1;
		xCoordinate= -1;
		yCoordinate= -1;
		parameterManager= ParameterManager.getInstance();
		
	}

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
		output.append("|");
		output.append(tA - lastTA);
		output.append("\t");
		output.append("|");
		output.append(tA);
		output.append(System.getProperty("line.separator"));
		
	}
	
	public abstract void getInfo(StringBuilder output);
	
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

	public int analyzeTA(int[] exactWireLengt) {
		if(tA != -1) {
			return tA;
		}
		else {
			return annotateTA(exactWireLengt);
		}
	}
	
	public int analyzeTRAndSlack(int criticalPathLength, int[] exactWireLengthDummy) {
		if(tR != -1) {
			return tR;
		}
		else {
			return annotateTRAndSlack(criticalPathLength, exactWireLengthDummy);
		}
	}
	
	/**
	 * annotates tA first for all previous nodes, then for this node
	 * @param exactWireLengt 
	 * @return
	 */
	protected abstract int annotateTA(int[] exactWireLengt);
	
	/**
	 * annotates tA first for all next nodes, then for this node
	 * @return
	 */
	protected abstract int annotateTRAndSlack(int criticalPathLength, int[] exactWireLengthDummy);
	
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
