package designAnalyzer.inputParser;

import java.io.FileNotFoundException;

import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.LogicBlock;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

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
			ErrorReporter.reportInconsistentArgumentError(parameterManager.getXGridSize(), currentLine[2], "xGridSize", this);
		}
		
		
		
	}

	@Override
	protected void parseOneBlock() {
		
		Net currentNet= parseBlockHeader();
		
		currentLine= readLineAndTokenize();
		
		parseSource(currentNet);
		
		currentLine= readLineAndTokenize();
		
		parseUntilSink(currentNet);
		
		currentLine= readLineAndTokenize();
		
		while(!NET_TOKEN.equals(currentLine[0])){
			
			parseUntilSink(currentNet);
			
			currentLine= readLineAndTokenize();
			
		}
		
	}

	private void parseSource(Net currentNet) {
		
		if(!NET_TOKEN.equals(currentLine[0])){
			ErrorReporter.reportSyntaxError(NET_TOKEN, currentLine[0], this);
		}
		
		final int xCoordinate= Integer.valueOf(currentLine[1].substring(1, currentLine[1].indexOf(",") - 1));
		final int yCoordinate= Integer.valueOf(currentLine[1].substring(currentLine[1].indexOf(",") - 1, currentLine[1].length() - 2));
		
		NetlistBlock currentBlock= structureManager.retrieveBlockByCoordinates(xCoordinate, yCoordinate);
		if(currentBlock == null){
			ErrorReporter.reportMissingBlockError(xCoordinate, yCoordinate, this);
		}
		
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
		
		currentNet.setSource(currentBlock);
		
		currentNet.setActiveBlock(currentBlock);
		
		
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

}
