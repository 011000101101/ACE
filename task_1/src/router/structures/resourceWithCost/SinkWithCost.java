package router.structures.resourceWithCost;

import java.util.PriorityQueue;

import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import router.structures.blockPinCost.BlockPinCost;
import router.structures.blockPinCost.IOBlockPinCost;
import router.structures.blockPinCost.LogicBlockPinCost;

public class SinkWithCost extends ResourceWithCost {
	
	BlockPinCost sinkCost;
	
	/**
	 * flag if it is (left or right) pin
	 */
	private boolean leftOrRight;
	
	/**
	 * flag if it is (left or Top) (or (right or bottom)) pin
	 */
	private boolean leftOrBottom;


	public SinkWithCost(BlockPinCost newSinkCost, ChannelWithCost newPrevious) {
		super(newPrevious);
		sinkCost= newSinkCost;
		if(newPrevious.getX() == sinkCost.getX() && newPrevious.getY() == sinkCost.getY()) leftOrBottom= false; //right or top
		else leftOrBottom= true; //left or bottom
		if(newPrevious.getHorizontal()) leftOrRight= false; //bottom or top
		else leftOrRight= true; //left or right
	}

	
	
	@Override
	public int getX() {
		return sinkCost.getX();
	}

	@Override
	public int getY() {
		return sinkCost.getY();
	}



	@Override
	public void addChannelToPriorityQueue(PriorityQueue<ResourceWithCost> pQ) {
		//nothing
	}



	@Override
	public double computeCost(int pFak, int currentChannelWidth, int innerIterationCounter, int globalIterationCounter) { // + 1 - 1 = 0
		double pv = (double) 1 + /*(double) Math.max(0,*/ (double) (getUsedCounter(globalIterationCounter) /* + 1 - 1 */) * (double) 50 * (double) pFak /*)*/;
//		if(sinkCost instanceof LogicBlockPinCost && getUsedCounter(iterationCounter) != 0) System.err.println("flag 014");
		return hv * pv * 0.95; //bv = 0.95, input pin...
	}



	@Override
	public void incUsedCounter() {
		if(sinkCost instanceof IOBlockPinCost) ((IOBlockPinCost) sinkCost).incUsedCounter();
		else {
			if(leftOrRight) { //is left or right
				if(leftOrBottom) ((LogicBlockPinCost) sinkCost).incLeftUsedCounter();
				else ((LogicBlockPinCost) sinkCost).incRightUsedCounter();
			}
			else { //is top or bottom
				if(leftOrBottom) ((LogicBlockPinCost) sinkCost).incBottomUsedCounter();
				else ((LogicBlockPinCost) sinkCost).incTopUsedCounter();
			}
		}
	}
	
	protected void setUsedCounterToOne() {
		if(sinkCost instanceof IOBlockPinCost) ((IOBlockPinCost) sinkCost).setUsedCounterToOne();
		else {
			if(leftOrRight) { //is left or right
				if(leftOrBottom) ((LogicBlockPinCost) sinkCost).setLeftUsedCounterToOne();
				else ((LogicBlockPinCost) sinkCost).setRightUsedCounterToOne();
			}
			else { //is top or bottom
				if(leftOrBottom) ((LogicBlockPinCost) sinkCost).setBottomUsedCounterToOne();
				else ((LogicBlockPinCost) sinkCost).setTopUsedCounterToOne();
			}
		}
	}



	@Override
	public int getUsedCounter(int globalIterationCounter) {
		if(sinkCost instanceof IOBlockPinCost) return ((IOBlockPinCost) sinkCost).getInPinUsedCounter(globalIterationCounter);
		else {
			if(leftOrRight) { //is left or right
				if(leftOrBottom) return ((LogicBlockPinCost) sinkCost).getLeftInPinUsedCounter(globalIterationCounter);
				else return ((LogicBlockPinCost) sinkCost).getRightInPinUsedCounter(globalIterationCounter);
			}
			else { //is top or bottom
				if(leftOrBottom) return ((LogicBlockPinCost) sinkCost).getBottomInPinUsedCounter(globalIterationCounter);
				else return ((LogicBlockPinCost) sinkCost).getTopInPinUsedCounter(globalIterationCounter);
			}
		}
	}
	
	public BlockPinCost getSinkCost(){
		return sinkCost;
	}
	
	public int getPinNum() {
		if(leftOrRight) { //is left or right
			if(leftOrBottom) return 1; //left
			else return 3; //right
		}
		else { //is top or bottom
			if(leftOrBottom) return 0; //bottom
			else return 2; //top
		}
	}



	@Override
	public boolean neighbours(ResourceWithCost branchingPoint) {
		// won't be called...
		return false;
	}
	
	@Override
	public String toString() {
		return "IPin {" + sinkCost.getBlock().getName() + "} @ (" + sinkCost.getX() + "," + sinkCost.getY() + "), " + (leftOrRight ? (leftOrBottom ? "left" : "right") : (leftOrBottom ? "bottom" : "right"));
	}



	@Override
	public void resetCounters() {
		sinkCost.resetCounters();
	}

}
