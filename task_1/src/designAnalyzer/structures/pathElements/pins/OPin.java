package designAnalyzer.structures.pathElements.pins;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import designAnalyzer.structures.pathElements.PathElement;
import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.LogicBlock;

public class OPin extends PathElement {

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
	public void printCriticalPath(StringBuilder output, int lastTA) {

		printThisNode(output, lastTA);
		criticalNext.printCriticalPath(output, tA);

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
	protected PathElement searchAllNext(int checkXCoordinate, int checkYCoordinate, int checkTrack, boolean isChanX,
			boolean init, boolean isPin) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean checkIfBranchingPoint(int checkXCoordinate, int checkYCoordinate, int checkTrack,
			boolean isChanX, boolean isPin) {
		if(isPin) {
			if(checkXCoordinate == xCoordinate && checkYCoordinate == yCoordinate) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected int annotateTA() {
		
		tA= previous.analyzeTA();
		if(previous instanceof IOBlock) {//previous is external input block
			tA+= parameterManager.T_IPAD; 
		}
		else { //previous is logic block
			if(((LogicBlock) previous).isSequential()) {
				tA += parameterManager.T_FFOUT; //previous is clocked logic block
			}
			// else do nothing, previous is unclocked logic block, delay already handled in logicBlock
		}
		return tA;
	}

	@Override
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
		
		next.put(newNext, -1);

	}

	@Override
	public String getName() {
		return "IPIN(" + xCoordinate + "," + yCoordinate + ")";
	}

}
