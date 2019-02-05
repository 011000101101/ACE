package router.structures.resourceWithCost;


import java.util.PriorityQueue;

import designAnalyzer.ParameterManager;

public abstract class ResourceWithCost {

	double cost;

	protected double hv;
	private static ParameterManager parameterManager;
	
	//previous can only be channel, as source is not explicitly saved.
	ChannelWithCost previous; //possible to save space by just saving direction of previous (has to be direct neighbour), but then additional logic needed for backtracking
	
	int costValidityDate;

	private boolean alreadyAdded;
	
	public ResourceWithCost(ChannelWithCost newPrevious) {
		cost= 0;
		previous= newPrevious;
		costValidityDate= -1;
		parameterManager= ParameterManager.getInstance();
		alreadyAdded= false;
		//TODO init Hv
	}
	
	public void updateHistoryCongestion(int iterationCounter) {
		hv = hv + (double) Math.max(0, getUsedCounter(iterationCounter) - parameterManager.CHANNEL_WIDTH);
	}
	
	public abstract int getX();
	
	public abstract int getY();
	
	public abstract double computeCost(int pFak, int currentChannelWidth, int iterationCounter);
	
	public double getCost() {
		return cost;
	}
	
	public ChannelWithCost getPrevious() {
		return previous;
	}

	public abstract void setUsed(int iterationCounter);
	
	public abstract int getUsedCounter(int iterationCounter);
	
	public void setPathCostAndPreviousIfNotYetComputedInThisIteration(ChannelWithCost newPrevious, int pFak, int currentChannelWidth, int iterationCounter) {
		if(costValidityDate == iterationCounter) return;
		else {
			previous= newPrevious;
			cost= computeCost(pFak, currentChannelWidth, iterationCounter);
			costValidityDate= iterationCounter;
		}
	}
	
	public abstract void addChannelToPriorityQueue(PriorityQueue<ResourceWithCost> pQ);
	

	public boolean alreadyAdded() {
		return alreadyAdded;
	}
	
	public void setAlreadyAdded(boolean newAlreadyAdded) {
		alreadyAdded = newAlreadyAdded ;
	}

	public abstract boolean neighbours(ResourceWithCost branchingPoint);
	
}
