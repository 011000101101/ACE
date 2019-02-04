package router.structures.blockPinCost;

public class LogicBlockPinCost extends BlockPinCost {

	/**
	 * usage counter for left input pin
	 * (always <=4, or invalid placement)
	 */
	private byte leftInPinUsedCounter= 0;
	/**
	 * usage counter for top input pin
	 * (always <=4, or invalid placement)
	 */
	private byte topInPinUsedCounter= 0;
	/**
	 * usage counter for right input pin
	 * (always <=4, or invalid placement)
	 */
	private byte rightInPinUsedCounter= 0;
	/**
	 * usage counter for bottom input pin
	 * (always <=4, or invalid placement)
	 */
	private byte bottomInPinUsedCounter= 0;
	
	private int usedCounterValidityDate= 0;

	@Override
	public boolean limitExceeded(int iterationCounter) {
		if(iterationCounter == usedCounterValidityDate && (leftInPinUsedCounter > 1 || topInPinUsedCounter > 1 || rightInPinUsedCounter > 1 || bottomInPinUsedCounter > 1)) return true;
		else return false;
	}
	
	//TODO can a Block have 2 nets attached to output? maybe add outPinXY, but why should it?
	
}
