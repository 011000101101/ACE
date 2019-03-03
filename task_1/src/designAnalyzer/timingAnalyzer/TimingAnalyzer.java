package designAnalyzer.timingAnalyzer;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedList;
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
	
	/**
	 * object managing all datastructure instances <br>
	 * -handling insertion, retrieval and others
	 */
	private StructureManager structureManager;
	
	/**
	 * object managing all structural parameters 
	 */
	private ParameterManager parameterManager;
	
	/**
	 * collection containing all nets
	 */
	private Collection<Net> nets;
	

	private int xMax;
	private int yMax;

	/**
	 * look up table for delay values indexed by block type, deltaX and deltaY
	 */
	private int[][][] delayLUT;

	private int[][] delayLUT_LL;

	private int[][] delayLUT_LIO;

	private int[][] delayLUT_IOL;

	private int[][] delayLUT_IOIO;
	
	private static TimingAnalyzer singleton;
	
	
	private TimingAnalyzer(){
		
		structureManager= StructureManager.getInstance();
		parameterManager= ParameterManager.getInstance();

		xMax= parameterManager.X_GRID_SIZE + 1;
		yMax= parameterManager.Y_GRID_SIZE + 1;
		
	}
	
	public static TimingAnalyzer getInstance() {
		if(singleton == null) {
			singleton= new TimingAnalyzer();
		}
		return singleton;
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
			if(!n.getIsClocknNet()) { // ignore clock nets
				int temp= estimateSingleNet(n);
				if(temp > criticalPathLength){
					criticalPathLength= temp;
					criticalNet= n;
				}
			}
		}
		
		printEstimatedCriticalPath(criticalNet, criticalPathLength);
		
	}

	
	/**
	 * prints the estimated critical path by printing source and sink
	 * @param criticalNet
	 * @param criticalPathLength
	 */
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
		
		
		printToFile(output, true);
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
		Collection<NetlistBlock> sinks= currentNet.getSinks();
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
		
		
		
		/**
		 * delay from channel into sink
		 */
		int tIn= parameterManager.T_FFIN ;
		
		/**
		 * delay from source to channel
		 */
		int tOut= parameterManager.T_FFOUT ;
		
		if(source instanceof IOBlock){
			tOut= parameterManager.T_IPAD ;
		}

		if(sink instanceof IOBlock){
			tIn= parameterManager.T_OPAD ;
		}
		
		
		
		return tIn + tOut + estimateSinglePathNoEndpoints(source.getX(), source.getY(), sink.getX(), sink.getY());
	}
	
	public int estimateSinglePathNoEndpoints(int xSource, int ySource, int xSink, int ySink) {
		
		int horizontalChannelsUsed=-1;
		int verticalChannelsUsed=-1;
		
		int direction= computeDirection(xSource, ySource, xSink, ySink);
		
		switch (direction){
		
			case 0: // sink directly above source
				
				horizontalChannelsUsed= 0; //use right output and input pin of source and sink
				verticalChannelsUsed= ySink-ySource + 1; //from right side to right side (or from left to left if both are on right border)
				
				if(ySource == 0){ //[...] is iopad at bottom, use top out pin and channel instead of right, rout it to vertical channel at top right
					horizontalChannelsUsed++;
					verticalChannelsUsed--;
					if(ySink == 1) { //sink directly above source
						verticalChannelsUsed= 0;
					}
				}
				if(ySink == yMax){ //[...] is iopad at top, use bottom out pin and channel instead of right, rout it to vertical channel at bottom right
					horizontalChannelsUsed++;
					verticalChannelsUsed--;
				}
				
			break;
			
			case 4: // sink directly below source
				
				
				horizontalChannelsUsed= 2; //use bottom output and top input pin of source and sink
				verticalChannelsUsed= ySource - ySink -1; 

				if(xSource == 0){ //[...] is iopad at left border
					horizontalChannelsUsed= 0;
					verticalChannelsUsed+= 2;
				}
				else if(xSource == xMax){ //[...] is iopad at right border
					horizontalChannelsUsed= 0;
					verticalChannelsUsed+= 2;
				}
				// not left or right IOBlocks
				else if(ySink + 1 == ySource){
					horizontalChannelsUsed= 1;
					verticalChannelsUsed=0;
				}
				
			break;
			
			case 2: // sink directly right of source
				
				horizontalChannelsUsed= xSink - xSource - 1;
				verticalChannelsUsed= 2; 

				if(ySource == 0){ //[...] is iopad at bottom border
					verticalChannelsUsed = 0;
					horizontalChannelsUsed+= 2;
				}
				else if(ySource == yMax){ //[...] is iopad at top border
					verticalChannelsUsed= 0;
					horizontalChannelsUsed += 2;
				}
				// not top or bottom IOBlocks
				else if(xSink - 1 == xSource){ //if directly next to each other
					horizontalChannelsUsed= 0;
					verticalChannelsUsed=1;
				}
				
			break;
			
			case 6: // sink directly left of source
				
				 horizontalChannelsUsed= xSource - xSink; //bottom out
				 verticalChannelsUsed= 1; //right in

				if(ySource == 0){ //[...] is iopad at bottom
					verticalChannelsUsed = 0;
					horizontalChannelsUsed+= 1;
				}
				else if(ySource == yMax){ //[...] is iopad at top
					verticalChannelsUsed = 0;
					horizontalChannelsUsed+= 1;
				}
				else if(xSource == xMax){
					horizontalChannelsUsed--; //left out instead of bottom
					if(!(xSource - 1 == xSink)){
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
				
				horizontalChannelsUsed= xSink - xSource; //right out
				verticalChannelsUsed= ySink - ySource; //bottom in

				if(ySource == 0){ //[...] is iopad at bottom
					horizontalChannelsUsed++; //top out
					verticalChannelsUsed--;
				}
				if(xSink == xMax){ //[...] is iopad at right border
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
				
				horizontalChannelsUsed = xSink - xSource; //right out
				verticalChannelsUsed = ySource - ySink; //top in
				
				if(ySource == yMax) { // source is iopad at top
					horizontalChannelsUsed++; // bottom out
					verticalChannelsUsed--;
				}
				
				if(xSink == xMax) { // sink is iopad at right border
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
				
				horizontalChannelsUsed = xSource - xSink; //bottom out
				verticalChannelsUsed = ySource - ySink; //right in
				
				if(xSource == xMax) { // source is iopad at right border
					verticalChannelsUsed ++; //left out
					horizontalChannelsUsed --;
				}
				
				if(ySink == 0) { // sink is iopad at bottom
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
				
				horizontalChannelsUsed = xSource - xSink; // bottom out
				verticalChannelsUsed = ySink - ySource + 1; // right in
				
				if(ySource == 0) { //source is at bottom
					verticalChannelsUsed --; //top out
				}
				if(xSource == xMax) { //source is at right
					horizontalChannelsUsed--; //left out instead of right out
				}
				
				if(ySink == yMax) { // sink is at top
					horizontalChannelsUsed ++; // bottom in instead of right
					verticalChannelsUsed --;
				}
			
			break;
				
		}
		
		return 2 * parameterManager.T_SWITCH +  ( (horizontalChannelsUsed + verticalChannelsUsed - 1) * parameterManager.T_SWITCH );
		
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
			if(ySource < ySink) return 7; //sink above source
			else return 5; //sink below source
		}
	}


	/**
	 * computes the exact timings and outputs the critical path
	 */
	private void startAnalyzeTiming() {
		
		/**
		 * keep track of the net containing the critical path
		 */
		Net criticalNet= null; 
		int criticalPathLength= -1;
		
		List<int[]> exactWireLengths= new LinkedList<int[]>();
		
		for(Net n : nets) {
			if(!n.getIsClocknNet()) { // ignore clock nets
				int temp= n.annotateTA(exactWireLengths);
				if(temp > criticalPathLength){
					criticalPathLength= temp;
					criticalNet= n;
				}
			}
		}
		
		int[] exactWiringLengthDummy= new int[8]; //create dummy (Ta already computed and will only be retrieved, no need to store anything as all segments have already been counted
		// 
		for(Net n : nets) {
			if(!n.getIsClocknNet()) { // ignore clock nets
				n.annotateTRAndSlack(criticalPathLength, exactWiringLengthDummy);
			}
		}
		
		printCriticalPath(criticalNet); //output the critical path
		
		printExactWireLenghts(exactWireLengths);
		
	}

	/**
	 * print the exact wire lengths in segments for each net and the overall sum
	 * @param exactWireLengths
	 */
	private void printExactWireLenghts(List<int[]> exactWireLengths) {
		int[] exactWieLengthSums= new int[7];
		StringBuilder output= new StringBuilder();
		output.append("\n\n");
		output.append("exact wire length per path (in segments): \n \n");
		output.append("net#:\tpath#:\tCHAN:\tIPAD:\tOPAD:\tSWITCH:\tCOMB:\tFFIN:\tFFOUT:\n");
		output.append("---\t---\t---\t---\t---\t---\t---\t---\t---\n");
		for(int[] wl : exactWireLengths) {
			output.append("[");
			output.append(wl[7]);
			output.append("]");
			output.append("\t");
			output.append("-");
			output.append(wl[8]);
			output.append("-");
			output.append("\t");
			output.append(wl[0]);
			output.append("\t");
			output.append(wl[1]);
			output.append("\t");
			output.append(wl[2]);
			output.append("\t");
			output.append(wl[3]);
			output.append("\t");
			output.append(wl[4]);
			output.append("\t");
			output.append(wl[5]);
			output.append("\t");
			output.append(wl[6]);
			output.append("\n");
			
			for(int j= 0; j < exactWieLengthSums.length; j++) {
				exactWieLengthSums[j]+= wl[j];
			}
		}
		output.append("---\t---\t---\t---\t---\t---\t---\t---\t---\n");
		
		output.append("[all]");
		output.append("\t");
		output.append("\t");
		output.append(exactWieLengthSums[0]);
		output.append("\t");
		output.append(exactWieLengthSums[1]);
		output.append("\t");
		output.append(exactWieLengthSums[2]);
		output.append("\t");
		output.append(exactWieLengthSums[3]);
		output.append("\t");
		output.append(exactWieLengthSums[4]);
		output.append("\t");
		output.append(exactWieLengthSums[5]);
		output.append("\t");
		output.append(exactWieLengthSums[6]);
		output.append("\n");
		
		
		printToFile(output, false);
		System.out.println(output.toString());
		
	}

	/**
	 * print the critical path by making the pathelements recursively print themselves.
	 * @param criticalNet
	 */
	private void printCriticalPath(Net criticalNet) {
		
		StringBuilder output= new StringBuilder();
		
		output.append("exact critical path: \n \n");
		
		addTimingHeader(output);
		
		criticalNet.printCriticalPath(output);
		
		printToFile(output, true);
		System.out.println(output.toString());
		
	}
	
	/**
	 * adds timing header to the stringbuilder
	 * @param output
	 */
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

	/**
	 * saves output to file
	 * @param output
	 * @param criticalPath
	 */
	private void printToFile(StringBuilder output, boolean criticalPath) {
		String filename;
		if(criticalPath) {
			filename= "critical_path";
		}
		else {
			filename= "segment_count";
		}
		try {
			PrintWriter writer = new PrintWriter(filename + ".txt", "UTF-8");
			writer.append(output);
			writer.close();
		}
		catch (Exception e){
			System.err.println("could not print critical path to file.");
		}
	}

	/**
	 * looks up the delay of a path in the delay LUTs based on the distance between source and sink, computes and stores it if it has not previously been computed
	 * @param source source block of the path
	 * @param sink sink block of the path
	 * @return the delay of the path, without the input and output delays
	 */
	public int lookUpSinglePathNoEndpoints(NetlistBlock source, NetlistBlock sink, int xSource, int ySource, int xSink, int ySink) {
		int x_max= parameterManager.X_GRID_SIZE;
		int y_max= parameterManager.Y_GRID_SIZE;
		int deltaX= xSink - xSource;
		int deltaY= ySink - ySource;
		int delay;
		if(source instanceof IOBlock) {
			if(sink instanceof IOBlock) {
				delay= delayLUT_IOIO[deltaX + xMax][deltaY + yMax];
				if(delay < 0) {
					delay= estimateSinglePathNoEndpoints(xSource, ySource, xSink, ySink);
					delayLUT_IOIO[deltaX + xMax][deltaY + yMax]= delay;
				}
				return delay;
			}
			else {
				delay= delayLUT_IOL[deltaX + x_max][deltaY + y_max];
				if(delay < 0) {
					delay= estimateSinglePathNoEndpoints(xSource, ySource, xSink, ySink);
					delayLUT_IOL[deltaX + x_max][deltaY + y_max]= delay;
				}
				return delay;
			}
		}
		else {
			if(sink instanceof IOBlock) {
				delay= delayLUT_LIO[deltaX + x_max][deltaY + y_max];
				if(delay < 0) {
					delay= estimateSinglePathNoEndpoints(xSource, ySource, xSink, ySink);
					delayLUT_LIO[deltaX + x_max][deltaY + y_max]= delay;
				}
				return delay;
			}
			else {
				delay= delayLUT_LL[deltaX + x_max - 1][deltaY + y_max - 1];
				if(delay < 0) {
					delay= estimateSinglePathNoEndpoints(xSource, ySource, xSink, ySink);
					delayLUT_LL[deltaX + x_max - 1][deltaY + y_max - 1]= delay;
				}
				return delay;
			}
		}
	}
	
	/**
	 * initializes the four delay LUTs with -1 as every entry
	 */
	public void initializeDelayLUT() {
		int x_max= parameterManager.X_GRID_SIZE;
		int y_max= parameterManager.Y_GRID_SIZE;
		delayLUT_LL= new int[2*(x_max - 1) + 1][2*(y_max - 1) + 1];
		for(int i= -x_max + 1; i < x_max; i++) {
			for(int j= -y_max + 1; j < y_max; j++) {
				delayLUT_LL[i + x_max - 1][j + y_max - 1]= -1;
				/*
				if(i<0) {
					if(j<0) {
						delayLUT_LL[i + x_max - 1][j + y_max - 1]= estimateSinglePathNoEndpoints(x_max, y_max, x_max + i, y_max + j);
					}
					else {
						delayLUT_LL[i + x_max - 1][j + y_max - 1]= estimateSinglePathNoEndpoints(x_max, 1, x_max + i, 1 + j);
					}
				}
				else {
					if(j<0) {
						delayLUT_LL[i + x_max - 1][j + y_max - 1]= estimateSinglePathNoEndpoints(1, y_max, 1 + i, y_max + j);
					}
					else {
						delayLUT_LL[i + x_max - 1][j + y_max - 1]= estimateSinglePathNoEndpoints(1, 1, 1 + i, 1 + j);
					}
				}
				*/
			}
		}
		delayLUT_LIO= new int[2*(x_max) + 1][2*(y_max) + 1];
		for(int i= -x_max; i <= x_max; i++) {
			for(int j= -y_max; j <= y_max; j++) {
				delayLUT_LIO[i + x_max][j + y_max]= -1;
				/*
				if(i<0) {
					if(j<0) {
						delayLUT_LIO[i + x_max][j + y_max]= estimateSinglePathNoEndpoints(-i, -j, 0, 0);
					}
					else {
						delayLUT_LIO[i + x_max][j + y_max]= estimateSinglePathNoEndpoints(-i, yMax - j, 0, yMax);
					}
				}
				else {
					if(j<0) {
						delayLUT_LIO[i + x_max][j + y_max]= estimateSinglePathNoEndpoints(xMax - i, -j, xMax, 0);
					}
					else {
						delayLUT_LIO[i + x_max][j + y_max]= estimateSinglePathNoEndpoints(xMax - i, yMax - j, xMax, yMax);
					}
				}
				*/
			}
		}
		delayLUT_IOL= new int[2*(x_max) + 1][2*(y_max) + 1];
		for(int i= -x_max; i <= x_max; i++) {
			for(int j= -y_max; j <= y_max; j++) {
				delayLUT_IOL[i + x_max][j + y_max]= -1;
			}
		}
		delayLUT_IOIO= new int[2*(xMax) + 1][2*(yMax) + 1];
		for(int i= -xMax; i <= xMax; i++) {
			for(int j= -yMax; j <= yMax; j++) {
				delayLUT_IOIO[i + xMax][j + yMax]= -1;
			}
		}
	}

}
