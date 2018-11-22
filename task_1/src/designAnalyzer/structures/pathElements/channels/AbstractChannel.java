package designAnalyzer.structures.pathElements.channels;

import designAnalyzer.ParameterManager;
import designAnalyzer.structures.pathElements.PathElement;

public abstract class AbstractChannel extends PathElement{

	/**
	 * X coordinate of this channel
	 */
	protected int xCoordinate;
	
	/**
	 * Y coordinate of this channel
	 */
	protected int yCoordinate;
	
	/**
	 * wire this channel uses<br>
	 * (multiple channels at same coordinates allowed, if wire different)<br>
	 * this channel may only be connected to adjacent blocks, and to adjacent channels with the <b>same</b> wire number
	 */
	protected int wire;

	/**
	 * each cell represents a possible connection to an adjacent PathElement <br>
	 * <br>
	 * at most 1 stored path element can be the (local) signal source <br>
	 * all other connected path elements are (local) sinks
	 */
	protected PathElement[] connectedNodes;
	
	/**
	 * stores the location of the source node in the connection array connectedNodes
	 * @see designAnalyzer.structures.pathElements.PathElement#connectedNodes
	 */
	protected int sourceIndex= -1;
	
	/**
	 * stores the location of the output node that lies on the critical path in the connection array connectedNodes
	 * @see designAnalyzer.structures.pathElements.PathElement#connectedNodes
	 */
	private int criticalPathIndex;
	

	public int analyzeTiming(){
		
		for(int i= 0; i < connectedNodes.length; i++){ //process all connected output nodes (PathElements)
			
			if(i != sourceIndex && connectedNodes[i] != null){	//proces only the connected output nodes
				
				int temp= connectedNodes[i].analyzeTiming() + connectedNodes[i].getTConnectToChannel();	//compute critical path from here to all sinks reachable through this output node 
				
				if(temp >= t){	//if this critical path is longer, save it
				
					t += temp;
					criticalPathIndex= i;
					
				}
				
			}
			
		}
		return t;
	}
	
	public void printCriticalPath(){
		printThisNode();
		connectedNodes[criticalPathIndex].printCriticalPath();
	}

	/**
	 * prints this node as part of the critical path
	 */
	protected abstract void printThisNode();

	/**
	 * returns value of parameter constant stored in the ParameterManager
	 * @return the requested value
	 */
	public int getTConnectToIOBlock() {
		
		return ParameterManager.T_CONNECT_CHANNEL_IOBLOCK;
		
	}
	
	public int getTConnectToChannel(){
		
		return ParameterManager.T_CONNECT_CHANNEL_CHANNEL;
		
	}

	/**
	 * returns value of parameter constant stored in the ParameterManager
	 * @return the requested value
	 */
	public int getTConnectToLogicBlock() {
		
		return ParameterManager.T_CONNECT_CHANNEL_LOGIC_BLOCK;
		
	}
	
	
}
