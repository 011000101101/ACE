package designAnalyzer.structures.pathElements.channels;

import designAnalyzer.structures.pathElements.PathElement;

public class ChannelX extends AbstractChannel {


	@Override
	public boolean isHorizontal() {
		return true;
	}

	@Override
	public boolean isNeighbour(PathElement neighbour) {
		
		boolean temp= true;
		
		if(neighbour instanceof ChannelX) {
			
			temp= temp && (yCoordinate == neighbour.getY());
			temp= temp && ((xCoordinate - 1 == neighbour.getX()) || (xCoordinate + 1 == neighbour.getX()));
			
		}
		else if(neighbour instanceof ChannelY) {
			
			temp= temp && ((xCoordinate - 1 == neighbour.getX()) || (xCoordinate == neighbour.getX()));
			temp= temp && ((yCoordinate  == neighbour.getY()) || (yCoordinate + 1 == neighbour.getY()));
			
		}
		else { // nighbour is block
			
			temp= temp && (xCoordinate == neighbour.getX());
			temp= temp && ((yCoordinate - 1 == neighbour.getY()) || (yCoordinate + 1 == neighbour.getY()));
			
		}
		
		return temp;
	}

	protected boolean matchesIsChanX(boolean isChanX) {
		return true;
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
	
}
