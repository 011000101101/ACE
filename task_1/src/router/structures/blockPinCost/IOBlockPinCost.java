package router.structures.blockPinCost;

import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

public class IOBlockPinCost extends BlockPinCost {

	public IOBlockPinCost(NetlistBlock newBlock) {
		super(newBlock);
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
	private int usedCounterValidityDate= 0;
	
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
	public boolean limitExceeded(int iterationCounter) {
		if(iterationCounter == usedCounterValidityDate && usedCounter > 1) return true;
		else return false;
	}
	
}
