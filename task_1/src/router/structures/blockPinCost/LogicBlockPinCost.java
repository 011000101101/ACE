package router.structures.blockPinCost;

import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

public class LogicBlockPinCost extends BlockPinCost {

	public LogicBlockPinCost(NetlistBlock newBlock) {
		super(newBlock);
	}

	/**
	 * usage counter for left input pin
	 * (always <=4, or invalid placement)
	 */
	private int leftInPinUsedCounter= 0;
	/**
	 * usage counter for top input pin
	 * (always <=4, or invalid placement)
	 */
	private int topInPinUsedCounter= 0;
	/**
	 * usage counter for right input pin
	 * (always <=4, or invalid placement)
	 */
	private int rightInPinUsedCounter= 0;
	/**
	 * usage counter for bottom input pin
	 * (always <=4, or invalid placement)
	 */
	private int bottomInPinUsedCounter= 0;
	
	private int usedCounterValidityDate= 0;

	@Override
	public boolean limitExceeded(int globalIterationCounter) {
		if(globalIterationCounter == usedCounterValidityDate && (leftInPinUsedCounter > 1 || topInPinUsedCounter > 1 || rightInPinUsedCounter > 1 || bottomInPinUsedCounter > 1)) return true;
		else return false;
	}
	
	public int getLeftInPinUsedCounter(int globalIterationCounter) {
		if(usedCounterValidityDate == globalIterationCounter) return leftInPinUsedCounter;
		else return 0;
	}
	
	public int getTopInPinUsedCounter(int globalIterationCounter) {
		if(usedCounterValidityDate == globalIterationCounter) return topInPinUsedCounter;
		else return 0;
	}
	
	public int getRightInPinUsedCounter(int globalIterationCounter) {
		if(usedCounterValidityDate == globalIterationCounter) return rightInPinUsedCounter;
		else return 0;
	}
	
	public int getBottomInPinUsedCounter(int globalIterationCounter) {
		if(usedCounterValidityDate == globalIterationCounter) return bottomInPinUsedCounter;
		else return 0;
	}

	public void setLeftInPinUsed(int globalIterationCounter) {
		if(usedCounterValidityDate == globalIterationCounter) leftInPinUsedCounter++;
		else {
			leftInPinUsedCounter= 1;
			topInPinUsedCounter= 0;
			rightInPinUsedCounter= 0;
			bottomInPinUsedCounter= 0;
			usedCounterValidityDate= globalIterationCounter;
		}
	}

	public void setTopInPinUsed(int globalIterationCounter) {
		if(usedCounterValidityDate == globalIterationCounter) topInPinUsedCounter++;
		else {
			leftInPinUsedCounter= 0;
			topInPinUsedCounter= 1;
			rightInPinUsedCounter= 0;
			bottomInPinUsedCounter= 0;
			usedCounterValidityDate= globalIterationCounter;
		}
	}

	public void setRightInPinUsed(int globalIterationCounter) {
		/*if(usedCounterValidityDate == globalIterationCounter)*/ rightInPinUsedCounter++;
//		else {
//			leftInPinUsedCounter= 0;
//			topInPinUsedCounter= 0;
//			rightInPinUsedCounter= 1;
//			bottomInPinUsedCounter= 0;
//			usedCounterValidityDate= globalIterationCounter;
//		}
	}

	public void setBottomInPinUsed(int globalIterationCounter) {
		if(usedCounterValidityDate == globalIterationCounter) bottomInPinUsedCounter++;
		else {
			leftInPinUsedCounter= 0;
			topInPinUsedCounter= 0;
			rightInPinUsedCounter= 0;
			bottomInPinUsedCounter= 1;
			usedCounterValidityDate= globalIterationCounter;
		}
	}

	@Override
	public void resetCounters() {
		usedCounterValidityDate= -1;
	}

	@Override
	public String getUsedCounters(int globalIterationCounter) {
		return "[" + leftInPinUsedCounter + "][" + topInPinUsedCounter + "][" + rightInPinUsedCounter + "][" + bottomInPinUsedCounter + "]";
	}
	
	//... can a Block have 2 nets attached to output? maybe add outPinXY, but why should it?
	
	//TODO increase counters
	
}
