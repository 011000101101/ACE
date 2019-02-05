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
	public double computeCost(int pFak, int currentChannelWidth, int iterationCounter) { // + 1 - 1 = 0
		double pv = (double) 1 + (double) Math.max(0, (double) (getUsedCounter(iterationCounter) /* + 1 - 1 */) * 0.5 * (double) pFak );
		return hv * pv * 0.95; //bv = 0.95, input pin...
	}



	@Override
	public void setUsed(int iterationCounter) {
		if(sinkCost instanceof IOBlockPinCost) ((IOBlockPinCost) sinkCost).setInPinUsed(iterationCounter);
		else {
			if(leftOrRight) { //is left or right
				if(leftOrBottom) ((LogicBlockPinCost) sinkCost).setLeftInPinUsed(iterationCounter);
				else ((LogicBlockPinCost) sinkCost).setRightInPinUsed(iterationCounter);
			}
			else { //is top or bottom
				if(leftOrBottom) ((LogicBlockPinCost) sinkCost).setBottomInPinUsed(iterationCounter);
				else ((LogicBlockPinCost) sinkCost).setTopInPinUsed(iterationCounter);
			}
		}
	}



	@Override
	public int getUsedCounter(int iterationCounter) {
		if(sinkCost instanceof IOBlockPinCost) return ((IOBlockPinCost) sinkCost).getInPinUsedCounter(iterationCounter);
		else {
			if(leftOrRight) { //is left or right
				if(leftOrBottom) return ((LogicBlockPinCost) sinkCost).getLeftInPinUsedCounter(iterationCounter);
				else return ((LogicBlockPinCost) sinkCost).getRightInPinUsedCounter(iterationCounter);
			}
			else { //is top or bottom
				if(leftOrBottom) return ((LogicBlockPinCost) sinkCost).getBottomInPinUsedCounter(iterationCounter);
				else return ((LogicBlockPinCost) sinkCost).getTopInPinUsedCounter(iterationCounter);
			}
		}
	}
	
	public BlockPinCost getSinkCost(){
		return sinkCost;
	}
	
	public int getPinNum() {
		if(leftOrRight) { //is left or right
			if(leftOrBottom) return 1;
			else return 3;
		}
		else { //is top or bottom
			if(leftOrBottom) return 0;
			else return 2;
		}
	}



	@Override
	public boolean neighbours(ResourceWithCost branchingPoint) {
		// won't be called...
		return false;
	}

}
