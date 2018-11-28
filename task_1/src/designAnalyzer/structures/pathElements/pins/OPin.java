package designAnalyzer.structures.pathElements.pins;

import designAnalyzer.structures.pathElements.PathElement;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import designAnalyzer.structures.pathElements.channels.ChannelX;
import designAnalyzer.structures.pathElements.channels.ChannelY;

public class OPin extends PathElement {

	@Override
	public void printCriticalPath(StringBuilder output, int lastTA) {
		// TODO Auto-generated method stub

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


	/**
	 * checks if the channel, IOBlock or logic block is a neighbour of the opin <br>
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
	public void setCoordinates(int xCoordinate, int yCoordinate) {
		// TODO Auto-generated method stub

	}

	@Override
	protected PathElement searchAllNext(int checkXCoordinate, int checkYCoordinate, int checkTrack, boolean isChanX,
			boolean init) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean checkIfBranchingPoint(int checkXCoordinate, int checkYCoordinate, int checkTrack,
			boolean isChanX) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected int annotateTA() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int annotateTRAndSlack(int criticalPathLength) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addPrevious(PathElement newPrevoius) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addNext(PathElement newNext) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
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
}
