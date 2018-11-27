package designAnalyzer.structures.pathElements.blocks;


import static designAnalyzer.ParameterManager.T_IPAD;
import static designAnalyzer.ParameterManager.T_OPAD;
import static designAnalyzer.ParameterManager.T_SWITCH;
import static designAnalyzer.ParameterManager.T_COMB;
import static designAnalyzer.ParameterManager.T_FFIN;
import static designAnalyzer.ParameterManager.T_FFOUT;
import designAnalyzer.ParameterManager;
import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.pathElements.PathElement;
import designAnalyzer.structures.pathElements.channels.AbstractChannel;

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
	
	protected int annotateTA() {

		tA= 0;	//is external input block
		return 0;
		
	}
	
	public int startAnalyzeTA() { //is external output block

		int tA= previous.analyzeTA();
		tA+= T_SWITCH; //always connected to a channel
		return tA;
		
	}
	
	protected int annotateTR(int criticalPathLength) {

		tR= criticalPathLength;	//is external Output block
		return tR;
		
	}
	
	public void startAnalyzeTRAndSlack(int criticalPathLength) { //is external input block

		int tR= next.analyzeTRAndSlack(criticalPathLength); //next.tR
		int w= next.analyzeTA() - tA; //next.tA already computed -> retrieve, path length is difference to local
		int slack= tR - tA - w; //slack of connection from this to p
		slackToNext= slack; //store slack
		tR-= w; //compute local tR
		
	}
	
	public void addPrevious(PathElement newPrevious) {
		previous= newPrevious;
		//TODO check if connectivity has to be verified here
	}
	
	public void addNext(PathElement newNext) {
		next= newNext;
		//TODO check if connectivity has to be verified here
	}
	
}
