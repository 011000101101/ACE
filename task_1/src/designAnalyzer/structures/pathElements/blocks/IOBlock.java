package designAnalyzer.structures.pathElements.blocks;



import designAnalyzer.structures.Net;
import designAnalyzer.structures.pathElements.PathElement;

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
	private int slackToNext;
	 
	
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
	protected int annotateTA() {

		tA= 0;	//is external input block
		return 0;
		
	}
	
	@Override
	public int startAnalyzeTA() { //is external output block

		tA= previous.analyzeTA();
		tA+= parameterManager.T_OPAD; //always connected to a IPIN
		return tA;
		
	}
	
	@Override
	protected int annotateTRAndSlack(int criticalPathLength) {

		tR= criticalPathLength;	//is external Output block
		return tR;
		
	}
	
	@Override
	public void startAnalyzeTRAndSlack(int criticalPathLength) { //is external input block

		int tR= next.analyzeTRAndSlack(criticalPathLength); //next.tR
		int w= next.analyzeTA() - tA; //next.tA already computed -> retrieve, path length is difference to local
		int slack= tR - tA - w; //slack of connection from this to p
		slackToNext= slack; //store slack
		tR-= w; //compute local tR
		
	}
	
	@Override
	public void addPrevious(PathElement newPrevious) {
		previous= newPrevious;
		//TODO check if connectivity has to be verified here
	}
	
	@Override
	public void addNext(PathElement newNext) {
		next= newNext;
		//TODO check if connectivity has to be verified here
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
		output.append((pinAssignments[1] != null) ? "I_Block" : "O_BLOCK");
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
	public PathElement getOriginInit() {

		return previous.getOrigin();
		
	}
	
}
