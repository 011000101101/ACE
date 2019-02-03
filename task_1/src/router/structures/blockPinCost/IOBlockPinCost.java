package router.structures.blockPinCost;

public class IOBlockPinCost extends BlockPinCost {

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
	
	public double getInPinCost() {
		//TODO return static value? IO pins can only be used once in routing, or placement is invalid
		return 0;
	}
	
	public double getOutPinCost() {
		//TODO return static value? IO pins can only be used once in routing, or placement is invalid
		return 0;
	}
	
	public boolean getLeftOrRight() {
		return leftOrRight;
	}
	
	public boolean getLeftOrTop() {
		return leftOrTop;
	}
	
}