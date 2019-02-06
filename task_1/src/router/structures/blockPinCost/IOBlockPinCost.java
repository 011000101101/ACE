package router.structures.blockPinCost;

import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

public class IOBlockPinCost extends BlockPinCost {

	public IOBlockPinCost(NetlistBlock newBlock, int X_GRID_SIZE) {
		super(newBlock);
		if(newBlock.getX() == 0) {
			leftOrRight= true;
			leftOrTop= true;
		}
		else if(newBlock.getY() == 0) {
			leftOrRight= false;
			leftOrTop= false;
		}
		else if(newBlock.getX() == X_GRID_SIZE) {
			leftOrRight= true;
			leftOrTop= false;
		}
		else{
			leftOrRight= false;
			leftOrTop= true;
		}
	}

	/**
	 * flag if it is (left or right) IO block
	 */
	private boolean leftOrRight;
	
	/**
	 * flag if it is (left or Top) IO block
	 */
	private boolean leftOrTop;
	
	double inPinCost;
	
	double outPinCost;
	
	private int usedCounter= 0;
	private int usedCounterValidityDate= -1;
	
//	public double getInPinCost() {
//		//T ODO return static value? IO pins can only be used once in routing, or placement is invalid
//		return 0;
//	}
	
//	public double getOutPinCost() {
//		//T ODO return static value? IO pins can only be used once in routing, or placement is invalid
//		return 0;
//	}
	
	public boolean getLeftOrRight() {
		return leftOrRight;
	}
	
	public boolean getLeftOrTop() {
		return leftOrTop;
	}

	@Override
	public boolean limitExceeded(int globalIterationCounter) {
		if(globalIterationCounter == usedCounterValidityDate && usedCounter > 1) return true;
		else return false;
	}
	
	public void setInPinUsed(int globalIterationCounter) {
		if(globalIterationCounter == usedCounterValidityDate) usedCounter++;
		else{
			usedCounter= 1;
			usedCounterValidityDate= globalIterationCounter;
		}
	}
	
	public int getInPinUsedCounter(int globalIterationCounter) {
		if(globalIterationCounter == usedCounterValidityDate) return usedCounter;
		else return 0;
	}

	@Override
	public void resetCounters() {
		usedCounterValidityDate= -1;
	}

	@Override
	public String getUsedCounters(int globalIterationCounter) {
		return "[" + usedCounter + "]";
	}
	
}
