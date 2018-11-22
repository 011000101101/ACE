package designAnalyzer.structures.pathElements.blocks;

import designAnalyzer.ParameterManager;
import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.pathElements.PathElement;
import designAnalyzer.structures.pathElements.channels.AbstractChannel;

public class IOBlock extends NetlistBlock {

	 
	
	public IOBlock(String newName, int newAssignedIdentifier) {
		
		super(newName, newAssignedIdentifier);
		
		pinAssignments= new Net[2];
		
	}

	//only called on input blocks
	@Override
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

	//only called on output blocks
	@Override
	public int analyzeTiming() {
		
		return 0; //is output block, critical path length to all sinks is 0 (this is the only sink)
		
	}

	@Override
	public int getTConnectToChannel() {
		
		return ParameterManager.T_CONNECT_CHANNEL_IOBLOCK;
		
	}
	
}
