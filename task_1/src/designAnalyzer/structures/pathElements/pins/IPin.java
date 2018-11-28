package designAnalyzer.structures.pathElements.pins;

import designAnalyzer.structures.pathElements.PathElement;
import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.LogicBlock;
import designAnalyzer.structures.pathElements.channels.AbstractChannel;

public class IPin extends PathElement{

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

	@Override
	public void printCriticalPath(StringBuilder output, int lastTA) {

		printThisNode(output, lastTA);
		next.printCriticalPath(output, tA);
		
	}

	@Override
	public void getInfo(StringBuilder output) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isNeighbour(PathElement neighbour) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCoordinates(int newXCoordinate, int newYCoordinate) {

		//TODO maybe check resource availability
		xCoordinate= newXCoordinate;
		yCoordinate= newYCoordinate;
		
	}

	@Override
	protected PathElement searchAllNext(int checkXCoordinate, int checkYCoordinate, int checkTrack, boolean isChanX, boolean isPin,
			boolean init) {
		return next.getBranchingElement(checkXCoordinate, checkYCoordinate, checkTrack, isChanX, isPin, false);
	}

	@Override
	protected boolean checkIfBranchingPoint(int checkXCoordinate, int checkYCoordinate, int checkTrack,
			boolean isChanX, boolean isPin) {
		//can't branch at IPin
		return false;
	}

	@Override
	protected int annotateTA() {
		
		tA= previous.analyzeTA();
		tA+= parameterManager.T_SWITCH; //previous always channel
		return tA;
		
	}

	@Override
	protected int annotateTRAndSlack(int criticalPathLength) {
		
		tR= next.analyzeTRAndSlack(criticalPathLength); //next.tR
		int w= next.analyzeTA() - tA; //next.tA already computed -> retrieve, path length is difference to local
		int slack= tR - tA - w; //slack of connection from this to p
		slackToNext= slack; //store slack
		tR-= w; //compute local tR
		
		return tR;
	}

	@Override
	public void addPrevious(PathElement newPrevious) {
		if(previous != null) {
			//TODO report error
			//ErrorReporter.re
		}
		else {
			previous = newPrevious;
		}
		
	}

	@Override
	public void addNext(PathElement newNext) {
		if(next != null) {
			//TODO report error
			//ErrorReporter.re
		}
		else {
			next = newNext;
		}
		
	}

	@Override
	public String getName() {
		return "IPIN(" + xCoordinate + "," + yCoordinate + ")." + pinNumber;
	}

}
