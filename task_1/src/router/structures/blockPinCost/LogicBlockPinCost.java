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
	public boolean limitExceeded(int iterationCounter) {
		if(iterationCounter == usedCounterValidityDate && (leftInPinUsedCounter > 1 || topInPinUsedCounter > 1 || rightInPinUsedCounter > 1 || bottomInPinUsedCounter > 1)) return true;
		else return false;
	}
	
	public int getLeftInPinUsedCounter(int iterationCounter) {
		if(usedCounterValidityDate == iterationCounter) return leftInPinUsedCounter;
		else return 0;
	}
	
	public int getTopInPinUsedCounter(int iterationCounter) {
		if(usedCounterValidityDate == iterationCounter) return topInPinUsedCounter;
		else return 0;
	}
	
	public int getRightInPinUsedCounter(int iterationCounter) {
		if(usedCounterValidityDate == iterationCounter) return rightInPinUsedCounter;
		else return 0;
	}
	
	public int getBottomInPinUsedCounter(int iterationCounter) {
		if(usedCounterValidityDate == iterationCounter) return bottomInPinUsedCounter;
		else return 0;
	}

	public void setLeftInPinUsed(int iterationCounter) {
		if(usedCounterValidityDate == iterationCounter) leftInPinUsedCounter++;
		else {
			leftInPinUsedCounter= 1;
			topInPinUsedCounter= 0;
			rightInPinUsedCounter= 0;
			bottomInPinUsedCounter= 0;
			usedCounterValidityDate= iterationCounter;
		}
	}

	public void setTopInPinUsed(int iterationCounter) {
		if(usedCounterValidityDate == iterationCounter) topInPinUsedCounter++;
		else {
			leftInPinUsedCounter= 0;
			topInPinUsedCounter= 1;
			rightInPinUsedCounter= 0;
			bottomInPinUsedCounter= 0;
			usedCounterValidityDate= iterationCounter;
		}
	}

	public void setRightInPinUsed(int iterationCounter) {
		if(usedCounterValidityDate == iterationCounter) rightInPinUsedCounter++;
		else {
			leftInPinUsedCounter= 0;
			topInPinUsedCounter= 0;
			rightInPinUsedCounter= 1;
			bottomInPinUsedCounter= 0;
			usedCounterValidityDate= iterationCounter;
		}
	}

	public void setBottomInPinUsed(int iterationCounter) {
		if(usedCounterValidityDate == iterationCounter) bottomInPinUsedCounter++;
		else {
			leftInPinUsedCounter= 0;
			topInPinUsedCounter= 0;
			rightInPinUsedCounter= 0;
			bottomInPinUsedCounter= 1;
			usedCounterValidityDate= iterationCounter;
		}
	}
	
	//... can a Block have 2 nets attached to output? maybe add outPinXY, but why should it?
	
	//TODO increase counters
	
}
