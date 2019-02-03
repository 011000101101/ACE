package router.structures.resourceWithCost;

import java.util.PriorityQueue;

import designAnalyzer.ParameterManager;


public class ChannelWithCost extends ResourceWithCost{

	private int x;
	private int y;
	private static double hv;
	
	private static ParameterManager parameterManager;
	private Boolean horizontal;
	
	private static int usedCounter;
	private int usedCounterValidityDate;
	
	public ChannelWithCost(int newX, int newY, boolean newHorizontal, double newCost, ChannelWithCost newPrevious) {
		super(newCost, newPrevious);
		parameterManager= ParameterManager.getInstance();
		x= newX;
		y= newY;
		horizontal= newHorizontal;
		usedCounter= 0;
		
	}
	
	public void updateHistoryCongestion() {
		hv = hv + Math.max(0, usedCounter - parameterManager.CHANNEL_WIDTH);
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
	public static double computeCost(int channelXCoordinate, int channelYCoordinate, boolean channelOrientation, double pFak) {
	 
		double pv = 1 + Math.max(0, (usedCounter + 1 - parameterManager.CHANNEL_WIDTH)* pFak );
		return hv * pv;
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