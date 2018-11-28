package designAnalyzer.structures.pathElements.channels;

import designAnalyzer.structures.pathElements.PathElement;
import designAnalyzer.structures.pathElements.pins.IPin;
import designAnalyzer.structures.pathElements.pins.OPin;

/**
 * 
 * @author Dennis Grotz, Rumei Ma, Vincenz Mechler
 *
 */
public class ChannelX extends AbstractChannel {


	@Override
	public boolean isHorizontal() {
		return true;
	}

	@Override
	public boolean isNeighbour(PathElement neighbour) {
		
		boolean temp= true;
		
		if(neighbour instanceof ChannelX) {
			
			temp= (yCoordinate == neighbour.getY());
			temp= temp && ((xCoordinate - 1 == neighbour.getX()) || (xCoordinate + 1 == neighbour.getX()));
			
		}
		else if(neighbour instanceof ChannelY) {
			
			temp= ((xCoordinate - 1 == neighbour.getX()) || (xCoordinate == neighbour.getX()));
			temp= temp && ((yCoordinate  == neighbour.getY()) || (yCoordinate + 1 == neighbour.getY()));
			
		}
		else if(neighbour instanceof IPin || neighbour instanceof OPin){ // neighbour is pin
			
			temp= (xCoordinate == neighbour.getX());
			temp= temp && ((yCoordinate == neighbour.getY()) || (yCoordinate + 1 == neighbour.getY()));
			
		}
		
		return temp;
	}

	protected boolean matchesIsChanX(boolean isChanX) {
		return isChanX;
	}


	@Override
	public void getInfo(StringBuilder output) {
		output.append("CHANX");
		output.append("\t");
		output.append("|");
		output.append("\t");
		output.append("|");
		output.append("(");
		output.append(xCoordinate);
		output.append(",");
		output.append(yCoordinate);
		output.append(").");
		output.append(wire);
		
	}

	@Override
	public String getName() {
		return "ChanX(" + xCoordinate + "," + yCoordinate + ")";
	}
	
}
