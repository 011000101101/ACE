package TimingAnalyzer;

import java.util.Collection;
import java.util.List;

import designAnalyzer.structures.Net;
import designAnalyzer.structures.StructureManager;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

public class TimingAnalyzer {
	
	private StructureManager structureManager;
	private Collection<Net> nets;
	
	
	public TimingAnalyzer(){
		
		structureManager= StructureManager.getInstance();
		
	}
	
	public void analyzeTiming(boolean routingFileProvided){
		
		nets= structureManager.getNetCollection();
		
		if(routingFileProvided){
			startAnalyzeTiming();
		}
		else{
			estimateTiming();
		}
	}

	/**
	 * estimate the timing of the whole design and output results
	 */
	private void estimateTiming() {
		
		Net criticalNet= null;
		int criticalPathLength= -1;
		
		for(Net n : nets){
			int temp= estimateSingleNet(n);
			if(temp > criticalPathLength){
				criticalPathLength= temp;
				criticalNet= n;
			}
		}
		
	}

	/**
	 * determines the estimated length of the estimated critical path of a net<br>
	 * and annotates the sink causing the critical path back into the processed net
	 * @param currentNet the net to estimate the critical path length of
	 * @return estimated length of the estimated critical path of the given net
	 */
	private int estimateSingleNet(Net currentNet) {
		
		NetlistBlock source= currentNet.getSource();
		List<NetlistBlock> sinks= currentNet.getSinks();
		int criticalPathLength= -1;
		
		for(NetlistBlock b : sinks){
			int temp= estimateSinglePath(source, b);
			if(temp > criticalPathLength){
				criticalPathLength= temp;
				currentNet.setCriticalSink(b);
			}
		}
		
		return criticalPathLength;
		
	}

	/**
	 * estimates the delay between one source and one sink by computing the shortest possible path and its delays without regard to resource usage by other paths or nets
	 * @param source source node of the path, <br>
	 * either an IOBlock or a logicBlock<br>
	 * <br>
	 * @param b sink node of the path, <br>
	 * either an IOBlock or a logicBlock<br>
	 * <br>
	 * @return estimated length of the path in the unit of time defined in the parameterManager
	 */
	private int estimateSinglePath(NetlistBlock source, NetlistBlock b) {
		
		int direction= computeDirection(source.getX(), source.getY(), b.getX(), b.getY());
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * computes the relative direction from the source block towards the sink block, <br>
	 * <br>
	 * coded as integer as follows (position of sink relative to source):<br>
	 * top: 0<br>
	 * top-right: 1<br>
	 * (...)<br>
	 * left: 7<br>
	 * top-left: 8<br>
	 * <br>
	 * (coordinates (0,0) are bottom left of grid)<br>
	 * @param xSource X coordinate of source node
	 * @param ySource Y coordinate of source node
	 * @param xSink X coordinate of sink node
	 * @param ySink Y coordinate of sink node
	 * @return
	 */
	private int computeDirection(int xSource, int ySource, int xSink, int ySink) {
		if(xSource == xSink){
			if(ySource < ySink) return 0;
			else return 4;
		}
	}

	private int estimateDelayBetweenTwoChannels(){
		// TODO Auto-generated method stub
		return 0;
	}

	private void startAnalyzeTiming() {
		// TODO Auto-generated method stub
		
	}

}
