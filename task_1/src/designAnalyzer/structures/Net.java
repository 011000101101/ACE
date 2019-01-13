package designAnalyzer.structures;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.structures.pathElements.PathElement;
import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.LogicBlock;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import designAnalyzer.structures.pathElements.pins.IPin;

/**
 * 
 * @author Dennis Grotz, Rumei Ma, Vincenz Mechler
 *
 */
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
	 * list of sink Blocks with additional flag if it has already been routed
	 */
	private Map<NetlistBlock, IPin> sinks;
	
	
	/**
	 * sink reached by following the critical path
	 */
	private NetlistBlock criticalSink;
	
	/**
	 * path from source to all destinations, excluding source, stored as root of sub-tree
	 */
	private PathElement firstInternalNode;
	
	private boolean isClockNet= false;
	
	/**
	 * holds all simplePaths the sinks of hich are part of this net
	 */
	private SimplePath[] sinkingPaths;
	
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
		sinks= new HashMap<NetlistBlock, IPin>();
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
		sinks.put(currentBlock, null);
		
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
	/* different implementation of critical path discovery, no longer needed
	public int beginAnalyzeTiming(){
		if(isClockNet){	//clock nets are ignored during timing analysis
			return -1;	//send negative value so it will be ignored in maximum computation
		}
		else{
			return source.startAnalyzeTiming(); //compute critical path length starting at source and return
		}
		
	}*/
	
	/**
	 * prints the critical path of this net
	 */
	public void printCriticalPath(StringBuilder output){
		criticalSink.getOriginInit(sinks.get(criticalSink)).printCriticalPath(output, 0);
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


	public boolean containsSink(NetlistBlock checkSink, IPin iPin) {
		for(NetlistBlock b : sinks.keySet()){
			if(b.equals(checkSink)){
				sinks.replace(b, iPin);
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
	public Collection<NetlistBlock> getSinks() {
		return sinks.keySet();
	}


	public boolean recoverCurrentPathElement(int xCoordinate, int yCoordinate, int track, boolean isChanX, boolean isPin) {

		activePathElement= source.getBranchingElement(xCoordinate, yCoordinate , track, isChanX, isPin, true);
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
		for(NetlistBlock b : sinks.keySet()) {
			int temp= b.startAnalyzeTA(sinks.get(b));
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

	public NetlistBlock getCriticalSink() {
		return criticalSink;
	}

	// TODO remove in final version
	/*
	public void setIsClockNet(boolean newIsClockNet) {
		
		isClockNet= newIsClockNet;
		
	}
	*/
	
	public Map<NetlistBlock, IPin> getSinkMap(){
		return sinks;
	}


	public void setIsClockNet(boolean b) {

		isClockNet= b;
		
	}


	public NetlistBlock[] getBlocks() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * updates tR in all simplePaths the sinks of which are part of this net
	 * @param newTR new value for tR
	 */
	public void updateTRAllSinks(int newTR) {
		for(SimplePath p : sinkingPaths) {
			p.updateTR(newTR);
		}
	}
	
	/**
	 * generates all simplePaths sinking in this net and saves them locally
	 * @return 
	 */
	public List<SimplePath> generateSimplePaths() {
		List<SimplePath> pathsTemp= new LinkedList<SimplePath>();
		
		for(NetlistBlock b : sinks.keySet()) {
			if(b instanceof IOBlock || ((LogicBlock) b).isClocked()) {
				createPaths(b, pathsTemp);
			}
		}
		sinkingPaths= pathsTemp.toArray(new SimplePath[0]);
		return pathsTemp;
	}

	/**
	 * creates all simplePaths ending in the given sink (a path to a single sink may branch at a combinatorial logic block)
	 * @param pathSink the sink of the sinmplePaths
	 * @param pathsTemp list holding all simplePaths sinking in this net that have already been generated
	 */
	private void createPaths(NetlistBlock pathSink, List<SimplePath> pathsTemp) {
		if(source instanceof IOBlock || ((LogicBlock) source).isClocked()) {
			pathsTemp.add(new SimplePath(source, pathSink, this));
		}
		else {
			for(Net n : StructureManager.getInstance().getNetCollection()) {
				if(n.getSinks().contains(source)) {
					List<NetlistBlock> intermediate= new LinkedList<NetlistBlock>();
					intermediate.add(0, source);
					n.extendPath(intermediate, pathSink, this, pathsTemp);
				}
			}
		}
	}

	/**
	 * creates all simplePaths ending in the given remote sink, leading through a sink of this net (a path to a single sink may branch at a combinatorial logic block)
	 * @param intermediate list of all blocks on a the path between source and remoteSink
	 * @param remoteSink the final sink of the path, part of a different net
	 * @param sinkingNet the net that contains remoteSink as a sink
	 * @param pathsTemp list holding all simplePaths sinking in sinkingNet(!) that have already been generated
	 */
	private void extendPath(List<NetlistBlock> intermediate, NetlistBlock remoteSink, Net sinkingNet,  List<SimplePath> pathsTemp) {
		if(source instanceof IOBlock || ((LogicBlock) source).isClocked()) {
			pathsTemp.add(new SimplePath(source, remoteSink, intermediate.toArray(new NetlistBlock[0]), sinkingNet));
		}
		else {
			for(Net n : StructureManager.getInstance().getNetCollection()) {
				if(n.getSinks().contains(source)) {
					intermediate.add(source);
					n.extendPath(intermediate, remoteSink, sinkingNet, pathsTemp);
				}
			}
		}
	}
}
