package designAnalyzer.structures;

public class Net {

	/**
	 * name from netlist file
	 */
	private String name; 
	
	
	/**
	 * unique identifier
	 */
	private int assignedIdentifier; 
	
	/**
	 * source block
	 */
	private NetlistBlock source;
	
	/**
	 * path from source to all destinations
	 */
	private PathElement path;
	
	private boolean isClocknet= false;
	
	public Net(String newName, int newAssignedIdentifier) {
		name= newName;
		assignedIdentifier= newAssignedIdentifier;
	}
	
	public Net(String newName, int newAssignedIdentifier, boolean newIsClocknet) {
		name= newName;
		assignedIdentifier= newAssignedIdentifier;
		isClocknet= newIsClocknet;
	}
}
