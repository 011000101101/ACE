package designAnalyzer.structures.blocks;

import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.structures.nets.Net;

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
	
	protected int xCoordinate;
	protected int yCoordinate;
	
	
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
	public void setXCoordinate(int newXCoordinate) {
		xCoordinate= newXCoordinate;
	}

	/**
	 * standard setter method
	 * @param newYCoordinate
	 */
	public void setYCoordinate(int newYCoordinate) {
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
	 * standard getter
	 * @return the name of this block
	 */
	public String getName() {
		return name;
	}
	
}
