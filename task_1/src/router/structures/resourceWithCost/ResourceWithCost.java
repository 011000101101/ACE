package router.structures.resourceWithCost;


import java.util.PriorityQueue;

import designAnalyzer.ParameterManager;

public abstract class ResourceWithCost {

	double cost;

	protected double hv;
	
	ResourceWithCost previous; //possible to save space by just saving direction of previous (has to be direct neighbour), but then additional logic needed for backtracking
	
	int costValidityDate;
	protected int usedCounterValidityDate;

	private boolean alreadyAdded;

	private int costValidityDate2;

	private int costValidityDate3;

	private int alreadyAddedDate;

	private int alreadyAddedDate1;

	private int hvValidityDate;
	
	public ResourceWithCost(ChannelWithCost newPrevious) {
		cost= 0;
		previous= newPrevious;
		costValidityDate= -1;
		costValidityDate2= -1;
		costValidityDate3= -1;
		usedCounterValidityDate= -1;
		alreadyAddedDate= -1;
		alreadyAddedDate1= -1;
		hvValidityDate= -1;
		alreadyAdded= false;
		hv= 1;
	}
	
	public void updateHistoryCongestion(int iterationCounter, int globalIterationCounter) {
		if(hvValidityDate == globalIterationCounter) {
			hv = hv + (double) Math.max(0, getUsedCounter(iterationCounter) - 1);
		}
		else {
			hv = 1 + (double) Math.max(0, getUsedCounter(iterationCounter) - 1);
		}
	}
	
	protected double getHv(int globalIterationCounter) {
		if(hvValidityDate == globalIterationCounter) {
			return hv;
		}
		else {
			return 1;
		}
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

	public void setUsed(int iterationCounter) {
		//TODO check
		costValidityDate= -1;
		costValidityDate2= -1;
		costValidityDate3= -1;
		
		if(usedCounterValidityDate == iterationCounter) {
			incUsedCounter();
		}
		else {
			setUsedCounterToOne();
			usedCounterValidityDate= iterationCounter;
		}
	}
	
	protected abstract void setUsedCounterToOne();

	public abstract void incUsedCounter();
	
	public abstract int getUsedCounter(int iterationCounter);
	
	public void setPathCostAndPreviousIfNotYetComputedInThisIteration(ResourceWithCost newPrevious, int pFak, int currentChannelWidth, int innerIterationCounter, int iterationCounter, int globalIterationCounter) {
		if(costValidityDate == innerIterationCounter && costValidityDate2 == iterationCounter && costValidityDate3 == globalIterationCounter) {
//			System.out.println("cost: " + cost);
			return;
		}
		else {
			previous= newPrevious;
			cost= previous.getCost() + computeCost(pFak, currentChannelWidth, iterationCounter, globalIterationCounter);
			costValidityDate= innerIterationCounter;
			costValidityDate2= iterationCounter;
			costValidityDate3= globalIterationCounter;
//			System.out.println("cost: " + cost);
		}
	}
	
	public abstract void addChannelToPriorityQueue(PriorityQueue<ResourceWithCost> pQ);
	

	public boolean alreadyAdded(int innerIterationCounter, int iterationCounter) {
		if(alreadyAdded && alreadyAddedDate == innerIterationCounter && alreadyAddedDate1 == iterationCounter && globalIterationCounter) return true;
		else return false;
	}
	
	public void setAlreadyAdded(boolean newAlreadyAdded, int innerIterationCounter, int iterationCounter) {
		alreadyAdded = newAlreadyAdded ;
		alreadyAddedDate= innerIterationCounter;
		alreadyAddedDate1= iterationCounter;
		alreadyAddedDate2= globalIterationCounter;
	}

	public abstract boolean neighbours(ResourceWithCost branchingPoint);
	
	public abstract void resetCounters();


	public void invalidateCaches() {
		costValidityDate= -1;
		costValidityDate2= -1;
		costValidityDate3= -1;
		resetCounters();
	}


	public void invalidateCostCache() {
		costValidityDate= -1;
		costValidityDate2= -1;
		costValidityDate3= -1;
	}
}
