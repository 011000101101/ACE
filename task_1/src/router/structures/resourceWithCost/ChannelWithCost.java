package router.structures.resourceWithCost;

import java.util.PriorityQueue;


public class ChannelWithCost extends ResourceWithCost{

	private int x;
	private int y;
	private int trackNum;
	
	/**
	 * flag for horizontal or vertical channels
	 */
	private Boolean horizontal;
//	private boolean lastChannel;
	
	//BlockPinCost sinkToReach;
	
	public ChannelWithCost(int newX, int newY, boolean newHorizontal, int newTrackNum) {
		super(null);
		trackNum= newTrackNum;
		x= newX;
		y= newY;
		horizontal= newHorizontal;
		usedCounter= 0;
	}
	
	
	/**
	 * computes the cost of the current channel 
	 * cost(u, v) = bv * hv * pv 
	 * b(v) = 1 
	 * h(v)i = h(v)i-1 + max(0, occupancy(v) - capacity(v))
	 * p(v) = 1 + max(0, [occupancy(v) + 1 - capacity(v)] * pfak 
	 * @param xCoordinateChannel
	 * @param yCoordinateChannel
	 * @param channelOrientation
	 * @return
	 */
	@Override
	public double computeCost(double pFak, int currentChannelWidth) { //not - currentChannelWidth, but -1, because only one track, therefore + 1 - 1 = 0
		double pv = (double) 1 + /*(double) Math.max(0, */(double) (getUsedCounter() /* + 1 - currentChannelWidth */ ) * pFak /*)*/;
		return hv * pv; //bv = 1
	}


	@Override
	public int getX() {
		return x;
	}
	
	@Override
	public int getY() {
		return y;
	}
	
	public Boolean getHorizontal() {
		return horizontal;
	}

	@Override
	public void addChannelToPriorityQueue(PriorityQueue<ResourceWithCost> pQ) {
		pQ.add(this);
	}


	public int getTrackNum() {
		return trackNum;
	}


	@Override
	public boolean neighbours(ResourceWithCost branchingPoint) {
		if(branchingPoint instanceof ChannelWithCost) {
			if(((ChannelWithCost) branchingPoint).getTrackNum() != trackNum) return false;
			if(getHorizontal()) { //is a horizontal channel
				if(x == branchingPoint.getX() - 1 && y == branchingPoint.getY() && ((ChannelWithCost) branchingPoint).getHorizontal()) return true;
				if(x == branchingPoint.getX() + 1 && y == branchingPoint.getY() && ((ChannelWithCost) branchingPoint).getHorizontal()) return true;
				if(x == branchingPoint.getX() && y == branchingPoint.getY() && !((ChannelWithCost) branchingPoint).getHorizontal()) return true;
				if(x == branchingPoint.getX() - 1 && y == branchingPoint.getY() && !((ChannelWithCost) branchingPoint).getHorizontal()) return true;
				if(x == branchingPoint.getX() && y == branchingPoint.getY() + 1 && !((ChannelWithCost) branchingPoint).getHorizontal()) return true;
				if(x == branchingPoint.getX() - 1 && y == branchingPoint.getY() + 1 && !((ChannelWithCost) branchingPoint).getHorizontal()) return true;
			}
			else { //is a vertical channel
				if(x == branchingPoint.getX() && y == branchingPoint.getY() - 1 && !((ChannelWithCost) branchingPoint).getHorizontal()) return true;
				if(x == branchingPoint.getX() && y == branchingPoint.getY() + 1 && !((ChannelWithCost) branchingPoint).getHorizontal()) return true;
				if(x == branchingPoint.getX() && y == branchingPoint.getY() && ((ChannelWithCost) branchingPoint).getHorizontal()) return true;
				if(x == branchingPoint.getX() && y == branchingPoint.getY() - 1 && ((ChannelWithCost) branchingPoint).getHorizontal()) return true;
				if(x == branchingPoint.getX() + 1 && y == branchingPoint.getY() && ((ChannelWithCost) branchingPoint).getHorizontal()) return true;
				if(x == branchingPoint.getX() + 1 && y == branchingPoint.getY() - 1 && ((ChannelWithCost) branchingPoint).getHorizontal()) return true;
			}
		}
		else {
			switch(((SinkWithCost) branchingPoint).getPin()) {
				case 0: //bottom pin
					if(x == branchingPoint.getX() && y == branchingPoint.getY() - 1 && horizontal) return true;
					break;
				case 1: //left pin
					if(x == branchingPoint.getX() - 1 && y == branchingPoint.getY() && !horizontal) return true;
					break;
				case 2: //top pin
					if(x == branchingPoint.getX() && y == branchingPoint.getY() && horizontal) return true;
					break;
				case 3: //right pin
					if(x == branchingPoint.getX() && y == branchingPoint.getY() && !horizontal) return true;
					break;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "Channel @ (" + x + "," + y + ") [" + ( horizontal ? "h" : "v" ) + "] (track " + trackNum + ")";
	}
	
}
