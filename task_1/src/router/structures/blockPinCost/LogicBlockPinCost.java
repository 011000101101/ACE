package router.structures.blockPinCost;

public class LogicBlockPinCost extends BlockPinCost {

	/**
	 * usage counter for left input pin
	 * (always <=4, or invalid placement)
	 */
	private byte leftInPinUsage;
	/**
	 * usage counter for top input pin
	 * (always <=4, or invalid placement)
	 */
	private byte topInPinUsage;
	/**
	 * usage counter for right input pin
	 * (always <=4, or invalid placement)
	 */
	private byte rightInPinUsage;
	/**
	 * usage counter for bottom input pin
	 * (always <=4, or invalid placement)
	 */
	private byte bottomInPinUsage;
	
	//TODO can a Block have 2 nets attached to output? maybe add outPinXY, but why should it?
	
}
