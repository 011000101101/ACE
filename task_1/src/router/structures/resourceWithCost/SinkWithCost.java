package router.structures.resourceWithCost;

import java.util.PriorityQueue;

import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

public class SinkWithCost extends ResourceWithCost {
	
	NetlistBlock sink;


	public SinkWithCost(NetlistBlock newSink, double newCost, ChannelWithCost newPrevious) {
		super(newCost, newPrevious);
		sink= newSink;
	}

	
	
	@Override
	public int getX() {
		return sink.getX();
	}

	@Override
	public int getY() {
		return sink.getY();
	}



	@Override
	public void addChannelToPriorityQueue(PriorityQueue<ChannelWithCost> pQ) {
		//nothing
	}

}
