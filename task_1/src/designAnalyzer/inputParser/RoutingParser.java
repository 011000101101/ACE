package designAnalyzer.inputParser;

import java.io.FileNotFoundException;

import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.pathElements.PathElement;
import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.LogicBlock;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import designAnalyzer.structures.pathElements.channels.AbstractChannel;

public class RoutingParser extends AbstractInputParser {

	private static final String NET_TOKEN = "Net";
	private static final String SOURCE_TOKEN = "SOURCE";
	private static final String OPIN_TOKEN = "OPIN";
	private static final String IPIN_TOKEN = "IPIN";
	private static final String CHANX_TOKEN = "CHANX";
	private static final String CHANY_TOKEN = "CHANY";
	private static final String SINK_TOKEN = "SINK";
	
	private static final String PAD_TOKEN = "Pad:";
	private static final String TRACK_TOKEN = "Track:";
	private static final String PIN_TOKEN = "Pin:";
	private static final String CLASS_TOKEN = "Class:";
	
	

	public RoutingParser(String newFilePath) throws FileNotFoundException {
		super(newFilePath);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void parseHeader() {
		
		if(!"Array".equals(currentLine[0])){
			ErrorReporter.reportSyntaxError("Array", currentLine[0], this);
		}
		if(!"size:".equals(currentLine[1])){
			ErrorReporter.reportSyntaxError("Array", currentLine[1], this);
		}
		if(!(Integer.valueOf(currentLine[2]) == parameterManager.getXGridSize())){
			ErrorReporter.reportInconsistentArgumentError(parameterManager.getXGridSize(), currentLine[2], "xGridSize", this);
		}
		if(!"x".equals(currentLine[3])){
			ErrorReporter.reportSyntaxError("x", currentLine[3], this);
		}
		if(!(Integer.valueOf(currentLine[4]) == parameterManager.getYGridSize())){
			ErrorReporter.reportInconsistentArgumentError(parameterManager.getYGridSize(), currentLine[2], "xGridSize", this);
		}
		
		
		
	}

	@Override
	protected void parseOneBlock() {
		
		Net currentNet= parseBlockHeader();
		
		currentLine= readLineAndTokenize();
		
		parseSource(currentNet);
		
		currentLine= readLineAndTokenize();
		
		parseUntilNextSink(currentNet);
		
		currentLine= readLineAndTokenize();
		
		while(!NET_TOKEN.equals(currentLine[0])){
			
			connectPathAndParseNextSink(currentNet);
			
			currentLine= readLineAndTokenize();
			
		}
		
	}

	/**
	 * connects the next partial path to a sink to the appropriate previous node and parses the path to the next sink
	 * <b>requires:<br> currentLine contains a duplicate occurrence of a PathElement that has already been parsed (connection point)
	 * @param currentNet the net currently being parsed
	 */
	private void connectPathAndParseNextSink(Net currentNet) {
		connectPath(currentNet);
		parseUntilNextSink(currentNet);
		
	}

	/**
	 * connects the next partial path to a sink to the appropriate previous node by setting currentNet.activeNode to the next PathElement, which has already been parsed at least once<br>
	 * <b>requires:</b> currentLine contains a PathElement that has already been parsed at least once in the current net
	 * @param currentNet the net currently being parsed
	 */
	private void connectPath(Net currentNet) {

		if(!currentNet.recoverCurrentPathElement(parseXCoordinate(), parseYCoordinate(), Integer.valueOf(currentLine[3]), CHANX_TOKEN.equals(currentLine[0]))) {
			ErrorReporter.reportInvalidConnectionPointRoutingError(currentNet, parseXCoordinate(), parseYCoordinate(), Integer.valueOf(currentLine[3]), CHANX_TOKEN.equals(currentLine[0]));
		}
		
		currentLine= readLineAndTokenize();
		
	}

	/**
	 * parses the path from the activeElement of the currentNet to the next sink in the PathElement input sequence<br>
	 * <b>requires:</b> activeElement of currentNet has just been parsed (and connected to appropriate node if the first source has already been parsed)
	 * @param currentNet the net currently being parsed
	 */
	private void parseUntilNextSink(Net currentNet) {
		
		while(!SINK_TOKEN.equals(currentLine[0])){
			
			parseSinglePathElement(currentNet);
			
			currentLine= readLineAndTokenize();
			
		}
		
		parseSink(currentNet);
		
	}

	/**
	 * requires: the loaded line has to start with CHANX or CHANY
	 * @param currentNet
	 */
	private void parseSinglePathElement(Net currentNet) {
		
		if(currentLine[0].equals(CHANX_TOKEN)) {
			parseChanX(currentNet);
		} else {
			parseChanY(currentNet);
		}
	}

	/**
	 * parses a single sink line
	 * @param currentNet the net which is being parsed
	 */
	private void parseSink(Net currentNet) {
		
		int xCoordinate= parseXCoordinate();
		int yCoordinate= parseYCoordinate();
		int pad= Integer.valueOf(currentLine[3]);
		
		
		
		currentLine= readLineAndTokenize();
		
		
		if(!SINK_TOKEN.equals(currentLine[0])){
			ErrorReporter.reportSyntaxError(SINK_TOKEN, currentLine[0], this);
		}
		
		NetlistBlock currentBlock= parseBlockCoordinates();
		
		parseClassOrPad(currentBlock);
		
		if(!currentNet.containsSink(currentBlock)){
			ErrorReporter.reportExcessSinkRoutingNetError(currentNet, currentBlock, this);
		}
		
		if(!currentNet.getActivePathElement().isNeighbour(currentBlock)){
			ErrorReporter.reportInvalidRoutingError(currentNet.getActivePathElement(), currentBlock, this);
		}
		
		checkSameCoordinates(currentBlock, xCoordinate, yCoordinate);
		checkSamePadOrPin(currentBlock, currentLine[3]);
		checkValidCoordinates(currentNet.getActivePathElement(), currentBlock);
		
		currentNet.getActivePathElement().addNext(currentBlock); //link graph node with previous node and vice versa
		currentBlock.addPrevious(currentNet.getActivePathElement());
		
		currentNet.setActivePathElement(currentBlock);
		
	}

	private void checkValidCoordinates(PathElement activePathElement, PathElement currentElement) {
		
		int activeX= activePathElement.getX();
		int activeY= activePathElement.getY();
		int currentX= currentElement.getX();
		int currentY= currentElement.getY();
		
		if(activePathElement instanceof AbstractChannel){
			if(currentElement instanceof AbstractChannel){
				int activeWire= ((AbstractChannel) activePathElement).getWire();
				int currentWire= ((AbstractChannel) currentElement).getWire();
			}
		}
		else{
			
		}
		
	}

	private void parseSource(Net currentNet) {
		
		if(!SOURCE_TOKEN.equals(currentLine[0])){
			ErrorReporter.reportSyntaxError(SOURCE_TOKEN, currentLine[0], this);
		}
		
		NetlistBlock currentBlock= parseBlockCoordinates();
		
		parseClassOrPad(currentBlock);
		
		if(!currentNet.getSource().equals(currentBlock)){
			ErrorReporter.reportInvalidSourceRoutingNetError(currentNet, currentBlock, this);
		}
		
		currentNet.setActivePathElement(currentBlock);
		
		
		
		currentLine= readLineAndTokenize();
		
		
		//parse OPIN
		checkSameCoordinates(currentBlock, parseXCoordinate(), parseYCoordinate());
		checkSamePadOrPin(currentBlock, currentLine[3]);
		
		if(currentBlock instanceof IOBlock){
			
			if(!PAD_TOKEN.equals(currentLine[2])){
				ErrorReporter.reportSyntaxError(PAD_TOKEN, currentLine[2], this);
			}
			
			if(!((IOBlock) currentBlock).getSubblk_1() == (ONE_TOKEN.equals(currentLine[3])) ){
				ErrorReporter.reportPinConnectionRoutingError(currentBlock, currentLine[0], currentLine[1], currentLine[3], this);
			}
			
		}
		else{
			
			if(!PIN_TOKEN.equals(currentLine[2])){
				ErrorReporter.reportSyntaxError(PIN_TOKEN, currentLine[2], this);
			}
			
			if(! ( Integer.valueOf(currentLine[3]) == 4) ){
				ErrorReporter.reportPinConnectionRoutingError(currentBlock, currentLine[0], currentLine[1], currentLine[3], this);
			}
			
		}
		
		
	}
	
	private void checkSamePadOrPin(NetlistBlock currentBlock, String padOrPinNumber) {
		
		if(currentBlock instanceof IOBlock){
			
			if(! (currentBlock.getSubblk_1() == (ONE_TOKEN.equals(padOrPinNumber))) ){
				ErrorReporter.reportPinConnectionRoutingError(currentBlock, currentLine[0], currentLine[1], currentLine[3], this);
			}
			
		}
		else{
			
			if(! (((LogicBlock) currentBlock).getBlockClass() == Integer.valueOf(currentLine[3])) ){
				ErrorReporter.reportPinConnectionRoutingError(currentBlock, currentLine[0], currentLine[1], currentLine[3], this);
			}
			
		}
		
	}

	private void parseClassOrPad(NetlistBlock currentBlock){
		
		if(currentBlock instanceof IOBlock){
			
			if(!PAD_TOKEN.equals(currentLine[2])){
				ErrorReporter.reportSyntaxError(PAD_TOKEN, currentLine[2], this);
			}
			
			if(ONE_TOKEN.equals(currentLine[3])){
				currentBlock.setSubblk_1(true);
			}
			else if(ZERO_TOKEN.equals(currentLine[3])){
				currentBlock.setSubblk_1(false);
			}
			else{
				ErrorReporter.reportSyntaxMultipleChoiceError(new String[] {ONE_TOKEN, ZERO_TOKEN}, currentLine[3], this);
			}
			
		}
		else{
			
			if(!CLASS_TOKEN.equals(currentLine[2])){
				ErrorReporter.reportSyntaxError(CLASS_TOKEN, currentLine[2], this);
			}
			
			((LogicBlock) currentBlock).setClass(Integer.valueOf(currentLine[3]));
			
		}
	
	}

	private Net parseBlockHeader() {

		if(!NET_TOKEN.equals(currentLine[0])){
			ErrorReporter.reportSyntaxError(NET_TOKEN, currentLine[0], this);
		}
		
		final int netNumber= Integer.valueOf(currentLine[1]);
		
		Net currentNet= structureManager.retrieveNetNoInsert(currentLine[2].substring(1, currentLine[2].length() - 2));
		
		if(currentNet == null){
			ErrorReporter.reportMissingNetError(currentLine[2].substring(1, currentLine[2].length() - 2), this);
			return null;
		}
		
		currentNet.setNetNumber(netNumber);
		
		return currentNet;
		
	}
	
	/**
	 * parses coordinates as the second token in the current line and returns the appropriate block
	 * @return the block located at the parsed coordinates, or null if no such block exists
	 */
	private NetlistBlock parseBlockCoordinates(){
		
		final int xCoordinate= parseXCoordinate();
		final int yCoordinate= parseYCoordinate();
		
		NetlistBlock currentBlock= structureManager.retrieveBlockByCoordinates(xCoordinate, yCoordinate);
		if(currentBlock == null){
			ErrorReporter.reportMissingBlockError(xCoordinate, yCoordinate, this);
		}
		
		return currentBlock;
		
	}

	/**
	 * parse x coordinate of format "(x,y)" in currentLine[1]
	 * @return x coordinate
	 */
	private int parseXCoordinate() {
		return Integer.valueOf(currentLine[1].substring(1, currentLine[1].indexOf(",") - 1));
	}

	/**
	 * parse x coordinate of format "(x,y)" in currentLine[1]
	 * @return y coordinate
	 */
	private int parseYCoordinate() {
		return Integer.valueOf(currentLine[1].substring(currentLine[1].indexOf(",") - 1, currentLine[1].length() - 2));
	}

	private void checkSameCoordinates(NetlistBlock lastBlock, int xCoordinate, int yCoordinate) {
		if(!(lastBlock.getX() == xCoordinate && lastBlock.getY() == yCoordinate)){
			ErrorReporter.reportPinConnectionRoutingError(lastBlock, currentLine[0], currentLine[1], currentLine[3], this);
		}
	}

}
