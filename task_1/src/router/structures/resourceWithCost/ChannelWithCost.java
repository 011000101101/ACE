package router.structures.resourceWithCost;

import java.util.PriorityQueue;

public class ChannelWithCost extends ResourceWithCost{

	private int x;
	private int y;
	
	private Boolean horizontal;
	
	private int usedCounter;
	private int usedCounterValidityDate;
	
	public ChannelWithCost(int newX, int newY, boolean newHorizontal, double newCost, ChannelWithCost newPrevious) {
		super(newCost, newPrevious);
		x= newX;
		y= newY;
		horizontal= newHorizontal;
		usedCounter= 0;
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

	public void setUsed(int iterationCounter) {
		if(usedCounterValidityDate == iterationCounter) {
			usedCounter++;
		}
		else {
			//TODO maybe update Hv?
			usedCounterValidityDate= iterationCounter;
			usedCounter= 1;
		}
	}
	
}
