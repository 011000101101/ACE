package designAnalyzer.structures.pathElements.channels;

import designAnalyzer.structures.pathElements.PathElement;
import designAnalyzer.structures.pathElements.pins.IPin;
import designAnalyzer.structures.pathElements.pins.OPin;

/**
 * 
 * @author Dennis Grotz, Rumei Ma, Vincenz Mechler
 *
 */
public class ChannelY extends AbstractChannel {

	
	public ChannelY() {
		super();
	}

	@Override
	public boolean isHorizontal() {
		return false;
	}
	
	protected boolean matchesIsChanX(boolean isChanX) {
		return !isChanX;
	}
	

	@Override
	public void getInfo(StringBuilder output) {
		output.append("CHAN_Y");
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
	public boolean isNeighbour(PathElement neighbour) {
		
		boolean temp= false;
		
		if(neighbour instanceof ChannelY) {
			
			temp= (xCoordinate == neighbour.getX());
			temp= temp && ((yCoordinate - 1 == neighbour.getY()) || (yCoordinate + 1 == neighbour.getY()));
			
		}
		else if(neighbour instanceof ChannelX) {
			
			temp= ((xCoordinate + 1 == neighbour.getX()) || (xCoordinate == neighbour.getX()));
			temp= temp && ((yCoordinate  == neighbour.getY()) || (yCoordinate - 1 == neighbour.getY()));
			
		}
		else if(neighbour instanceof IPin || neighbour instanceof OPin){ // neighbour is pin
			
			temp= (yCoordinate == neighbour.getY());
			temp= temp && ((xCoordinate == neighbour.getX()) || (xCoordinate + 1 == neighbour.getX()));
			
		}
		
		return temp;
	}

	@Override
	public String getName() {
		return "ChanY(" + xCoordinate + "," + yCoordinate + ")";
	}
}
