package designAnalyzer.structures;

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
	 * saves all net assignments to pins
	 */
	protected Net[] pinAssignments;
	
	protected int xCoordinate;
	protected int yCoordinate;
	
	protected boolean subblock;
	
	/**
	 * internal blocknumber for debugging purposes
	 */
	protected boolean subblk_1; 
	
}
