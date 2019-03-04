package designAnalyzer.structures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.structures.pathElements.PathElement;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import designAnalyzer.structures.pathElements.pins.IPin;
import placer.Placer;

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
//	private SimplePath[] sinkingPaths;
	
	//for Star+ o(1) update
	private double uix;
	
	private double uiy;
	
	private double vix;
	
	private double viy;
	
	private double wiringCost= 0;

	/**
	 * flag to avoid duplicate wiring cost update
	 */
	private boolean updated= false;

	/**
	 * cache to be able to restore old value if swap was rejected
	 */
	private double uixOld;

	/**
	 * cache to be able to restore old value if swap was rejected
	 */
	private double uiyOld;

	/**
	 * cache to be able to restore old value if swap was rejected
	 */
	private double vixOld;
	
	/**
	 * cache to be able to restore old value if swap was rejected
	 */
	private double viyOld;

	/**
	 * cache to be able to restore old value if swap was rejected
	 */
	private double oldWiringCost;

	private boolean buffersChanged= false;
	
	
	
	
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
	
//	public void linkSinkToPin(NetlistBlock currentBlock, IPin pin) {
//		sinks.put(currentBlock, pin);
//	}


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
				sinks.put(b, iPin);
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
	 * @param exactWireLengths pointer to save exact wire lengths
	 * @return length of the critical path
	 */
	public int annotateTA(List<int[]> exactWireLengths) {
		int criticalLength= -1;
		int pathCounter= 0;
		for(NetlistBlock b : sinks.keySet()) {
			int[] exactWireLengt= new int[9]; //create counter for segments of this (extended) net (combinatorial paths sinking in this net will be computed completely, but only in their sinkng net.
			int temp= b.startAnalyzeTA(sinks.get(b), exactWireLengt);
			if(temp != -1) { // == -1 -> internal node of combinatorial path...
				exactWireLengt[7]= netNumber; //save netNumber
				exactWireLengt[8]= pathCounter;
				exactWireLengths.add(exactWireLengt); //save segment counter
				pathCounter++;
			}
			if(temp > criticalLength) {
				criticalLength= temp;
				criticalSink= b;
			}
		}
		return criticalLength;
	}

	/**
	 * annotates tR and slack on all paths
	 * @param exactWireLengths 
	 */
	public void annotateTRAndSlack(int criticalPathLength, int[] exactWireLengthDummy) {
		source.startAnalyzeTRAndSlack(criticalPathLength, exactWireLengthDummy);
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

	
	public Map<NetlistBlock, IPin> getSinkMap(){
		return sinks;
	}


	public void setIsClockNet(boolean b) {

		isClockNet= b;
		
	}

	/**
	 * returns all blocks this net uses
	 * @return array of blocks
	 */
	private NetlistBlock[] getBlocks() {
		ArrayList<NetlistBlock> returnArray = new ArrayList<NetlistBlock>();
		returnArray.add(source);
		for(NetlistBlock block: sinks.keySet()) {
			returnArray.add(block);
		}
		return returnArray.toArray(new NetlistBlock[0]);
	}
	
	/**
	 * updates tR in all simplePaths the sinks of which are part of this net
	 * @param newTR new value for tR
	 */
//	public void updateTRAllSinks(int newTR) {
//		for(SimplePath p : sinkingPaths) {
//			p.updateTR(newTR);
//		}
//	}
	
	/**
	 * generates all simplePaths sinking in this net and saves them locally
	 * @return 
	 */
//	public List<SimplePath> generateSimplePaths() {
//		List<SimplePath> pathsTemp= new LinkedList<SimplePath>();
//		
//		for(NetlistBlock b : sinks.keySet()) {
//			if(b instanceof IOBlock || ((LogicBlock) b).isClocked()) {
//				createPaths(b, pathsTemp);
//			}
//		}
//		sinkingPaths= pathsTemp.toArray(new SimplePath[0]);
//		return pathsTemp;
//	}

	/**
	 * creates all simplePaths ending in the given sink (a path to a single sink may branch at a combinatorial logic block)
	 * @param pathSink the sink of the sinmplePaths
	 * @param pathsTemp list holding all simplePaths sinking in this net that have already been generated
	 */
//	private void createPaths(NetlistBlock pathSink, List<SimplePath> pathsTemp) {
//		if(source instanceof IOBlock || ((LogicBlock) source).isClocked()) {
//			pathsTemp.add(new SimplePath(source, pathSink, this));
//		}
//		else {
//			for(Net n : StructureManager.getInstance().getNetCollection()) {
//				if(n.getSinks().contains(source)) {
//					List<NetlistBlock> intermediate= new LinkedList<NetlistBlock>();
//					intermediate.add(0, source);
//					n.extendPath(intermediate, pathSink, this, pathsTemp);
//				}
//			}
//		}
//	}

	/**
	 * creates all simplePaths ending in the given remote sink, leading through a sink of this net (a path to a single sink may branch at a combinatorial logic block)
	 * @param intermediate list of all blocks on a the path between source and remoteSink
	 * @param remoteSink the final sink of the path, part of a different net
	 * @param sinkingNet the net that contains remoteSink as a sink
	 * @param pathsTemp list holding all simplePaths sinking in sinkingNet(!) that have already been generated
	 */
//	private void extendPath(List<NetlistBlock> intermediate, NetlistBlock remoteSink, Net sinkingNet,  List<SimplePath> pathsTemp) {
//		if(source instanceof IOBlock || ((LogicBlock) source).isClocked()) {
//			pathsTemp.add(new SimplePath(source, remoteSink, intermediate.toArray(new NetlistBlock[0]), sinkingNet));
//		}
//		else {
//			for(Net n : StructureManager.getInstance().getNetCollection()) {
//				if(n.getSinks().contains(source)) {
//					List<NetlistBlock> intermediateNew= new LinkedList<NetlistBlock>();
//					intermediateNew.addAll(intermediate);
//					intermediateNew.add(0, source);
//					n.extendPath(intermediateNew, remoteSink, sinkingNet, pathsTemp);
//				}
//			}
//		}
//	}
	
	/**
	 * initializes star+ metric value
	 */
	public void initializeWiringCost() {
		NetlistBlock[] currentBlocks;
		uix = 0;
		uiy = 0;
		vix = 0;
		viy = 0;
		currentBlocks = this.getBlocks(); 
		for(NetlistBlock currentSingleBlock: currentBlocks) {
			uix += Math.pow(currentSingleBlock.getX(),2);
			uiy += Math.pow(currentSingleBlock.getY(),2);
			vix += currentSingleBlock.getX();
			viy += currentSingleBlock.getY();
		}
		double wiringCostX = Placer.GAMMA * Math.sqrt(uix - Math.pow(vix, 2)/currentBlocks.length + Placer.PHI);
		double wiringCostY = Placer.GAMMA * Math.sqrt(uiy - Math.pow(viy, 2)/currentBlocks.length + Placer.PHI);
		wiringCost = wiringCostX + wiringCostY;
	}

	/**
	 * standard getter
	 * @return wiring cost of this net
	 */
	public double getWiringCost() {
		return wiringCost;
	}

	/**
	 * update star+ metric values and stores them locally
	 * @param block1
	 * @param block2
	 * @param logicBlockSwap
	 * @return
	 */
	public double update (NetlistBlock block1, NetlistBlock block2, int[] logicBlockSwap) { //TODO vorzeichenfehler bei deltaWiringCost!!11elf
		if(!updated) {
			
			double deltaUix;
			double deltaUiy;
			double deltaVix;
			double deltaViy;
			double wiringCostX;
			double wiringCostY;
			double deltaWiringCost= 0;
			
			if(source.equals(block1) || sinks.containsKey(block1)) {
				deltaUix = Math.pow(logicBlockSwap[3], 2) - Math.pow(block1.getX(), 2);
				deltaUiy = Math.pow(logicBlockSwap[4], 2) - Math.pow(block1.getY(), 2);
				deltaVix = - block1.getX() + logicBlockSwap[3];
				deltaViy = - block1.getY() + logicBlockSwap[4];
				if(!buffersChanged) {
					uixOld= uix;
					uiyOld= uiy;
					vixOld= vix;
					viyOld= viy;
					oldWiringCost = wiringCost;
					buffersChanged= true;
				}
				uix += deltaUix;
				uiy += deltaUiy;
				vix += deltaVix;
				viy += deltaViy;
//				System.out.println("uix " +uix);
//				System.out.println("uiy " +uiy);
//				System.out.println("vix " +vix);
//				System.out.println("uix - Math.pow(vix, 2)/getBlocks().length + Placer.PHI " +(uix - Math.pow(vix, 2)/getBlocks().length + Placer.PHI));
				wiringCostX = Placer.GAMMA * Math.sqrt(uix - Math.pow(vix, 2)/getBlocks().length + Placer.PHI);
				wiringCostY = Placer.GAMMA * Math.sqrt(uiy - Math.pow(viy, 2)/getBlocks().length + Placer.PHI);
//				System.out.println("wiringCostX " +wiringCostX);
//				System.out.println("wiringCostY "+wiringCostY);
				wiringCost = wiringCostX + wiringCostY;
				deltaWiringCost+= (wiringCost - oldWiringCost);
			}
			
			if(block2 instanceof NetlistBlock && (source.equals(block2) || sinks.containsKey(block2))) {
				deltaUix = Math.pow(logicBlockSwap[0], 2) - Math.pow(block2.getX(), 2);
				deltaUiy = Math.pow(logicBlockSwap[1], 2) - Math.pow(block2.getY(), 2);
				deltaVix = - block2.getX() + logicBlockSwap[0];
				deltaViy = - block2.getY() + logicBlockSwap[1];
				if(!buffersChanged) {
					uixOld= uix;
					uiyOld= uiy;
					vixOld= vix;
					viyOld= viy;
					oldWiringCost = wiringCost;
					buffersChanged= true;
				}
				uix += deltaUix;
				uiy += deltaUiy;
				vix += deltaVix;
				viy += deltaViy;
				wiringCostX = Placer.GAMMA * Math.sqrt(uix - Math.pow(vix, 2)/getBlocks().length + Placer.PHI);
				wiringCostY = Placer.GAMMA * Math.sqrt(uiy - Math.pow(viy, 2)/getBlocks().length + Placer.PHI);
				oldWiringCost = wiringCost;
				wiringCost = wiringCostX + wiringCostY;
				deltaWiringCost+= (wiringCost - oldWiringCost);
			}
			
			updated= true;
			return deltaWiringCost;
			
		}
		return 0;
	}


	/**
	 * reset the updated flag to false
	 */
	public void resetUpdatedFlag() {
		updated= false;
		buffersChanged= false;
	}


	/**
	 * reset changed wiring cost values if swap was rejected
	 */
	public void resetWiringCost() {
		uix= uixOld;
		uiy= uiyOld;
		vix= vixOld;
		viy= viyOld;
		wiringCost= oldWiringCost;
	}


	

}

