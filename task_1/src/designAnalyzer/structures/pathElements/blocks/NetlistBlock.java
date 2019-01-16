package designAnalyzer.structures.pathElements.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.SimplePath;
import designAnalyzer.structures.StructureManager;
import designAnalyzer.structures.pathElements.PathElement;
import designAnalyzer.structures.pathElements.channels.ChannelX;
import designAnalyzer.structures.pathElements.channels.ChannelY;
import designAnalyzer.structures.pathElements.pins.IPin;
import designAnalyzer.structures.pathElements.pins.OPin;

/**
 * 
 * @author Dennis Grotz, Rumei Ma, Vincenz Mechler
 *
 */
public abstract class NetlistBlock extends PathElement{

	/**
	 * name from netlist file
	 */
	protected String name;
	
	/**
	 * unique identifier
	 */
	protected int assignedIdentifier;
	
	/**
	 * saves net assignments to pins <br>
	 * <br>
	 * <b>if IOBlock:</b> <br>
	 * 		pin 0: input pin <br>
	 * 		pin 1: output pin <br>
	 * 		<b>constraint:</b> (pinAssignments[0] == null || pinAssignments[1] == null)<br>
	 * <br>
	 * <b>if LogicBlock:</b> <br>
	 * 		pin 0 - 3: input pins <br>
	 * 		pin 4: output pin (2 physical pins, but only one connection to a net) <br>
	 * 		pin 5: clock pin <br>
	 */
	protected Net[] pinAssignments;
	
	
	/**
	 * internal blocknumber for debugging purposes
	 */
	protected int blockNumber;
	
	
	/**
	 * subblock-number: false = 0, true = 1
	 */
	protected boolean subblk_1;

	/**
	 * list holding all connected paths
	 */
	private List<SimplePath> connectedPaths= new LinkedList<SimplePath>(); 
	
	
	
	public NetlistBlock(String newName, int newAssignedIdentifier) {
		name= newName;
		assignedIdentifier= newAssignedIdentifier;
	}
	
	public void setBlockNumber(int newInt) {
		blockNumber = newInt;
	}
	
	/**
	 * standard setter method
	 * @param newVal
	 */
	public void setSubblk_1(boolean newVal) {
		subblk_1= newVal;
	}
	
	/**
	 * standard setter method
	 * @param newXCoordinate
	 */
	/* TODO remove in final version
	public void setXCoordinate(int newXCoordinate) {
		xCoordinate= newXCoordinate;
	}
	*/

	/**
	 * standard setter method
	 * @param newYCoordinate
	 */
	/* TODO remove in final version
	public void setYCoordinate(int newYCoordinate) {
		yCoordinate= newYCoordinate;
	}
	*/
	
	/**
	 * sets X and Y coordinate of this block <br>
	 * and adds it to an indexing structure allowing for efficient lookup by coordinates
	 * @param newXCoordinate the X coordinate of this block
	 * @param newYCoordinate the Y coordinate of this block
	 */
	public void setCoordinates(int newXCoordinate, int newYCoordinate) {
		xCoordinate= newXCoordinate;
		yCoordinate= newYCoordinate;
		
		StructureManager.getInstance().insertIntoBlockIndexingStructure(xCoordinate, yCoordinate, (subblk_1 ? 1 : 0), this);
		
	}
	
	/**
	 * sets X and Y coordinate of this block without checking for coordinates already in use <br>
	 * @param newXCoordinate the X coordinate of this block
	 * @param newYCoordinate the Y coordinate of this block
	 */
	public void updateCoordinates(int newXCoordinate, int newYCoordinate) {
		xCoordinate= newXCoordinate;
		yCoordinate= newYCoordinate;
		
		
	}
	
	public void connect(Net netToConnect, int pinNumber) {
		if(pinAssignments[pinNumber] == null) {
			pinAssignments[pinNumber]= netToConnect;
		}
		else {
			ErrorReporter.reportDuplicatePinAssignmentError(this, pinNumber, netToConnect);
		}
	}
	
	
	/**
	 * method to start timing analysis on the source node of a net
	 * @return
	 */
	/* alternative critical path computation, not needed
	public abstract int startAnalyzeTiming();
	*/


	/**
	 * standard getter
	 * @return the name of this block
	 */
	public String getName() {
		return name;
	}
	
	public boolean getSubblk_1(){
		return subblk_1;
	}
	
	@Override
	protected boolean checkIfBranchingPoint(int checkXCoordinate, int checkYCoordinate, int checkTrack, boolean isChanX, boolean isPin) {
		return false;
	}
	
	public int getBlockNumber() {
		return blockNumber;
	}
	public abstract int startAnalyzeTA(PathElement iPin);
	
	public abstract void startAnalyzeTRAndSlack(int criticalPathLength);

	/**
	 * checks if the inpin or outpin is a neighbour of this block <br>
	 * pinNumber not relevant
	 */
	@Override
	public boolean isNeighbour(PathElement neighbour) {
		
		if(neighbour instanceof IPin || neighbour instanceof OPin) {
			
			return ( ( xCoordinate == neighbour.getX() ) && ( yCoordinate == neighbour.getY() ));
			
		}
		else {
			return false;
		}
	}
	
	public abstract PathElement getOriginInit(IPin pin);

	/**
	 * marks all connected paths as changed
	 */
	public void setChanged() {
		
		for(SimplePath p : connectedPaths) {
			p.setChanged();
		}
		
	}
	
	/**
	 * adds a path to the list of connected paths
	 * @param p the path to add
	 */
	public void addPath(SimplePath p) {
		connectedPaths.add(p);
	}
	
	/**
	 * standard getter
	 * @return
	 */
	public List<SimplePath> getConnectedPaths(){
		return connectedPaths;
	}

	/**
	 * returns an array of connected nets without clock net
	 * @return array of connected nets
	 */
	public Net[] getNet() {
		ArrayList<Net> returnArray = new ArrayList<Net>();
		for(int i = 0; i <= pinAssignments.length - 2; i++) {
			if(pinAssignments[i] != null && !pinAssignments[i].getIsClocknNet()) {
				returnArray.add(pinAssignments[i]);
			}
		}
		return returnArray.toArray(new Net[0]);
	}

	
}
