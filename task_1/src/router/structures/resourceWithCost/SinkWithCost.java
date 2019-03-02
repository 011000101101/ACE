
package router.structures.resourceWithCost;

import java.util.PriorityQueue;

import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

public class SinkWithCost extends ResourceWithCost {
	
	/**
	 * input pin into the sink:
	 * 0: top pin
	 * 1: right pin
	 * 2: bottom pin
	 * 3: left pin
	 */
	private int pin;
	
	private NetlistBlock sinkingBlock;

	
	public SinkWithCost(NetlistBlock newSinkingBlock, ChannelWithCost newPrevious, int newPin) {
		super(newPrevious);
		sinkingBlock= newSinkingBlock;
		pin= newPin;
	}



	@Override
	public int getX() {
		return sinkingBlock.getX();
	}

	@Override
	public int getY() {
		return sinkingBlock.getY();
	}



	@Override
	public void addChannelToPriorityQueue(PriorityQueue<ResourceWithCost> pQ) {
		//nothing
	}



	@Override
	public double computeCost(double pFak, int currentChannelWidth) { // + 1 - 1 = 0
		double pv = (double) 1 + /*(double) Math.max(0,*/ (double) (getUsedCounter() /* + 1 - 1 */) * pFak /*)*/;
//		if(sinkCost instanceof LogicBlockPinCost && getUsedCounter(iterationCounter) != 0) System.err.println("flag 014");
		return hv * pv * 0.95; //bv = 0.95, input pin...
	}
	
	public NetlistBlock getSinkingBlock(){
		return sinkingBlock;
	}


	@Override
	public boolean neighbours(ResourceWithCost branchingPoint) {
		// won't be called...
		return false;
	}
	
	@Override
	public String toString() {
		return "IPin {" + sinkingBlock.getName() + "} @ (" + getX() + "," + getY() + "), " + ((pin > 1) ? ((pin == 3) ? "right" : "top") : ((pin == 0) ? "bottom" : "left"));
	}



	public int getPin() {
		return pin;
	}

}
