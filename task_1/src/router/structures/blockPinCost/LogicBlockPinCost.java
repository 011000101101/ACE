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
	

	@Override
	public boolean limitExceeded(int iterationCounter, int globalIterationCounter) {
		if(usedCounterValidityDate == iterationCounter && usedCounterValidityDate2 == globalIterationCounter && (leftInPinUsedCounter > 1 || topInPinUsedCounter > 1 || rightInPinUsedCounter > 1 || bottomInPinUsedCounter > 1)) return true;
		else return false;
	}
	
	/**
	 * only returns left IPin used counter if validity date is correct
	 * @param iterationCounter
	 * @param globalIterationCounter
	 * @return
	 */
	public int getLeftInPinUsedCounter(int iterationCounter, int globalIterationCounter) {
		if(usedCounterValidityDate == iterationCounter && usedCounterValidityDate2 == globalIterationCounter) return leftInPinUsedCounter;
		else return 0;
	}
	
	/**
	 * only returns top IPin used counter if validity date is correct
	 * @param iterationCounter
	 * @param globalIterationCounter
	 * @return
	 */
	public int getTopInPinUsedCounter(int iterationCounter, int globalIterationCounter) {
		if(usedCounterValidityDate == iterationCounter && usedCounterValidityDate2 == globalIterationCounter) return topInPinUsedCounter;
		else return 0;
	}
	
	/**
	 * only returns right IPin used counter if validity date is correct
	 * @param iterationCounter
	 * @param globalIterationCounter
	 * @return
	 */
	public int getRightInPinUsedCounter(int iterationCounter, int globalIterationCounter) {
		if(usedCounterValidityDate == iterationCounter && usedCounterValidityDate2 == globalIterationCounter) return rightInPinUsedCounter;
		else return 0;
	}
	
	/**
	 * only returns bottom IPin used counter if validity date is correct
	 * @param iterationCounter
	 * @param globalIterationCounter
	 * @return
	 */
	public int getBottomInPinUsedCounter(int iterationCounter, int globalIterationCounter) {
		if(usedCounterValidityDate == iterationCounter && usedCounterValidityDate2 == globalIterationCounter) return bottomInPinUsedCounter;
		else return 0;
	}

	public void incLeftUsedCounter() {
		leftInPinUsedCounter++;
	}

	public void incTopUsedCounter() {
		topInPinUsedCounter++;
	}

	public void incRightUsedCounter() {
		rightInPinUsedCounter++;
	}

	public void incBottomUsedCounter() {
		bottomInPinUsedCounter++;
	}

	public void setLeftUsedCounterToOne(int iterationCounter, int globalIterationCounter) {
		leftInPinUsedCounter= 1;
			topInPinUsedCounter= 0;
			rightInPinUsedCounter= 0;
			bottomInPinUsedCounter= 0;
			usedCounterValidityDate= iterationCounter;
			usedCounterValidityDate2= globalIterationCounter;
	}

	public void setTopUsedCounterToOne(int iterationCounter, int globalIterationCounter) {
		leftInPinUsedCounter= 0;
			topInPinUsedCounter= 1;
			rightInPinUsedCounter= 0;
			bottomInPinUsedCounter= 0;
			usedCounterValidityDate= iterationCounter;
			usedCounterValidityDate2= globalIterationCounter;
	}

	public void setRightUsedCounterToOne(int iterationCounter, int globalIterationCounter) {
		leftInPinUsedCounter= 0;
			topInPinUsedCounter= 0;
			rightInPinUsedCounter= 1;
			bottomInPinUsedCounter= 0;
			usedCounterValidityDate= iterationCounter;
			usedCounterValidityDate2= globalIterationCounter;
	}

	public void setBottomUsedCounterToOne(int iterationCounter, int globalIterationCounter) {
		leftInPinUsedCounter= 0;
			topInPinUsedCounter= 0;
			rightInPinUsedCounter= 0;
			bottomInPinUsedCounter= 1;
			usedCounterValidityDate= iterationCounter;
			usedCounterValidityDate2= globalIterationCounter;
	}

	@Override
	public void resetCounters() {
		usedCounterValidityDate= -1;
		usedCounterValidityDate2= -1;
	}

	@Override
	public String getUsedCounters(int globalIterationCounter) {
		return "[" + leftInPinUsedCounter + "][" + topInPinUsedCounter + "][" + rightInPinUsedCounter + "][" + bottomInPinUsedCounter + "]";
	}
	
	//... can a Block have 2 nets attached to output? maybe add outPinXY, but why should it?
	
	//TODO increase counters
	
}
