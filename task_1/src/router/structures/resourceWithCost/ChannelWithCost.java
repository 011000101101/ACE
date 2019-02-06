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
	private int trackNum;
	
	private Boolean horizontal;
	
	private int usedCounter;
	private boolean lastChannel;
	
	//BlockPinCost sinkToReach;
	
	public ChannelWithCost(int newX, int newY, boolean newHorizontal, int newTrackNum) {
		super(null);
		trackNum= newTrackNum;
		x= newX;
		y= newY;
		horizontal= newHorizontal;
		usedCounter= 0;
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
	@Override
	public double computeCost(int pFak, int currentChannelWidth, int iterationCounter, int globalIterationCounter) { //not - currentChannelWidth, but -1, because only one track, therefore + 1 - 1 = 0
		double pv = (double) 1 + /*(double) Math.max(0, */(double) (getUsedCounter(iterationCounter) /* + 1 - currentChannelWidth */ ) * (double) 0.5 * (double) pFak /*)*/;
		//if(sinkToReach == null) {
			return getHv(globalIterationCounter) * pv; //bv = 1
//		}
//		else { //add cost of sink pin (experimental)
//			if(sinkToReach instanceof LogicBlockPinCost) {
//				if(horizontal) {
//					if(y == sinkToReach.getY()) { //top pin
//						pv+= (double) Math.max(0, (double) (((LogicBlockPinCost) sinkToReach).getTopInPinUsedCounter(iterationCounter) + 1 - 1) * 0.5 * (double) pFak );
//					}
//					else { //bottom pin
//						//TODO remove
//						if(!(y == sinkToReach.getY() - 1)) System.err.println("Error 001");
//						
//						pv+= (double) Math.max(0, (double) (((LogicBlockPinCost) sinkToReach).getBottomInPinUsedCounter(iterationCounter) + 1 - 1) * 0.5 * (double) pFak );
//					}
//				}
//				else {
//					if(x == sinkToReach.getX()) { //right pin
//						pv+= (double) Math.max(0, (double) (((LogicBlockPinCost) sinkToReach).getRightInPinUsedCounter(iterationCounter) + 1 - 1) * 0.5 * (double) pFak );
//					}
//					else { //left pin
//						//TODO remove
//						if(!(x == sinkToReach.getX() - 1)) System.err.println("Error 001");
//						
//						pv+= (double) Math.max(0, (double) (((LogicBlockPinCost) sinkToReach).getLeftInPinUsedCounter(iterationCounter) + 1 - 1) * 0.5 * (double) pFak );
//					}
//				}
//			}
//			//else : io block pin will only be used once, no need to calculate anything
//			return hv * pv * 0.95; //bv = 0.95, currently acting as input pin...
//		}
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
	public void addChannelToPriorityQueue(PriorityQueue<ResourceWithCost> pQ) {
		pQ.add(this);
	}

	@Override
	public void incUsedCounter() {
		usedCounter++;
	}
	
	protected void setUsedCounterToOne() {
		usedCounter= 1;
	}

	@Override
	public int getUsedCounter(int iterationCounter) {
		if(usedCounterValidityDate == iterationCounter) {
			return usedCounter;
		}
		else {
			return 0;
		}
	}

	public void setLastChannel(BlockPinCost sinkPins) {
		lastChannel= true;
		//sinkToReach= sinkPins;
	}

	public void resetLastChannel() {
		lastChannel= false;
		//sinkToReach= null;
		//costValidityDate= -1; //invalidate cost, which was specific to this sink
	}


	public int getTrackNum() {
		return trackNum;
	}


	@Override
	public boolean neighbours(ResourceWithCost branchingPoint) {
		if(branchingPoint instanceof ChannelWithCost) {
			if(((ChannelWithCost) branchingPoint).getTrackNum() != trackNum) return false;
			if(getHorizontal()) { //is a horizontal channel
				if(x == branchingPoint.getX() - 1 && y == branchingPoint.getY() && ((ChannelWithCost) branchingPoint).getHorizontal()) return true;
				if(x == branchingPoint.getX() + 1 && y == branchingPoint.getY() && ((ChannelWithCost) branchingPoint).getHorizontal()) return true;
				if(x == branchingPoint.getX() && y == branchingPoint.getY() && !((ChannelWithCost) branchingPoint).getHorizontal()) return true;
				if(x == branchingPoint.getX() - 1 && y == branchingPoint.getY() && !((ChannelWithCost) branchingPoint).getHorizontal()) return true;
				if(x == branchingPoint.getX() && y == branchingPoint.getY() + 1 && !((ChannelWithCost) branchingPoint).getHorizontal()) return true;
				if(x == branchingPoint.getX() - 1 && y == branchingPoint.getY() + 1 && !((ChannelWithCost) branchingPoint).getHorizontal()) return true;
			}
			else { //is a vertical channel
				if(x == branchingPoint.getX() && y == branchingPoint.getY() - 1 && !((ChannelWithCost) branchingPoint).getHorizontal()) return true;
				if(x == branchingPoint.getX() && y == branchingPoint.getY() + 1 && !((ChannelWithCost) branchingPoint).getHorizontal()) return true;
				if(x == branchingPoint.getX() && y == branchingPoint.getY() && ((ChannelWithCost) branchingPoint).getHorizontal()) return true;
				if(x == branchingPoint.getX() && y == branchingPoint.getY() - 1 && ((ChannelWithCost) branchingPoint).getHorizontal()) return true;
				if(x == branchingPoint.getX() + 1 && y == branchingPoint.getY() && ((ChannelWithCost) branchingPoint).getHorizontal()) return true;
				if(x == branchingPoint.getX() + 1 && y == branchingPoint.getY() - 1 && ((ChannelWithCost) branchingPoint).getHorizontal()) return true;
			}
		}
		else {
			switch(((SinkWithCost) branchingPoint).getPinNum()) {
				case 0: //bottom pin
					if(x == branchingPoint.getX() && y == branchingPoint.getY() - 1 && horizontal) return true;
					break;
				case 1: //left pin
					if(x == branchingPoint.getX() - 1 && y == branchingPoint.getY() && !horizontal) return true;
					break;
				case 2: //top pin
					if(x == branchingPoint.getX() && y == branchingPoint.getY() && horizontal) return true;
					break;
				case 3: //right pin
					if(x == branchingPoint.getX() && y == branchingPoint.getY() && !horizontal) return true;
					break;
			}
		}
		return false;
	}

//	public void setSinkPinUsed(NetlistBlock sink, int iterationCounter) {
//		//TODO remove
//		if(sinkToReach == null || !sinkToReach.equals(sink)) System.err.println("Error 002");
//		
//		if(sinkToReach instanceof LogicBlockPinCost) {
//			if(horizontal) {
//				if(y == sinkToReach.getY()) { //top pin
//					((LogicBlockPinCost) sinkToReach).setTopInPinUsed(iterationCounter);
//				}
//				else { //bottom pin
//					((LogicBlockPinCost) sinkToReach).setBottomInPinUsed(iterationCounter);
//				}
//			}
//			else {
//				if(x == sinkToReach.getX()) { //right pin
//					((LogicBlockPinCost) sinkToReach).setRightInPinUsed(iterationCounter);
//				}
//				else { //left pin
//					((LogicBlockPinCost) sinkToReach).setLeftInPinUsed(iterationCounter);
//				}
//			}
//		}
//		// else : nothing, no need for a counter at io block, because only one path can be connected to the whole block anyways
//	}
	
	@Override
	public String toString() {
		return "Channel @ (" + x + "," + y + ") [" + ( horizontal ? "h" : "v" ) + "] (track " + trackNum + ")";
	}


	public void resetHistory() {
		hv= 1;
	}


	@Override
	public void resetCounters() {
		usedCounterValidityDate= -1;
	}
	
}
