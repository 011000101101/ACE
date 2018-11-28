package designAnalyzer.structures.pathElements.pins;

import designAnalyzer.structures.pathElements.PathElement;

public class OPin extends PathElement {

	@Override
	public void printCriticalPath(StringBuilder output, int lastTA) {
		// TODO Auto-generated method stub

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
