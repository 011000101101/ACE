package router.structures.resourceWithCost;

import java.util.PriorityQueue;

public class ChannelWithCost extends ResourceWithCost{

	private int x;
	private int y;
	
	private Boolean horizontal;
	
	public ChannelWithCost(int newX, int newY, boolean newHorizontal, double newCost, ChannelWithCost newPrevious) {
		super(newCost, newPrevious);
		x= newX;
		y= newY;
		horizontal= newHorizontal;
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
	public void addChannelToPriorityQueue(PriorityQueue<ChannelWithCost> pQ) {
		pQ.add(this);
	}
	
}
