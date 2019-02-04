package router.structures.resourceWithCost;

import java.util.PriorityQueue;

import designAnalyzer.ParameterManager;
import designAnalyzer.structures.pathElements.blocks.LogicBlock;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import router.structures.blockPinCost.BlockPinCost;
import router.structures.blockPinCost.LogicBlockPinCost;


public class ChannelWithCost extends ResourceWithCost{

	private int x;
	private int y;
	private double hv;
	
	private static ParameterManager parameterManager;
	private Boolean horizontal;
	
	private static int usedCounter;
	private int usedCounterValidityDate;
	
	BlockPinCost sinkToReach;
	
	public ChannelWithCost(int newX, int newY, boolean newHorizontal, double newCost, ChannelWithCost newPrevious) {
		super(newCost, newPrevious);
		parameterManager= ParameterManager.getInstance();
		x= newX;
		y= newY;
		horizontal= newHorizontal;
		usedCounter= 0;
		
	}
	
	public void updateHistoryCongestion() {
		hv = hv + (double) Math.max(0, usedCounter - parameterManager.CHANNEL_WIDTH);
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
	public double computeCost(int pFak, int iterationCounter) {
		double pv = (double) 1 + (double) Math.max(0, (double) (usedCounter + 1 - parameterManager.CHANNEL_WIDTH) * 0.5 * (double) pFak );
		if(sinkToReach == null) {
			return hv * pv; //bv = 1
		}
		else { //add cost of sink pin (experimental)
			if(sinkToReach instanceof LogicBlockPinCost) {
				if(horizontal) {
					if(y == sinkToReach.getY()) { //top pin
						pv+= (double) Math.max(0, (double) (((LogicBlockPinCost) sinkToReach).getTopInPinUsedCounter(iterationCounter) + 1 - 1) * 0.5 * (double) pFak );
					}
					else { //bottom pin
						//TODO remove
						if(!(y == sinkToReach.getY() - 1)) System.err.println("Error 001");
						
						pv+= (double) Math.max(0, (double) (((LogicBlockPinCost) sinkToReach).getBottomInPinUsedCounter(iterationCounter) + 1 - 1) * 0.5 * (double) pFak );
					}
				}
				else {
					if(x == sinkToReach.getX()) { //right pin
						pv+= (double) Math.max(0, (double) (((LogicBlockPinCost) sinkToReach).getRightInPinUsedCounter(iterationCounter) + 1 - 1) * 0.5 * (double) pFak );
					}
					else { //left pin
						//TODO remove
						if(!(x == sinkToReach.getX() - 1)) System.err.println("Error 001");
						
						pv+= (double) Math.max(0, (double) (((LogicBlockPinCost) sinkToReach).getLeftInPinUsedCounter(iterationCounter) + 1 - 1) * 0.5 * (double) pFak );
					}
				}
			}
			//else : io block pin will only be used once, no need to calculate anything
			return hv * pv * 0.95; //bv = 0.95, currently acting as input pin...
		}
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

	public int getUsedCounter() {
		return usedCounter;
	}

	public void setLastChannel(BlockPinCost sinkPins) {
		sinkToReach= sinkPins;
	}

	public void resetLastChannel() {
		sinkToReach= null;
		costValidityDate= -1; //invalidate cost, which was specific to this sink
	}

	public void setPathCostAndPreviousIfNotYetComputedInThisIteration(ChannelWithCost newPrevious, int pFak, int iterationCounter) {
		if(costValidityDate == iterationCounter) return;
		else {
			previous= newPrevious;
			cost= computeCost(pFak, iterationCounter);
			costValidityDate= iterationCounter;
		}
	}

	public void setSinkPinUsed(NetlistBlock sink, int iterationCounter) {
		//TODO remove
		if(sinkToReach == null || !sinkToReach.equals(sink)) System.err.println("Error 002");
		
		if(sinkToReach instanceof LogicBlockPinCost) {
			if(horizontal) {
				if(y == sinkToReach.getY()) { //top pin
					((LogicBlockPinCost) sinkToReach).setTopInPinUsed(iterationCounter);
				}
				else { //bottom pin
					((LogicBlockPinCost) sinkToReach).setBottomInPinUsed(iterationCounter);
				}
			}
			else {
				if(x == sinkToReach.getX()) { //right pin
					((LogicBlockPinCost) sinkToReach).setRightInPinUsed(iterationCounter);
				}
				else { //left pin
					((LogicBlockPinCost) sinkToReach).setLeftInPinUsed(iterationCounter);
				}
			}
		}
		// else : nothing, no need for a counter at io block, because only one path can be connected to the whole block anyways
	}
	
}
