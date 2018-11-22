package designAnalyzer.structures.pathElements.blocks;

import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.StructureManager;

public abstract class NetlistBlock {

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
	 * if IOBlock: <br>
	 * 		pin 0: input pin <br>
	 * 		pin 1: output pin <br>
	 * <br>
	 * if LogicBlock: <br>
	 * 		pin 0 - 3: input pins <br>
	 * 		pin 4: output pin (2 physical pins, but only one connection to a net) <br>
	 * 		pin 5: clock pin <br>
	 */
	protected Net[] pinAssignments;
	
	protected int xCoordinate= -1;
	protected int yCoordinate= -1;
	
	
	/**
	 * internal blocknumber for debugging purposes
	 */
	/*protected boolean subblock;*/
	
	
	/**
	 * subblock-number: false = 0, true = 1
	 */
	protected boolean subblk_1; 
	
	
	
	public NetlistBlock(String newName, int newAssignedIdentifier) {
		name= newName;
		assignedIdentifier= newAssignedIdentifier;
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
		
		StructureManager.getInstance().insertIntoBlockIndexingStructure(xCoordinate, yCoordinate, this);
		
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
	 * standard getter
	 * @return the name of this block
	 */
	public String getName() {
		return name;
	}


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
	
}
