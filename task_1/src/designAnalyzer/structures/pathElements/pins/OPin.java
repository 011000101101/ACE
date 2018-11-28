package designAnalyzer.structures.pathElements.pins;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import designAnalyzer.structures.pathElements.PathElement;
import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.LogicBlock;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import designAnalyzer.structures.pathElements.channels.ChannelX;
import designAnalyzer.structures.pathElements.channels.ChannelY;

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
		output.append("O_Block");
		output.append("\t");
		output.append(this.getName());
		output.append("\t");
		output.append("(");
		output.append(xCoordinate);
		output.append(",");
		output.append(yCoordinate);
		output.append(").");
		output.append("1");
				
	}


	@Override
	public boolean isNeighbour(PathElement neighbour) {

		if(neighbour instanceof NetlistBlock) {
			
			return ( ( xCoordinate == neighbour.getX() ) &&  ( yCoordinate == neighbour.getY() )  );
			
		}
		int i = this.getBoundary();
		if(neighbour instanceof ChannelX) {

			switch (i) {
			case 0:
				return ( ( xCoordinate == neighbour.getX()) && yCoordinate == neighbour.getY() + 1  );
			case 2:
				return ( ( xCoordinate == neighbour.getX()) && yCoordinate == neighbour.getY() );
			case -1: //this pin is attached to a logic block
				return ( ( xCoordinate == neighbour.getX())  && (yCoordinate == neighbour.getY() + 1 ) );
			default:
				return false;
			}
		}
		else if(neighbour instanceof ChannelY) {

			switch (i) {
			case 1:
				return ( ( xCoordinate == neighbour.getX() + 1) && yCoordinate == neighbour.getY() );
			case 3:
				return ( ( xCoordinate == neighbour.getX()) && yCoordinate == neighbour.getY() );
			case -1: //this pin is attached to a logic block
				return ( ( xCoordinate == neighbour.getX())  && yCoordinate == neighbour.getY()  );
			default:
				return false;
			}
		}
		else {
			return false;
		}

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
		PathElement found= null;
		for(PathElement p : next.keySet()) {
			if(found == null) {
				found = p.getBranchingElement(checkXCoordinate, checkYCoordinate, checkTrack, isChanX, isPin, false);
			}
		}
		return found;
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

	/**
	 * tells us where the block of the pin is placed
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
		return previous.getOrigin();
	}
}
