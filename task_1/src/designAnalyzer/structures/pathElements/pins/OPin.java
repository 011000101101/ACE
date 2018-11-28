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
		else if(neighbour instanceof ChannelX) {
			
			//if(pinNumber == 0) {
				return ( ( xCoordinate == neighbour.getX()) && ( yCoordinate == neighbour.getY() || yCoordinate - 1 == neighbour.getY() ) );
			//}
		}
		else if(neighbour instanceof ChannelY) {
			
			return ( ( yCoordinate == neighbour.getY() ) && ( ( xCoordinate == neighbour.getX() ) || ( xCoordinate - 1 == neighbour.getX() ) ) );
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

}
