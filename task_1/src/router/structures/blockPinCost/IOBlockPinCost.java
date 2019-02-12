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
		else if(newBlock.getX() == X_GRID_SIZE + 1) {
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
	 * flag if it is (left or top) IO block
	 */
	private boolean leftOrTop;
	
	/**
	 * counter for how many times this IOBlockPin is used
	 */
	private int usedCounter= 0;
	
//	public double getInPinCost() {
//		//T ODO return static value? IO pins can only be used once in routing, or placement is invalid
//		return 0;
//	}
	
//	public double getOutPinCost() {
//		//T ODO return static value? IO pins can only be used once in routing, or placement is invalid
//		return 0;
//	}
	
	/**
	 * check if it is a (left or right) IO block
	 * @return
	 */
	public boolean getLeftOrRight() {
		return leftOrRight;
	}
	
	/**
	 * check if it is a (left or Top) IO block
	 * @return
	 */
	public boolean getLeftOrTop() {
		return leftOrTop;
	}

	@Override
	public boolean limitExceeded(int iterationCounter, int globalIterationCounter) {
		if(usedCounterValidityDate == iterationCounter && usedCounterValidityDate2 == globalIterationCounter && usedCounter > 1) return true;
		else return false;
	}
	
	public void incUsedCounter() {
		usedCounter++;
	}

	/**
	 * set used counter to one and also set used counter validity date
	 * @param iterationCounter
	 * @param globalIterationCounter
	 */
	public void setUsedCounterToOne(int iterationCounter, int globalIterationCounter) {
		usedCounter= 1;
		usedCounterValidityDate= iterationCounter;
		usedCounterValidityDate2= globalIterationCounter;
	}
	
	/**
	 * only returns IPin used counter if validity date is correct
	 * @param iterationCounter
	 * @param globalIterationCounter
	 * @return
	 */
	public int getInPinUsedCounter(int iterationCounter, int globalIterationCounter) {
		if(iterationCounter == usedCounterValidityDate && globalIterationCounter == usedCounterValidityDate2) return usedCounter;
		else return 0;
	}

	@Override
	public void resetCounters() {
		usedCounterValidityDate= -1;
		usedCounterValidityDate2= -1;
	}

	@Override
	public String getUsedCounters(int globalIterationCounter) {
		return "[" + usedCounter + "]";
	}
	
}
