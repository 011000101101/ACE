package router.structures.resourceWithCost;

import java.util.PriorityQueue;

import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import designAnalyzer.ParameterManager;

public class SourceDummy extends ResourceWithCost {

	//a dummy representing source block. used as NodeOfResource(root) of a tree structure
	NetlistBlock source;
	
	String innerIterationCounter;
	String iterationCounter;
	String globalIterationCounter;
	String currentChannelWidth;

	public SourceDummy(NetlistBlock newSource) {
		super(null);
		source= newSource;
	}
	
	public SourceDummy(NetlistBlock newSource, String innerIterationCounter, String iterationCounter,
			String globalIterationCounter, String currentChannelWidth) {
		super(null);
		source= newSource;
		this.innerIterationCounter= innerIterationCounter;
		this.iterationCounter= iterationCounter;
		this.globalIterationCounter= globalIterationCounter;
		this.currentChannelWidth= currentChannelWidth;
	}

	@Override
	public int getX() {
		return source.getX();
	}

	@Override
	public int getY() {
		return source.getY();
	}

	@Override
	public double computeCost(double pFak) {
		return 0; //cost= 0
	}

	@Override
	public void incUsedCounter() {
		// nothing to do here
	}

	@Override
	public void decUsedCounter() {
		// nothing to do here
	}

	@Override
	public int getUsedCounter() {
		return -1; //no used counter
	}

	@Override
	public void addChannelToPriorityQueue(PriorityQueue<ResourceWithCost> pQ) {
		// ignore
	}

	@Override
	public boolean neighbours(ResourceWithCost branchingPoint) {
		
		if(!(branchingPoint instanceof ChannelWithCost)) return false;
		
		ParameterManager parameterManager= ParameterManager.getInstance();
		
		if(source instanceof IOBlock) {
			//bottom pin, top IO
			if(source.getX() == branchingPoint.getX() && source.getY() == parameterManager.Y_GRID_SIZE + 1 && branchingPoint.getY() == parameterManager.Y_GRID_SIZE && ((ChannelWithCost) branchingPoint).getHorizontal()) return true;
			//left pin, right IO
			if(source.getY() == branchingPoint.getY() && source.getX() == parameterManager.X_GRID_SIZE + 1 && branchingPoint.getX() == parameterManager.X_GRID_SIZE && !((ChannelWithCost) branchingPoint).getHorizontal()) return true;
			//top pin, bottom IO
			if(source.getX() == branchingPoint.getX() && source.getY() == 0 && branchingPoint.getY() == 0 && ((ChannelWithCost) branchingPoint).getHorizontal()) return true;
			//right pin, left IO
			if(source.getY() == branchingPoint.getY() && source.getX() == 0 && branchingPoint.getX() == 0 && !((ChannelWithCost) branchingPoint).getHorizontal()) return true;
			
		}
		else { //logic block, allow right and bottom pins
			//right pin
			if(source.getX() == branchingPoint.getX() && source.getY() == branchingPoint.getY() && !((ChannelWithCost) branchingPoint).getHorizontal()) return true;
			//bottom pin
			if(source.getX() == branchingPoint.getX() && source.getY() == branchingPoint.getY() + 1 && ((ChannelWithCost) branchingPoint).getHorizontal()) return true;
		}
		
		return false;
		
	}

	public NetlistBlock getBlock() {
		return source;
	}


	@Override
	public String toString() {
		return "values were: innerIterationCounter: [" + innerIterationCounter + "], iterationCounter: [" + iterationCounter + "], globalIterationCounter: [" + globalIterationCounter + "], currentChannelWidth: [" + currentChannelWidth + "]";
	}

	@Override
	protected double computeCostDistEst(int sinkX, int sinkY) {
		//do nothing
		return -1;
	}
}
