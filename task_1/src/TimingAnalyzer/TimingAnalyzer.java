package TimingAnalyzer;

import java.util.Collection;
import java.util.List;

import designAnalyzer.ParameterManager;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.StructureManager;
import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

public class TimingAnalyzer {
	
	private StructureManager structureManager;
	private ParameterManager parameterManager;
	private Collection<Net> nets;
	

	private int xMax;
	private int yMax;
	
	private int tIOToChan;
	private int tChanToIO;
	private int tChanToChan;
	private int tLBToChan;
	private int tChanToLB;
	
	public TimingAnalyzer(){
		
		structureManager= StructureManager.getInstance();
		parameterManager= ParameterManager.getInstance();

		xMax= parameterManager.getXGridSize();
		yMax= parameterManager.getYGridSize();
		
		//TODO initialize times
		
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
	 * @param sink sink node of the path, <br>
	 * either an IOBlock or a logicBlock<br>
	 * <br>
	 * @return estimated length of the path in the unit of time defined in the parameterManager
	 */
	private int estimateSinglePath(NetlistBlock source, NetlistBlock sink) {
		
		int horizontalChannelsUsed=-1;
		int verticalChannelsUsed=-1;
		
		/**
		 * delay from channel into sink
		 */
		int tIn= tChanToLB;
		
		/**
		 * delay from source to channel
		 */
		int tOut= tLBToChan;
		
		if(source instanceof IOBlock){
			tOut= tChanToIO;
		}

		if(sink instanceof IOBlock){
			tIn= tIOToChan;
		}
		
		int direction= computeDirection(source.getX(), source.getY(), sink.getX(), sink.getY());
		
		switch (direction){
		
			case 0: // sink directly above source
				
				horizontalChannelsUsed= 0; //use right output and input pin of source and sink
				verticalChannelsUsed= sink.getY()-source.getY() + 1; //from right side to right side (or from left to left if both are on right border)
				
				if(source.getY() == 0){ //[...] is iopad at bottom, use top out pin and channel instead of right, rout it to vertical channel at top right
					horizontalChannelsUsed++;
					verticalChannelsUsed--;
				}
				if(sink.getY() == yMax){ //[...] is iopad at top, use bottom out pin and channel instead of right, rout it to vertical channel at bottom right
					horizontalChannelsUsed++;
					verticalChannelsUsed--;
				}
				
			break;
			
			case 4: // sink directly below source
				
				if(sink.getY() + 1 == source.getY()){
					horizontalChannelsUsed= 1;
					verticalChannelsUsed=0;
				}
				else{
					horizontalChannelsUsed= 2; //use bottom output and top input pin of source and sink
					verticalChannelsUsed= source.getY() - sink.getY() -1; 

					if(source.getX() == 0){ //[...] is iopad at left border
						horizontalChannelsUsed= 0;
						verticalChannelsUsed+= 2;
					}
					if(source.getX() == xMax){ //[...] is iopad at right border
						horizontalChannelsUsed= 0;
						verticalChannelsUsed+= 2;
					}
				}
				
			break;
			
			case 2: // sink directly right of source
				
				if(sink.getX() - 1 == source.getX()){
					horizontalChannelsUsed= 0;
					verticalChannelsUsed=1;
				}
				else{
					horizontalChannelsUsed= sink.getX() - source.getX() - 1;
					verticalChannelsUsed= 2; 

					if(source.getY() == 0){ //[...] is iopad at bottom border
						verticalChannelsUsed = 0;
						horizontalChannelsUsed+= 2;
					}
					if(source.getY() == yMax){ //[...] is iopad at top border
						verticalChannelsUsed= 0;
						horizontalChannelsUsed += 2;
					}
				}
				
			break;
			
			case 6: // sink directly left of source
				
				 horizontalChannelsUsed= source.getX() - sink.getX(); //bottom out
				 verticalChannelsUsed= 1; //right in

				if(source.getY() == 0){ //[...] is iopad at bottom
					horizontalChannelsUsed--; //top in
					verticalChannelsUsed++;
				}
				if(source.getY() == yMax){ //[...] is iopad at top
					horizontalChannelsUsed--; //bottom in
					verticalChannelsUsed++;
				}
				
			break;
			
			case 1: //sink top right of source
				
				horizontalChannelsUsed= sink.getX() - source.getX(); //right out
				verticalChannelsUsed= sink.getY() - source.getY(); //bottom in

				if(source.getY() == 0){ //[...] is iopad at bottom
					horizontalChannelsUsed++; //top out
					verticalChannelsUsed--;
				}
				if(sink.getX() == xMax){ //[...] is iopad at right border
					horizontalChannelsUsed--; //right in
					verticalChannelsUsed++;
				}
				
			break;
			
			//TODO cases 3, 5, 7 (copy pattern of case 1)
				
		}
		
		if(source instanceof IOBlock){
			if(sink instanceof IOBlock){
				
			}
		}
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
			if(ySource < ySink) return 0; //sink directly above source
			else return 4; //sink directly below source
		}
		if(ySource == ySink){
			if(xSource < xSink) return 2; //sink directly right of source
			else return 6; //sink directly left of source
		}
		//neither x nor y coordinate of source and sink are equal if this point is reached
		if(xSource < xSink){ //sink right of source
			if(ySource < ySink) return 1; //sink above source
			else return 3; //sink below source
		}
		else{ //sink left of source
			if(ySource < ySink) return 1; //sink above source
			else return 3; //sink below source
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
