package designAnalyzer.structures.pathElements.channels;

import java.util.HashMap;
import java.util.Map;

import designAnalyzer.structures.pathElements.PathElement;

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
	
	


	
	@Override
	public void printCriticalPath(StringBuilder output, int lastTA){

		printThisNode(output, lastTA);
		criticalNext.printCriticalPath(output, tA);
	}

	
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
	

	
	@Override
	protected int annotateTA(int[] exactWireLengt) {
		
		tA= previous.analyzeTA(exactWireLengt);
		tA+= parameterManager.T_SWITCH;
		exactWireLengt[0] =exactWireLengt[0] + 1; //add CHANNEL segment
		exactWireLengt[3]= exactWireLengt[3] + 1; //add SWITCH segment
		return tA;
	}
	
	protected int annotateTRAndSlack(int criticalPathLength, int[] exactWireLengthDummy) {

		tR= Integer.MAX_VALUE;
		for(PathElement p : next.keySet()) {
			int temp= p.analyzeTRAndSlack(criticalPathLength, exactWireLengthDummy); //p.tR
			int w= p.analyzeTA(exactWireLengthDummy) - tA; //p.tA already computed -> retrieve, path length is difference to local
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
