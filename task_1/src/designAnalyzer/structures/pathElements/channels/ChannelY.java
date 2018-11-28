package designAnalyzer.structures.pathElements.channels;

import designAnalyzer.structures.pathElements.PathElement;

public class ChannelY extends AbstractChannel {


	@Override
	public boolean isHorizontal() {
		return false;
	}
	
	protected boolean matchesIsChanX(boolean isChanX) {
		return !isChanX;
	}
	

	@Override
	public void getInfo(StringBuilder output) {
		output.append("CHANX");
		output.append("\t");
		output.append("\t");
		output.append("(");
		output.append(xCoordinate);
		output.append(",");
		output.append(yCoordinate);
		output.append(").");
		output.append(wire);
		
	}

	@Override
	public boolean isNeighbour(PathElement neighbour) {
		
		boolean temp= true;
		
		if(neighbour instanceof ChannelY) {
			
			temp= temp && (xCoordinate == neighbour.getX());
			temp= temp && ((yCoordinate - 1 == neighbour.getY()) || (yCoordinate + 1 == neighbour.getY()));
			
		}
		else if(neighbour instanceof ChannelX) {
			
			temp= temp && ((xCoordinate + 1 == neighbour.getX()) || (xCoordinate == neighbour.getX()));
			temp= temp && ((yCoordinate  == neighbour.getY()) || (yCoordinate - 1 == neighbour.getY()));
			
		}
		else { // nighbour is block
			
			temp= temp && (yCoordinate == neighbour.getY());
			temp= temp && ((xCoordinate - 1 == neighbour.getX()) || (xCoordinate + 1 == neighbour.getX()));
			
		}
		
		return temp;
	}

	@Override
	public String getName() {
		return "ChanY(" + xCoordinate + "," + yCoordinate + ")";
	}
}
