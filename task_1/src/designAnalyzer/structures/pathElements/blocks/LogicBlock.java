package designAnalyzer.structures.pathElements.blocks;

import designAnalyzer.ParameterManager;
import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.pathElements.PathElement;
import designAnalyzer.structures.pathElements.channels.AbstractChannel;

public class LogicBlock extends NetlistBlock {

	/**
	 * true if this element has been analyzed by timing analyzer, false by default
	 */
	protected boolean analyzed= false;
	
	
	
	
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

	//only called on sink or input "side" of clocked sequential logic blocks and on unclocked combinatorial logic blocks
	@Override
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


	/**
	 * returns value of parameter constant stored in the ParameterManager
	 * @return the requested value
	 */
	private int getTConnectLogicBlockInternal() {
		
		return ParameterManager.T_CONNECT_LOGIC_BLOCK_INTERNAL;
		
	}


	@Override
	public int getTConnectToChannel() {
		
		return ParameterManager.T_CONNECT_CHANNEL_LOGIC_BLOCK;
		
	}
	
	
	
	
	
	
}
