package designAnalyzer.structures.pathElements.channels;

import java.util.HashMap;
import java.util.Map;

import designAnalyzer.structures.pathElements.PathElement;
import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.LogicBlock;

/**
 * 
 * @author Dennis Grotz, Rumei Ma, Vincenz Mechler
 *
 */
public abstract class AbstractChannel extends PathElement{

	
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
	/*
	protected PathElement[] connectedNodes;
	*/
	
	/**
	 * stores the location of the source node in the connection array connectedNodes
	 * @see designAnalyzer.structures.pathElements.PathElement#connectedNodes
	 */
	/*
	protected int sourceIndex= -1;
	*/
	
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
	private Map<PathElement, Integer> next= new HashMap<PathElement, Integer>();
	
	/**
	 * the next node on the critical path
	 */
	private PathElement criticalNext;
	
	

	/* alternative critical path computation, not needed
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
	*/
	
	@Override
	public void printCriticalPath(StringBuilder output, int lastTA){

		printThisNode(output, lastTA);
		criticalNext.printCriticalPath(output, tA);
	}

	/**
	 * returns value of parameter constant stored in the ParameterManager
	 * @return the requested value
	 */
	/*
	public int getTConnectToIOBlock() {
		
		return ParameterManager.T_CONNECT_CHANNEL_IOBLOCK;
		
	}
	
	public int getTConnectToChannel(){
		
		return ParameterManager.T_CONNECT_CHANNEL_CHANNEL;
		
	}*/

	/**
	 * returns value of parameter constant stored in the ParameterManager
	 * @return the requested value
	 */
	/*
	public int getTConnectToLogicBlock() {
		
		return ParameterManager.T_CONNECT_CHANNEL_LOGIC_BLOCK;
		
	}*/
	
	public int getWire(){
		return wire;
	}
	
	public void setWire(int newWire) {
		wire= newWire;
	}
	
	public abstract boolean isHorizontal();

	@Override
	public void setCoordinates(int newXCoordinate, int newYCoordinate) {
		xCoordinate= newXCoordinate;
		yCoordinate= newYCoordinate;
		
	}
	
	@Override
	protected boolean checkIfBranchingPoint(int checkXCoordinate, int checkYCoordinate, int checkTrack, boolean isChanX, boolean isPin) {
		return (!isPin) && matchesIsChanX(isChanX) && (checkXCoordinate == xCoordinate) && (checkYCoordinate == yCoordinate) && (checkTrack == wire);
	}

	/**
	 * checks if the class of this object matches the wanted ones
	 * @param isChanX describes the class of the wanted channel: true -> ChannelX, false -> ChannelY
	 * @return true if this is the wanted channel, false else
	 */
	protected abstract boolean matchesIsChanX(boolean isChanX);
	
	@Override
	protected PathElement searchAllNext(int checkXCoordinate, int checkYCoordinate, int checkTrack, boolean isChanX, boolean init, boolean isPin) {
		PathElement found= null;
		for(PathElement p : next.keySet()) {
			if(found == null) {
				found = p.getBranchingElement(checkXCoordinate, checkYCoordinate, checkTrack, isChanX, isPin, false);
			}
		}
		return found;
		
	}
	
	/*
	protected PathElement getSingleSource() {
		return previous;
	}*/
	
	
	protected int annotateTA() {
		
		tA= previous.analyzeTA();
		tA+= parameterManager.T_SWITCH;
		return tA;
	}
	
	protected int annotateTRAndSlack(int criticalPathLength) {

		tR= Integer.MAX_VALUE;
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
	
	@Override
	public PathElement getOrigin() {
		return previous.getOrigin();
	}
	
	
}
