package router.structures.resourceWithCost;

import java.util.PriorityQueue;

public abstract class ResourceWithCost {

	double cost;
	
	//previous can only be channel, as source is not explicitly saved.
	ChannelWithCost previous; //possible to save space by just saving direction of previous (has to be direct neighbour), but then additional logic needed for backtracking
	
	int costValidityDate;
	
	public ResourceWithCost(double newCost, ChannelWithCost newPrevious) {
		cost= newCost;
		previous= newPrevious;
		costValidityDate= -1;
	}
	
	public abstract int getX();
	
	public abstract int getY();
	
	public double getCost() {
		return cost;
	}
	
	public ChannelWithCost getPrevious() {
		return previous;
	}

	public abstract void addChannelToPriorityQueue(PriorityQueue<ChannelWithCost> pQ);
	
}
