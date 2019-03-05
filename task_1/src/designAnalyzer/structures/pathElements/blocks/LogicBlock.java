package designAnalyzer.structures.pathElements.blocks;



import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import designAnalyzer.abstractedTimingGraph.PassTerminal;
import designAnalyzer.abstractedTimingGraph.SinkTerminal;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.pathElements.PathElement;
import designAnalyzer.structures.pathElements.pins.IPin;

/**
 * 
 * @author Dennis Grotz, Rumei Ma, Vincenz Mechler
 *
 */
public class LogicBlock extends NetlistBlock {

	/**
	 * true if this element has been analyzed by timing analyzer, false by default
	 */
	protected boolean analyzed= false;
	
	private int blockClass;
	
	private List<IPin> previous= new LinkedList<IPin>();
	private PathElement next;
	
	
	private PathElement criticalPrevious;

	@SuppressWarnings("unused")
	private int slackToNext;
	
	int[] tA2= new int[4];

	private PassTerminal passTerminal;

	private SinkTerminal[] sinkTerminals;
	
	
	
	
	public LogicBlock(String newName, int newAssignedIdentifier) {

		super(newName, newAssignedIdentifier);
		
		/**
		 * save all net assignments to pins
		 */
		pinAssignments= new Net[6];
		
		subblk_1= false;
		
		passTerminal= null;
		sinkTerminals= new SinkTerminal[4];
		
	}

	/**
	 * gives info if this block is combinatorial or sequential
	 * @return true if sequential, false if combinatorial
	 */
	public boolean isClocked(){
		if(pinAssignments[5] == null){
			return false;
		}
		else{
			return true;
		}
	}

	//only called on source or output "side" of logic blocks (clocked and unclocked)
	/* alternative critical path computation, not needed
	@Override
	public int startAnalyzeTiming() {
		
		PathElement nextNode= pinAssignments[1].getFirstInternalNode(); //save next node for reuse
		
		if(nextNode instanceof AbstractChannel){//LogicBlock is always directly connected to a channel, not a block
			return nextNode.analyzeTiming() + ((AbstractChannel) nextNode).getTConnectToLogicBlock(); 
		}
		else{
			ErrorReporter.reportInvalidRoutingError(this, nextNode);
			return 0;
		}
	}
	*/

	//only called on sink or input "side" of clocked sequential logic blocks and on unclocked combinatorial logic blocks
	//@Override
	/* alternative critical path computation, not needed
	public int analyzeTiming() {
		
		if(isClocked()){ //is clocked block, critical path length to all sinks is 0 (this is the only sink)
			return 0;
		}
		else if(analyzed){ //is combinatorial logic block, already analyzed, return known critical path length
			return t;
		}
		else{ //is combinatorial logic block, recursively compute critical path length to all sinks of next net
			t= this.startAnalyzeTiming() + this.getTConnectLogicBlockInternal();
			return t;
		}
		
	}
	*/


	/**
	 * returns value of parameter constant stored in the ParameterManager
	 * @return the requested value
	 */
	/* not needed
	private int getTConnectLogicBlockInternal() {
		
		return ParameterManager.T_CONNECT_LOGIC_BLOCK_INTERNAL;
		
	}
	*/


	//@Override
	/* not needed
	public int getTConnectToChannel() {
		
		return ParameterManager.T_CONNECT_CHANNEL_LOGIC_BLOCK;
		
	}
	*/


	/**
	 * standard setter
	 * @param newBlockClass value to be set for variable
	 */
	public void setClass(Integer newBlockClass) {
		blockClass= newBlockClass;
	}


	public Integer getBlockClass() {
		return blockClass;
	}
	
	@Override
	protected int annotateTA(int[] exactWireLengt) {

		if(pinAssignments[5] == null) { //is combinatorial block
			tA= -1;
			for(PathElement p : previous) { // find critical previous node and its tA
				int temp= p.analyzeTA(exactWireLengt);
				if(temp > tA) {
					tA= temp;
					criticalPrevious= p;
				}
			}
			tA+= parameterManager.T_COMB; //always connected to a IPIN, is combinatorial Block
			exactWireLengt[4]= exactWireLengt[4] + 1; //add COMB segment
		}
		else {
			tA= 0; //is sequential block, starting point for annotation algorithm
		}
		return tA;
		
	}
	
	@Override
	public int startAnalyzeTA(PathElement iPin, int[] exactWireLengt) {

		
		if(pinAssignments[5] != null) {
			
			for(IPin p : previous) {
				int i = previous.indexOf(p);
				if(p.equals(iPin)) {
					
					
					int temp= p.analyzeTA(exactWireLengt);
				
					tA2[i]= temp;
					
					tA2[i]+= parameterManager.T_FFIN; //always connected to a IPIN, is sequential Block
					exactWireLengt[5]= exactWireLengt[5] + 1; //add a FFIN segment
					return tA2[i];
					
				}
					
			}
		}
		else {
			return -1; //internal node of path(s) running through this combinatorial lgic block, will be ignored in critical path computation
		}
		return -1;
	}
	

