
package router.structures.resourceWithCost;


import java.util.PriorityQueue;

public abstract class ResourceWithCost {

	double costSum;
	double costDistEst;

	protected double hv;
	
	ResourceWithCost previous; //possible to save space by just saving direction of previous (has to be direct neighbour), but then additional logic needed for backtracking

	protected int usedCounter;
	
	public ResourceWithCost(ChannelWithCost newPrevious) {
		costSum= -1;
		previous= newPrevious;
		hv= 1;
	}
	
	/**
	 * update hv according to algorithm
	 * @param iterationCounter
	 * @param globalIterationCounter
	 */
	public void updateHistoryCongestion() {
		hv = hv + (double) Math.max(0, getUsedCounter() - 1);
	}
	
	protected double getHv() {
		return hv;
	}
	
	public abstract int getX();
	
	public abstract int getY();
	
	public abstract double computeCost(double pFak);
	
	public double getCost() {
		return costSum + costDistEst;
	}
	
	public ResourceWithCost getPrevious() {
		return previous;
	}

	
	/**
	 * also set validity date
	 * @param iterationCounter
	 * @param globalIterationCounter
	 */
//	protected abstract void setUsedCounterToOne();



	public void incUsedCounter() {
		usedCounter++;
	}

	public void decUsedCounter() {
		usedCounter--;
	}

	public int getUsedCounter() {
		return usedCounter;
	}
	
	/**
	 * set if validity date is old. if that is the case, validity date is set to current date
	 * @param newPrevious
	 * @param pFak
	 * @param currentChannelWidth
	 * @param innerIterationCounter
	 * @param iterationCounter
	 * @param globalIterationCounter
	 */
	public void setPathCostAndPreviousIfNotYetComputedInThisIteration(ResourceWithCost newPrevious, double pFak, int sinkX, int sinkY) {
		if(costSum >= 0) {
//			System.out.println("cost: " + cost);
			return;
		}
		else {
			previous= newPrevious;
			costSum= previous.getCostSum() + computeCost(pFak);
			costDistEst= computeCostDistEst(sinkX, sinkY);
//			System.out.println("cost: " + cost);
		}
	}
	
	
	private double getCostSum() {
		return costSum;
	}

	protected abstract double computeCostDistEst(int sinkX, int sinkY);

	public abstract void addChannelToPriorityQueue(PriorityQueue<ResourceWithCost> pQ);
	

	/**
	 * returns whether another resourceWithCost is neighbour of this object
	 * @param branchingPoint
	 * @return
	 */
	public abstract boolean neighbours(ResourceWithCost branchingPoint);

	/**
	 * reset hv to 1
	 */
	public void resetHistory() {
		hv= 1;
	}

	/**
	 * reset cost to -1 and invalidate 'previous' value
	 */
	public void resetCost() {
		costSum= -1;
		costDistEst= -1;
	}

	/**
	 * set cost to 0 (and revalidate old 'previous' value
	 */
	public void setCostToZero(int sinkX, int sinkY) {
		costSum= 0;
		costDistEst= computeCostDistEst(sinkX, sinkY);
	}

	/**
	 * reset hv to 1
	 */
//	public void discardFromPQ() {
//		cost= -1;
//		alreadyAdded= false;
//	}
}
