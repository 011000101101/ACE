package designAnalyzer.structures.pathElements.channels;

import designAnalyzer.ParameterManager;

import java.util.List;
import java.util.Map;

import designAnalyzer.structures.pathElements.PathElement;
import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.LogicBlock;
import static designAnalyzer.ParameterManager.T_IPAD;
import static designAnalyzer.ParameterManager.T_OPAD;
import static designAnalyzer.ParameterManager.T_SWITCH;
import static designAnalyzer.ParameterManager.T_COMB;
import static designAnalyzer.ParameterManager.T_FFIN;
import static designAnalyzer.ParameterManager.T_FFOUT;

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

	/**
	 * previous node (in signal flow direction)
	 */
	private PathElement previous;
	
	/**
	 * next nodes and slack of connection to them (in signal flow direction)
	 */
	private Map<PathElement, Integer> next;
	
	//TODO check if needed
	private PathElement criticalNext;
	

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
	
	public int getWire(){
		return wire;
	}
	
	public abstract boolean isHorizontal();

	@Override
	public void setCoordinates(int newXCoordinate, int newYCoordinate) {
		xCoordinate= newXCoordinate;
		yCoordinate= newYCoordinate;
		
	}
	
	protected boolean checkIfBranchingPoint(int checkXCoordinate, int checkYCoordinate, int checkTrack, boolean isChanX) {
		return matchesIsChanX(isChanX) && (checkXCoordinate == xCoordinate) && (checkYCoordinate == yCoordinate) && (checkTrack == wire);
	}

	/**
	 * checks if the class of this object matches the wanted ones
	 * @param isChanX describes the class of the wanted channel: true -> ChannelX, false -> ChannelY
	 * @return true if this is the wanted channel, false else
	 */
	protected abstract boolean matchesIsChanX(boolean isChanX);
	
	protected PathElement getSingleSource() {
		return connectedNodes[sourceIndex];
	}
	
	
	protected int annotateTA() {
		
		int tA= previous.analyzeTA();
		if(previous instanceof IOBlock) {
			tA+= T_IPAD + T_SWITCH;
		}
		else if(previous instanceof LogicBlock) {
			tA+= T_SWITCH;
		}
		else if(previous instanceof AbstractChannel) {
			tA+= T_SWITCH;
		}
		return tA;
	}
	
	protected int annotateTRAndSlack(int criticalPathLength) {

		int tR= Integer.MAX_VALUE;
		for(PathElement p : next.keySet()) {
			int temp= p.analyzeTRAndSlack(criticalPathLength); //p.tR
			int w= p.analyzeTA() - tA; //p.tA already computed -> retrieve, path length is difference to local
			int slack= temp - tA - w; //slack of connection from this to p
			next.replace(p, -1, slack); //store slack
			temp-= w; //compute tR candidate
			if(temp < tR) { //store if better than last candidate
				tR= temp;
				criticalNext= p;
			}
		}
		return tR;
		
	}
	
	public void addPrevious(PathElement newPrevious) {
		previous= newPrevious;
	}
	
	public void addNext(PathElement newNext) {
		next.put(newNext, -1);
	}
	
	
}
