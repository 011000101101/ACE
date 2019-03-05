package designAnalyzer.structures.pathElements.blocks;



import java.util.ArrayList;
import designAnalyzer.abstractedTimingGraph.SinkTerminal;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.pathElements.PathElement;
import designAnalyzer.structures.pathElements.pins.IPin;

/**
 * 
 * @author Dennis Grotz, Rumei Ma, Vincenz Mechler
 *
 */
public class IOBlock extends NetlistBlock {

	/**
	 * previous node (in signal flow direction)
	 */
	private PathElement previous;
	
	/**
	 * next node (in signal flow direction)
	 */
	private PathElement next;
	
	/**
	 * slack of connection to next node
	 */
	@SuppressWarnings("unused")
	private int slackToNext;

	private SinkTerminal sinkTerminal;
	 
	
	public IOBlock(String newName, int newAssignedIdentifier) {
		
		super(newName, newAssignedIdentifier);
		
		pinAssignments= new Net[2];
		
	}

	//only called on input blocks
	//@Override
	/* alternative critical path computation, not needed
	public int startAnalyzeTiming() {
		
		PathElement nextNode= pinAssignments[1].getFirstInternalNode(); //save next node for reuse
		
		if(nextNode instanceof AbstractChannel){//IOBlock is always directly connected to a channel, not a block
			return nextNode.analyzeTiming() + ((AbstractChannel) nextNode).getTConnectToIOBlock(); //compute critical path length and return
		}
		else{
			ErrorReporter.reportInvalidRoutingError(this, nextNode); //report error if connected PathElement is not a channel
			return 0;
		}
		
	}
	*/

	//only called on output blocks
	//@Override
	/* alternative critical path computation, not needed
	public int analyzeTiming() {
		
		return 0; //is output block, critical path length to all sinks is 0 (this is the only sink)
		
	}
	*/

	//@Override
	/* not needed
	public int getTConnectToChannel() {
		
		return ParameterManager.T_CONNECT_CHANNEL_IOBLOCK;
		
	}
	*/
	
	@Override
	protected int annotateTA(int[] exactWireLengt) {

		tA= 0;	//is external input block
		return 0;
		
	}
	
	@Override
	public int startAnalyzeTA(PathElement iPin, int[] exactWireLengt) { //is external output block

		
		tA= previous.analyzeTA(exactWireLengt);
		tA+= parameterManager.T_OPAD; //always connected to a IPIN
		exactWireLengt[2]= exactWireLengt[2] + 1; //add a OPAD element
		return tA;
		
	}
	
	@Override
	protected int annotateTRAndSlack(int criticalPathLength, int[] exactWireLengthDummy) {

		tR= criticalPathLength;	//is external Output block
		return tR;
		
	}
	
	@Override
	public void startAnalyzeTRAndSlack(int criticalPathLength, int[] exactWireLengthDummy) { //is external input block

		int tR= next.analyzeTRAndSlack(criticalPathLength, exactWireLengthDummy); //next.tR
		int w= next.analyzeTA(exactWireLengthDummy) - tA; //next.tA already computed -> retrieve, path length is difference to local
		int slack= tR - tA - w; //slack of connection from this to p
		slackToNext= slack; //store slack
		tR-= w; //compute local tR
		
	}
	
	@Override
	public void addPrevious(PathElement newPrevious) {
		previous= newPrevious;
		
	}
	
	@Override
	public void addNext(PathElement newNext) {
		next= newNext;
		
	}

	@Override
	public void printCriticalPath(StringBuilder output, int lastTA) {
		
		printThisNode(output, lastTA);
		if(pinAssignments[1] != null) { //is input block
			next.printCriticalPath(output, tA);
		}
		
	}

	@Override
	public void getInfo(StringBuilder output) {
		output.append((pinAssignments[1] != null) ? "In_Block" : "Out_Block");
		output.append("\t");
		output.append("|");
		output.append(name);
		output.append("\t");
		output.append("|");
		output.append("(");
		output.append(xCoordinate);
		output.append(",");
		output.append(yCoordinate);
		output.append(").");
		output.append(subblk_1 ? 1 : 0);
		
		
	}

	/*
	@Override
	protected PathElement getSingleSource() {

		if(pinAssignments[1] != null) { //is input block
			return null;
		}
		else {
			return previous;
		}
		
	}*/
	

	@Override
	protected PathElement searchAllNext(int checkXCoordinate, int checkYCoordinate, int checkTrack, boolean isChanX, boolean isPin, boolean init) {
		
		if(init) { //is input block
			return next.getBranchingElement(checkXCoordinate, checkYCoordinate, checkTrack, isChanX, isPin, false);
		}
		else {
			return null;
		}
	}
	
	/**
	 * tells us where the IOBlock is placed
	 * @return 0 for upper boundary, 1 for right, 2 for lower and 3 for left 
	 */
	public int getBoundary() {
		 
		if(parameterManager.Y_GRID_SIZE + 1 == this.getY()) { //top
			return 0;
		} 
		else if(this.getX() == parameterManager.X_GRID_SIZE +1) {
			return 1;
		}
		else if (this.getY() == 0) {
			return 2;
		}
		else if (this.getX() == 0) {
			return 3;
		}
		else {
			return -1;
		}
		
	}
	

	
	@Override
	public PathElement getOrigin() {

		return this; //origin node of critical path
		
	}


	@Override
	public PathElement getOriginInit(IPin pin) {

		return previous.getOrigin();
		
	}
	
	@Override
	public Net[] getNet() {
		ArrayList<Net> returnArray = new ArrayList<Net>();
		for(int i = 0; i < pinAssignments.length; i++) {
			if(pinAssignments[i] != null && !pinAssignments[i].getIsClocknNet()) {
				returnArray.add(pinAssignments[i]);
			}
		}
		return returnArray.toArray(new Net[0]);
	}

	@Override
	public int getSignalEntryDelay() {
		return parameterManager.T_IPAD;
	}

	@Override
	public int getSignalExitDelay() {
		return parameterManager.T_OPAD;
	}

	@Override
	public int getSignalPassDelay() {
		return -1;
	}

	@Override
	public void addSinkTerminal(SinkTerminal newSinkTerminal) {
		sinkTerminal= newSinkTerminal;
	}
	
	public SinkTerminal getSinkTerminal() {
		return sinkTerminal;
	}
	
}
