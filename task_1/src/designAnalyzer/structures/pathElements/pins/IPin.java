package designAnalyzer.structures.pathElements.pins;

import designAnalyzer.structures.pathElements.PathElement;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import designAnalyzer.structures.pathElements.channels.ChannelX;
import designAnalyzer.structures.pathElements.channels.ChannelY;

/**
 * 
 * @author Dennis Grotz, Rumei Ma, Vincenz Mechler
 *
 */
public class IPin extends PathElement{

	/**
	 * pin number of the current IPin with the coordinates saved in PathElement
	 */
	int pinNumber = -1;

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
	@SuppressWarnings("unused")
	private int slackToNext;

	@Override
	public void printCriticalPath(StringBuilder output, int lastTA) {

		printThisNode(output, lastTA);
		next.printCriticalPath(output, tA);
		
	}

	@Override
	public void getInfo(StringBuilder output) {
		output.append("I_Pin");
		output.append("\t");
		output.append("|");
		output.append(this.getName());
		output.append("\t");
		output.append("|");
		output.append("(");
		output.append(xCoordinate);
		output.append(",");
		output.append(yCoordinate);
		output.append(").");
		output.append(pinNumber);
		
		
	}

	/**
	 * checks if the channel, IOBlock or logic block is a neighbour of the ipin <br>
	 * pinNumber not relevant
	 */
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
				if(pinNumber == 2) {
					return ( ( xCoordinate == neighbour.getX())  && (yCoordinate == neighbour.getY() ) );
				}
				else if(pinNumber == 0) {
					return ( ( xCoordinate == neighbour.getX())  && (yCoordinate == neighbour.getY() + 1 ) );
				}
				else {
					return false;
				}
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
				if(pinNumber == 3) {
					return ( (xCoordinate == neighbour.getX())  && yCoordinate == neighbour.getY() );
				}
				else if(pinNumber == 1) {
					return ( (xCoordinate == neighbour.getX() + 1 )  && yCoordinate == neighbour.getY()  );
				}
				else {
					return false;
				}
				
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
	protected int annotateTA(int[] exactWireLengt) {
		
		tA= previous.analyzeTA(exactWireLengt);
		tA+= parameterManager.T_SWITCH; //previous always channel
		exactWireLengt[3]= exactWireLengt[3] + 1; //add SWITCH segment
		return tA;
		
	}

	@Override
	protected int annotateTRAndSlack(int criticalPathLength, int[] exactWireLengthDummy) {
		
		tR= next.analyzeTRAndSlack(criticalPathLength, exactWireLengthDummy); //next.tR
		int w= next.analyzeTA(exactWireLengthDummy) - tA; //next.tA already computed -> retrieve, path length is difference to local
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

	public void setPinNumber(int newPinNum) {

		pinNumber= newPinNum;
		
	}
	
	@Override
	public PathElement getOrigin() {
		return previous.getOrigin();
	}
}
