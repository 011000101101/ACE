package router.structures.resourceWithCost;


import java.util.PriorityQueue;

import designAnalyzer.ParameterManager;

public abstract class ResourceWithCost {

	double cost;

	protected double hv;
	
	ResourceWithCost previous; //possible to save space by just saving direction of previous (has to be direct neighbour), but then additional logic needed for backtracking
	
	/**
	 * validity date for cost
	 */
	int costValidityDate;
	
	/**
	 * validity date for used count
	 */
	protected int usedCounterValidityDate;

	/**
	 * already added in this global iteration
	 */
	private boolean alreadyAdded;

	/**
	 * validity date for cost
	 */
	private int costValidityDate2;

	/**
	 * validity date for cost
	 */
	private int costValidityDate3;

	/**
	 * validity date for alreadyAdded
	 */
	private int alreadyAddedDate;

	/**
	 * validity date for alreadyAdded
	 */
	private int alreadyAddedDate1;

	/**
	 * validity date for hv
	 */
	private int hvValidityDate;

	/**
	 * validity date for alreadyAdded
	 */
	private int alreadyAddedDate2;

	/**
	 * validity date for used count
	 */
	protected int usedCounterValidityDate2;
	
	public ResourceWithCost(ChannelWithCost newPrevious) {
		cost= 0;
		previous= newPrevious;
		costValidityDate= -1;
		costValidityDate2= -1;
		costValidityDate3= -1;
		usedCounterValidityDate= -1;
		usedCounterValidityDate2= -1;
		alreadyAddedDate= -1;
		alreadyAddedDate1= -1;
		alreadyAddedDate2= -1;
		hvValidityDate= -1;
		alreadyAdded= false;
		hv= 1;
	}
	
	/**
	 * update hv according to algorithm
	 * @param iterationCounter
	 * @param globalIterationCounter
	 */
	public void updateHistoryCongestion(int iterationCounter, int globalIterationCounter) {
		if(hvValidityDate == globalIterationCounter) {
			hv = hv + (double) Math.max(0, getUsedCounter(iterationCounter, globalIterationCounter) - 1) * 1.25;
		}
		else {
			hvValidityDate= globalIterationCounter;
			hv = 1 + (double) Math.max(0, getUsedCounter(iterationCounter, globalIterationCounter) - 1) * 1.25;
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

	/**
	 * also set valfidity date
	 * @param iterationCounter
	 * @param globalIterationCounter
	 */
	public void setUsed(int iterationCounter, int globalIterationCounter) {
		//TODO check
		costValidityDate= -1;
		costValidityDate2= -1;
		costValidityDate3= -1;
		
		if(usedCounterValidityDate == iterationCounter && usedCounterValidityDate2 == globalIterationCounter) {
			incUsedCounter(iterationCounter, globalIterationCounter);
		}
		else {
			setUsedCounterToOne(iterationCounter, globalIterationCounter);
			usedCounterValidityDate= iterationCounter;
			usedCounterValidityDate2= globalIterationCounter;
		}
	}
	
	/**
	 * also set validity date
	 * @param iterationCounter
	 * @param globalIterationCounter
	 */
	protected abstract void setUsedCounterToOne(int iterationCounter, int globalIterationCounter);

	public abstract void incUsedCounter(int iterationCounter, int globalIterationCounter);
	
	/**
	 * also sets validity date
	 * @param iterationCounter
	 * @param globalIterationCounter
	 * @return
	 */
	public abstract int getUsedCounter(int iterationCounter, int globalIterationCounter);
	
	/**
	 * set if validity date is old. if that is the case, validity date is set to current date
	 * @param newPrevious
	 * @param pFak
	 * @param currentChannelWidth
	 * @param innerIterationCounter
	 * @param iterationCounter
	 * @param globalIterationCounter
	 */
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
	
	/**
	 * returns true if alreadyAdded date is correct
	 * @param innerIterationCounter
	 * @param iterationCounter
	 * @param globalIterationCounter
	 * @return
	 */
	public boolean alreadyAdded(int innerIterationCounter, int iterationCounter, int globalIterationCounter) {
		if(alreadyAdded && alreadyAddedDate == innerIterationCounter && alreadyAddedDate1 == iterationCounter && alreadyAddedDate2 == globalIterationCounter) return true;
		else return false;
	}
	
	/**
	 * also sets already added date
	 * @param newAlreadyAdded
	 * @param innerIterationCounter
	 * @param iterationCounter
	 * @param globalIterationCounter
	 */
	public void setAlreadyAdded(boolean newAlreadyAdded, int innerIterationCounter, int iterationCounter, int globalIterationCounter) {
		alreadyAdded = newAlreadyAdded ;
		alreadyAddedDate= innerIterationCounter;
		alreadyAddedDate1= iterationCounter;
		alreadyAddedDate2= globalIterationCounter;
	}

	/**
	 * returns whether another resourceWithCost is neighbour of this object
	 * @param branchingPoint
	 * @return
	 */
	public abstract boolean neighbours(ResourceWithCost branchingPoint);
	
	public abstract void resetCounters();

	/**
	 * change cost validity dates to -1(representing invalid value) and resets counter
	 */
	public void invalidateCaches() {
		costValidityDate= -1;
		costValidityDate2= -1;
		costValidityDate3= -1;
		resetCounters();
	}

	/**
	 * change cost validity dates to -1(representing invalid value)
	 */
	public void invalidateCostCache() {
		costValidityDate= -1;
		costValidityDate2= -1;
		costValidityDate3= -1;
	}
}
