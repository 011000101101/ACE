package designAnalyzer.timingAnalyzer;

import java.util.Collection;
import java.util.List;

import designAnalyzer.ParameterManager;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.StructureManager;
import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

/**
 * 
 * @author Dennis Grotz, Rumei Ma, Vincenz Mechler
 *
 */
public class TimingAnalyzer {
	
	private StructureManager structureManager;
	private ParameterManager parameterManager;
	private Collection<Net> nets;
	

	private int xMax;
	private int yMax;
	
	
	public TimingAnalyzer(){
		
		structureManager= StructureManager.getInstance();
		parameterManager= ParameterManager.getInstance();

		xMax= parameterManager.X_GRID_SIZE + 1;
		yMax= parameterManager.Y_GRID_SIZE + 1;
		
	}
	
	/**
	 * analyzes the timings of the design and outputs the results
	 * @param routingFileProvided true if a routing file has been provided and the timing can be analyzed exactly, false if timing has to be estimated
	 */
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
		
		printEstimatedCriticalPath(criticalNet, criticalPathLength);
		
	}

	private void printEstimatedCriticalPath(Net criticalNet, int criticalPathLength) {

		StringBuilder output= new StringBuilder();
		
		output.append("estimated critical path: \n \n");
		
		addTimingHeader(output);
		
		NetlistBlock source= criticalNet.getSource();
		NetlistBlock sink= criticalNet.getCriticalSink();
		
		//print source
		output.append((source instanceof IOBlock) ? "I_BLOCK" : "CLB(seq)");
		output.append("\t");
		output.append(source.getName());
		output.append("\t");
		output.append("(");
		output.append(source.getX());
		output.append(",");
		output.append(source.getY());
		output.append(")");
		if(source instanceof IOBlock) {
			output.append(source.getSubblk_1() ? 1 : 0);
		}
		output.append("\t");
		output.append("\t");
		output.append(0);
		

		output.append("\n"); //new line
		
		//print sink
		output.append((sink instanceof IOBlock) ? "O_BLOCK" : "CLB(seq)");
		output.append("\t");
		output.append(sink.getName());
		output.append("\t");
		output.append("(");
		output.append(sink.getX());
		output.append(",");
		output.append(sink.getY());
		output.append(")");
		if(sink instanceof IOBlock) {
			output.append(sink.getSubblk_1() ? 1 : 0);
		}
		output.append("\t");
		output.append("\t");
		output.append(criticalPathLength);

		output.append("\n"); //new line
		
		
		//TODO file output?
		System.out.println(output.toString());
		
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
		int tIn= parameterManager.T_FFIN + parameterManager.T_SWITCH;
		
		/**
		 * delay from source to channel
		 */
		int tOut= parameterManager.T_FFOUT + parameterManager.T_SWITCH;
		
		if(source instanceof IOBlock){
			tOut= parameterManager.T_IPAD + parameterManager.T_SWITCH;
		}

		if(sink instanceof IOBlock){
			tIn= parameterManager.T_OPAD + parameterManager.T_SWITCH;
		}
		
		int direction= computeDirection(source.getX(), source.getY(), sink.getX(), sink.getY());
		
		switch (direction){
		
			case 0: // sink directly above source
				
				horizontalChannelsUsed= 0; //use right output and input pin of source and sink
				verticalChannelsUsed= sink.getY()-source.getY() + 1; //from right side to right side (or from left to left if both are on right border)
				
				if(source.getY() == 0){ //[...] is iopad at bottom, use top out pin and channel instead of right, rout it to vertical channel at top right
					horizontalChannelsUsed++;
					verticalChannelsUsed--;
					if(sink.getY() == 1) { //sink directly above source
						verticalChannelsUsed= 0;
					}
				}
				if(sink.getY() == yMax){ //[...] is iopad at top, use bottom out pin and channel instead of right, rout it to vertical channel at bottom right
					horizontalChannelsUsed++;
					verticalChannelsUsed--;
				}
				
			break;
			
			case 4: // sink directly below source
				
				
				horizontalChannelsUsed= 2; //use bottom output and top input pin of source and sink
				verticalChannelsUsed= source.getY() - sink.getY() -1; 

				if(source.getX() == 0){ //[...] is iopad at left border
					horizontalChannelsUsed= 0;
					verticalChannelsUsed+= 2;
				}
				else if(source.getX() == xMax){ //[...] is iopad at right border
					horizontalChannelsUsed= 0;
					verticalChannelsUsed+= 2;
				}
				// not left or right IOBlocks
				else if(sink.getY() + 1 == source.getY()){
					horizontalChannelsUsed= 1;
					verticalChannelsUsed=0;
				}
				
			break;
			
			case 2: // sink directly right of source
				
				horizontalChannelsUsed= sink.getX() - source.getX() - 1;
				verticalChannelsUsed= 2; 

				if(source.getY() == 0){ //[...] is iopad at bottom border
					verticalChannelsUsed = 0;
					horizontalChannelsUsed+= 2;
				}
				else if(source.getY() == yMax){ //[...] is iopad at top border
					verticalChannelsUsed= 0;
					horizontalChannelsUsed += 2;
				}
				// not top or bottom IOBlocks
				else if(sink.getX() - 1 == source.getX()){ //if directly next to each other
					horizontalChannelsUsed= 0;
					verticalChannelsUsed=1;
				}
				
			break;
			
			case 6: // sink directly left of source
				
				 horizontalChannelsUsed= source.getX() - sink.getX(); //bottom out
				 verticalChannelsUsed= 1; //right in

				if(source.getY() == 0){ //[...] is iopad at bottom
					verticalChannelsUsed = 0;
					horizontalChannelsUsed+= 1;
				}
				else if(source.getY() == yMax){ //[...] is iopad at top
					verticalChannelsUsed = 0;
					horizontalChannelsUsed+= 1;
				}
				else if(source.getX() == xMax){
					horizontalChannelsUsed--; //left out instead of bottom
					if(!(source.getX() - 1 == sink.getX())){
						verticalChannelsUsed++; //first and last channel are not the same
					}
				}
				
			break;
			
			case 1: //sink top right of source
				//  ----------X    ---------- 
				// | ^------^ |   | ^------^ |
				// | |sourceO-|   | | sink I |
				// | L------J |   | L---I--J |
				//  ----------    X-----|---- 
				
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
			
			case 3: //sink bottom right of source
				//  ----------    X----T----- 
				// | ^------^ |   | ^--I---^ |
				// | |sourceO-|   | | sink | |
				// | L------J |   | L------J |
				//  ----------X    ---------- 
				
				horizontalChannelsUsed = sink.getX() - source.getX(); //right out
				verticalChannelsUsed = source.getY() - sink.getY(); //top in
				
				if(source.getY() == yMax) { // source is iopad at top
					horizontalChannelsUsed++; // bottom out
					verticalChannelsUsed--;
				}
				
				if(sink.getX() == xMax) { // sink is iopad at right border
					horizontalChannelsUsed--; //left in
					verticalChannelsUsed++;
				}
				
			break;
			
			case 5: //sink is bottom left of source
				//  ----------     ----------X
				// | ^------^ |   | ^------^ |
				// | |source| |   | | sink I-|
				// | L--O---J |   | L------J |
				// X----|-----     ---------- 
				
				horizontalChannelsUsed = source.getX() - sink.getX(); //bottom out
				verticalChannelsUsed = source.getY() - sink.getY(); //right in
				
				if(source.getX() == xMax) { // source is iopad at right border
					verticalChannelsUsed ++; //left out
					horizontalChannelsUsed --;
				}
				
				if(sink.getY() == 0) { // sink is iopad at bottom
					verticalChannelsUsed --; // top in
					horizontalChannelsUsed ++;
				}
			
			break;
			
			case 7: //sink is top left of source
				// X----------     ---------- 
				// | ^------^ |   | ^------^ |
				// | |source| |   | | sink I-|
				// | L--O---J |   | L------J |
				// L----|-----     ----------X
				
				horizontalChannelsUsed = source.getX() - sink.getX(); // bottom out
				verticalChannelsUsed = sink.getY() - source.getY() + 1; // right in
				
				if(source.getY() == 0) { //source is at bottom
					verticalChannelsUsed --; //top out
				}
				if(source.getX() == xMax) { //source is at right
					horizontalChannelsUsed--; //left out instead of right out
				}
				
				if(sink.getY() == yMax) { // sink is at top
					horizontalChannelsUsed ++; // bottom in instead of right
					verticalChannelsUsed --;
				}
			
			break;
				
		}
		
		return tIn + tOut + ( (horizontalChannelsUsed + verticalChannelsUsed - 1) * parameterManager.T_SWITCH );
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

	/*
	private int estimateDelayBetweenTwoChannels(){
		//T ODO Auto-generated method stub
		return 0;
	}*/

	/**
	 * computes the exact timings and outputs the critical path
	 */
	private void startAnalyzeTiming() {
		
		/**
		 * keep track of the net containing the critical path
		 */
		Net criticalNet= null; 
		int criticalPathLength= -1;
		
		for(Net n : nets) {
			int temp= n.annotateTA();
			if(temp > criticalPathLength){
				criticalPathLength= temp;
				criticalNet= n;
			}
		}
		
		//does not effect the output, but only annotates the data structures with tR and slack values, therefore can be disabled to improve performance
		for(Net n : nets) {
			n.annotateTRAndSlack(criticalPathLength);
		}
		
		printCriticalPath(criticalNet); //output the critical path
		
	}

	private void printCriticalPath(Net criticalNet) {
		
		StringBuilder output= new StringBuilder();
		
		output.append("exact critical path: \n \n");
		
		addTimingHeader(output);
		
		criticalNet.printCriticalPath(output);
		
		//TODO file output?
		System.out.println(output.toString());
		
	}

	private void addTimingHeader(StringBuilder output) {

		output.append("(time in ns) \n \n");
		output.append("Node:");
		output.append("\t");
		output.append("\t");
		output.append("\t");
		output.append("Time:");
		
		output.append("\n");
		
		output.append("Type:");
		output.append("\t");
		output.append("Name:");
		output.append("\t");
		output.append("Location:");
		output.append("\t");
		output.append("Delay:");
		output.append("\t");
		output.append("tA ('T-Arrival'):");
		
		output.append("\n");

		output.append("--");
		output.append("\t");
		output.append("--");
		output.append("\t");
		output.append("--");
		output.append("\t");
		output.append("--");
		output.append("\t");
		output.append("--");
		
		output.append("\n");
		
	}

}
