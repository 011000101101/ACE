package designAnalyzer.structures;

import java.util.LinkedList;
import java.util.List;

import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.structures.pathElements.PathElement;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

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
	 * list of sink Blocks
	 */
	private List<NetlistBlock> sinks;
	
	/**
	 * path from source to all destinations
	 */
	private PathElement path;
	
	private boolean isClockNet= false;
	
	// TODO remove in final version
	/*
	public Net(String newName, int newAssignedIdentifier) {
		name= newName;
		assignedIdentifier= newAssignedIdentifier;
	}
	*/
	
	
	public Net(String newName, int newAssignedIdentifier, boolean newIsClockNet) {
		name= newName;
		assignedIdentifier= newAssignedIdentifier;
		isClockNet= newIsClockNet;
		
		source= null;
		sinks= new LinkedList<NetlistBlock>();
	}


	/**
	 * standard getter
	 * @return if this net is a clock net 
	 */
	public boolean getIsClocknNet() {
		return isClockNet;
	}


	/**
	 * sets source block if it source was empty before, else reports a duplicateSourceError
	 * @param currentBlock block to be set as source of this Net (each block only possesses one output pin)
	 */
	public void setSource(NetlistBlock currentBlock) {

		if(source == null) {
			source= currentBlock;
		}
		else {
			ErrorReporter.reportDuplicateSourceError(this, currentBlock);
		}
		
	}


	public void addSink(NetlistBlock currentBlock) {
		//TODO revisit and check if sufficient
		sinks.add(currentBlock);
		
	}

	// TODO remove in final version
	/*
	public void setIsClockNet(boolean newIsClockNet) {
		
		isClockNet= newIsClockNet;
		
	}
	*/
}
