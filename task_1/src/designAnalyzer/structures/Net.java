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
	 * unique number for debug purposes
	 */
	private int netNumber;
	
	
	/**
	 * unique identifier
	 */
	private int assignedIdentifier; 
	
	/**
	 * source block
	 */
	private NetlistBlock source;

	/**
	 * last parsed path element
	 */
	private PathElement activePathElement;
	
	/**
	 * list of sink Blocks
	 */
	private List<NetlistBlock> sinks;
	
	/**
	 * sink reached by following the critical path
	 */
	private NetlistBlock criticalSink;
	
	/**
	 * path from source to all destinations, excluding source, stored as root of sub-tree
	 */
	private PathElement firstInternalNode;
	
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


	/**
	 * standard getter
	 * @return first node directly after source
	 * @see designAnalyzer.structures.Net#firstInternalNode
	 */
	public PathElement getFirstInternalNode() {
		return firstInternalNode;
	}
	
	/**
	 * computes the critical path length for this net
	 * @return critical path length if this is not a clock net, '-1' else
	 */
	public int beginAnalyzeTiming(){
		if(isClockNet){	//clock nets are ignored during timing analysis
			return -1;	//send negative value so it will be ignored in maximum computation
		}
		else{
			return source.startAnalyzeTiming(); //compute critical path length starting at source and return
		}
		
	}
	
	/**
	 * prints the critical path of this net
	 */
	public void printCriticalPath(){
		source.printCriticalPath();
	}


	/**
	 * standard setter
	 * @param newBlockClass value to be set for variable
	 */
	public void setNetNumber(int newNetNumber) {
		netNumber= newNetNumber;
	}


	/**
	 * standard setter
	 * @param newBlockClass value to be set for variable
	 */
	public void setActivePathElement(PathElement currentPathElement) {
		activePathElement= currentPathElement;
		
	}


	public boolean containsSink(NetlistBlock checkSink) {
		for(NetlistBlock b : sinks){
			if(b.equals(checkSink)){
				return true;
			}
		}
		return false;
	}


	/**
	 * standard getter
	 * @return the requested value
	 */
	public NetlistBlock getSource() {
		return source;
	}


	/**
	 * standard getter
	 * @return the requested value
	 */
	public PathElement getActivePathElement() {
		return activePathElement;
	}
	
	/**
	 * standard setter
	 * @param newBlockClass value to be set for variable
	 */
	public void setCriticalSink(NetlistBlock newCriticalSink){
		criticalSink= newCriticalSink;
	}


	/**
	 * standard getter
	 * @return the requested value
	 */
	public List<NetlistBlock> getSinks() {
		return sinks;
	}


	public boolean recoverCurrentPathElement(int xCoordinate, int yCoordinate, int track, boolean isChanX) {

		activePathElement= activePathElement.getBranchingElement(xCoordinate, yCoordinate , track, isChanX);
		if(activePathElement == null) {
			return false;
		}
		else {
			return true;
		}
		
	}

	/**
	 * annotates tA on all paths and subsequently computes the critical path
	 * @return length of the critical path
	 */
	public int annotateTA() {
		int criticalLength= -1;
		for(NetlistBlock b : sinks) {
			int temp= b.startAnalyzeTA();
			if(temp > criticalLength) {
				criticalLength= temp;
				criticalSink= b;
			}
		}
		return criticalLength;
	}

	/**
	 * annotates tR and slack on all paths
	 */
	public void annotateTRAndSlack(int criticalPathLength) {
		source.startAnalyzeTRAndSlack(criticalPathLength);
	}

/**
 * Standard Getter
 * @return name of net
 */
	public String getName() {
		
		return name;
	}

/**
 * Standard Getter
 * @return number of net
 */
	public int getNumber() {
	
		return netNumber;
	}

	// TODO remove in final version
	/*
	public void setIsClockNet(boolean newIsClockNet) {
		
		isClockNet= newIsClockNet;
		
	}
	*/
}