	@Override
	protected int annotateTRAndSlack(int criticalPathLength, int[] exactWireLengthDummy) {

		if(pinAssignments[5] == null) { //is combinatorial block
			tR= next.analyzeTRAndSlack(criticalPathLength, exactWireLengthDummy); //next.tR
			int w= next.analyzeTA(exactWireLengthDummy) - tA; //next.tA already computed -> retrieve, path length is difference to local
			int slack= tR - tA - w; //slack of connection from this to p
			slackToNext= slack; //store slack
			tR-= w; //compute local tR
		}
		else {
			tR= criticalPathLength; //is sequential block, starting point for annotation algorithm
		}
		return tR;
	}
		
	
	@Override
	public void startAnalyzeTRAndSlack(int criticalPathLength, int[] exactWireLengthDummy) {

		if(pinAssignments[5] != null) {
			next.analyzeTRAndSlack(criticalPathLength, exactWireLengthDummy);
		}
		else {
			//do nothing
		}
	}
	
	@Override
	public void addPrevious(PathElement newPrevious) {
		previous.add((IPin) newPrevious);
	}
	
	@Override
	public void addNext(PathElement newNext) {
		next= newNext;
	}


	@Override
	public void printCriticalPath(StringBuilder output, int lastTA) {
		
		if(pinAssignments[5] != null) { //is sequential logic block
			
			if(lastTA == 0) {
				printThisNode(output, lastTA);
				next.printCriticalPath(output, tA);
			}
			else {
				printThisNodeFinal(output, lastTA);
			}
			
		}
		else { // is combinatorial logic block
			
			printThisNode(output, lastTA);
			next.printCriticalPath(output, tA);
			
		}
		
		
	}


	private void printThisNodeFinal(StringBuilder output, int lastTA) {
		output.append("CLB(");
		output.append((pinAssignments[5] != null) ? "seq" : "comb");
		output.append(")");
		output.append("\t");
		output.append("|");
		output.append(name);
		output.append("\t");

		output.append("|");
		output.append("(");
		output.append(xCoordinate);
		output.append(",");
		output.append(yCoordinate);
		output.append(")");

		output.append("\t");
		output.append("|");
		output.append(parameterManager.T_FFIN);
		output.append("\t");
		output.append("|");
		output.append(lastTA + parameterManager.T_FFIN);
		output.append(System.getProperty("line.separator"));
	}


	@Override
	public void getInfo(StringBuilder output) {
		output.append("CLB(");
		output.append((pinAssignments[5] != null) ? "seq" : "comb");
		output.append(")");
		output.append("\t");
		output.append("|");
		output.append(name);
		output.append("\t");

		output.append("|");
		output.append("(");
		output.append(xCoordinate);
		output.append(",");
		output.append(yCoordinate);
		output.append(")");
		
	}



	
	@Override
	protected PathElement searchAllNext(int checkXCoordinate, int checkYCoordinate, int checkTrack, boolean isChanX, boolean isPin, boolean init) {
		
		if(init) { //is input block
			return next.getBranchingElement(checkXCoordinate, checkYCoordinate, checkTrack, isChanX, isPin, false);
		}
		else {
			return null;
		}
	}


	public boolean isSequential() {
		return (pinAssignments[5] != null);
	}
	

	
	@Override
	public PathElement getOrigin() {
		if(pinAssignments[5] == null) { //is combinatorial
			return criticalPrevious.getOrigin();
		}
		else {
			return this; //origin node of critical path
		}
	}


	@Override
	public PathElement getOriginInit(IPin pin) {
		
		for(IPin p : previous) {
			int i = previous.indexOf(p);
			return previous.get(i).getOrigin();
		}
		return null;
		
	}
	
	@Override
	public Net[] getNet() {
		ArrayList<Net> returnArray = new ArrayList<Net>();
		for(int i = 0; i <= pinAssignments.length - 2; i++) {
			if(pinAssignments[i] != null && !pinAssignments[i].getIsClocknNet()) {
				returnArray.add(pinAssignments[i]);
			}
		}
		return returnArray.toArray(new Net[0]);
	}

	@Override
	public int getSignalEntryDelay() {
		return parameterManager.T_FFOUT;
	}

	@Override
	public int getSignalExitDelay() {
		return parameterManager.T_FFIN;
	}

	@Override
	public int getSignalPassDelay() {
		return parameterManager.T_COMB;
	}

	public void setPassTerminal(PassTerminal newPassTerminal) {
		passTerminal= newPassTerminal;
	}

	public PassTerminal getPassTerminal() {
		return passTerminal;
	}

	@Override
	public void addSinkTerminal(SinkTerminal newSinkTerminal) {
		for(int j= 0; j < 4; j++) { //store in first free location
			if(sinkTerminals[j] == null) {
				sinkTerminals[j]= newSinkTerminal;
				break;
			}
		}
	}
	
	public SinkTerminal[] getSinkTerminals() {
		return sinkTerminals;
	}
	
	
	
	
	
}
