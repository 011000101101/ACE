package designAnalyzer.structures.pathElements.channels;

import designAnalyzer.structures.pathElements.PathElement;

public class ChannelX extends AbstractChannel {


	@Override
	public boolean isHorizontal() {
		return true;
	}

	@Override
	public boolean isNeighbour(PathElement neighbour) {
		// TODO Auto-generated method stub
		return false;
	}

	protected boolean matchesIsChanX(boolean isChanX) {
		return true;
	}
	
}
