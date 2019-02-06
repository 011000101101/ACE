package router.structures.resourceWithCost;


import java.util.PriorityQueue;

import designAnalyzer.ParameterManager;

public abstract class ResourceWithCost {

	double cost;

	protected double hv;
	private static ParameterManager parameterManager;
	
	ResourceWithCost previous; //possible to save space by just saving direction of previous (has to be direct neighbour), but then additional logic needed for backtracking
	
	int costValidityDate;

	private boolean alreadyAdded;
	
	public ResourceWithCost(ChannelWithCost newPrevious) {
		cost= 0;
		previous= newPrevious;
		costValidityDate= -1;
		parameterManager= ParameterManager.getInstance();
		alreadyAdded= false;
		//TODO init Hv
		hv= 1;
	}
	
	public void updateHistoryCongestion(int globalIterationCounter) {
		hv = hv + (double) Math.max(0, getUsedCounter(globalIterationCounter) - 1);
	}
	
	public abstract int getX();
	
	public abstract int getY();
	
	public abstract double computeCost(int pFak, int currentChannelWidth, int iterationCounter, int globalIterationCounter);
	
	public double getCost() {
		return cost;
	}
	
	public ResourceWithCost getPrevious() {
		return previous;
	}

	public abstract void setUsed(int globalIterationCounter);
	
	public abstract int getUsedCounter(int globalIterationCounter);
	
	public void setPathCostAndPreviousIfNotYetComputedInThisIteration(ResourceWithCost sourceDummy, int pFak, int currentChannelWidth, int iterationCounter, int globalIterationCounter) {
		if(costValidityDate == iterationCounter) {
//			System.out.println("cost: " + cost);
			return;
		}
		else {
			previous= sourceDummy;
			cost= computeCost(pFak, currentChannelWidth, iterationCounter, globalIterationCounter);
			costValidityDate= iterationCounter;
//			System.out.println("cost: " + cost);
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
	
	public abstract void resetCounters();


	public void invalidateCaches() {
		costValidityDate= -1;
		resetCounters();
	}


	public void invalidateCostCache() {
		costValidityDate= -1;
	}
}
