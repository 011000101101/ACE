package designAnalyzer.structures.pathElements.blocks;



import java.util.List;

import designAnalyzer.ParameterManager;
import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.pathElements.PathElement;
import designAnalyzer.structures.pathElements.channels.AbstractChannel;
import designAnalyzer.structures.pathElements.channels.ChannelX;
import designAnalyzer.structures.pathElements.channels.ChannelY;

public class LogicBlock extends NetlistBlock {

	/**
	 * true if this element has been analyzed by timing analyzer, false by default
	 */
	protected boolean analyzed= false;
	
	private int blockClass;
	
	private List<PathElement> previous;
	private PathElement next;
	
	//TODO check if needed
	private PathElement criticalPrevious;

	private int slackToNext;
	
	
	
	
	public LogicBlock(String newName, int newAssignedIdentifier) {

		super(newName, newAssignedIdentifier);
		
		/**
		 * save all net assignments to pins
		 */
		pinAssignments= new Net[6];
		
		subblk_1= false;
		
		
		
	}


	private boolean isClocked(){
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
	protected int annotateTA() {

		if(pinAssignments[5] == null) { //is combinatorial block
			int tA= -1;
			for(PathElement p : previous) { // find critical previous node and its tA
				int temp= p.analyzeTA();
				if(temp > tA) {
					tA= temp;
					criticalPrevious= p;
				}
			}
			tA+= parameterManager.T_SWITCH + parameterManager.T_COMB; //always connected to a channel, is combinatorial Block
		}
		else {
			tA= parameterManager.T_FFOUT; //is sequential block, starting point for annotation algorithm
		}
		return tA;
		
	}
	
	@Override
	public int startAnalyzeTA() {

		if(pinAssignments[5] != null) {
			int tA= -1;
			for(PathElement p : previous) {
				int temp= p.analyzeTA();
				if(temp > tA) {
					tA= temp;
					criticalPrevious= p;
				}
			}
			tA+= parameterManager.T_SWITCH + parameterManager.T_FFIN; //always connected to a channel, is sequential Block
			return tA;
		}
		else {
			return -1; //internal node of path(s) running through this combinatorial lgic block, will be ignored in critical path computation
		}
	}
	

	@Override
	protected int annotateTRAndSlack(int criticalPathLength) {

		if(pinAssignments[5] == null) { //is combinatorial block
			int tR= next.analyzeTRAndSlack(criticalPathLength); //next.tR
			int w= next.analyzeTA() - tA; //next.tA already computed -> retrieve, path length is difference to local
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
	public void startAnalyzeTRAndSlack(int criticalPathLength) {

		if(pinAssignments[5] != null) {
			next.analyzeTRAndSlack(criticalPathLength);
		}
		else {
			//do nothing
		}
	}
	
	@Override
	public void addPrevious(PathElement newPrevious) {
		previous.add(newPrevious);
	}
	
	@Override
	public void addNext(PathElement newNext) {
		next= newNext;
	}


	@Override
	public void printCriticalPath(StringBuilder output, int lastTA) {
		
		if(pinAssignments[5] != null) { //is sequential logic block
			
			printThisNode(output, lastTA);
			
			if(lastTA == 0) { //acts as source
				next.printCriticalPath(output, tA);
			}
			
		}
		else { // is combinatorial logic block
			
			printThisNode(output, lastTA);
			next.printCriticalPath(output, tA);
			
		}
		
		
	}


	@Override
	public void getInfo(StringBuilder output) {
		output.append("CLB(");
		output.append((pinAssignments[5] != null) ? "seq" : "comb");
		output.append(")");
		output.append("\t");
		output.append(name);
		output.append("\t");
		output.append("(");
		output.append(xCoordinate);
		output.append(",");
		output.append(yCoordinate);
		output.append(")");
		
	}
	
	
	
	
	
	
}
