package designAnalyzer.inputParser;

import java.io.FileNotFoundException;
import java.util.HashMap;

import designAnalyzer.structures.*;
import errorReporter.ErrorReporter;

public class NetlistParser extends AbstractInputParser {

	private static final String INPUT_TOKEN= ".input";
	private static final String OUTPUT_TOKEN= ".output";
	private static final String CLB_TOKEN= ".clb";
	private static final String GLOBAL_TOKEN= ".global";
	
	private int numberOfBlocks=0;
	
	public NetlistParser(String newFilePath, HashMap<String, Net> newNetMap) throws FileNotFoundException {
		super(newFilePath, newNetMap);
		// TODO Auto-generated constructor stub
		
		currentLine= readLineAndTokenize();
	}
	
	
	public void parseNetlist() {
		
		
		while(currentLine != null) {
			
			parseOneBlock();
			
		}
		
	}
	
	private NetlistBlock parseOneBlock() {
		
		/**
		 * the block currently being parsed
		 */
		NetlistBlock currentBlock= null;
		
		switch(currentLine[0]) {
			
			case INPUT_TOKEN:
				currentBlock= new IOBlock(currentLine[1], numberOfBlocks);
				
				currentLine= readLineAndTokenize();
				
				if(currentLine.length == 2) {	//check for correct number of arguments
				
					if(!"pinlist:".equals(currentLine[0])) { //check for correct token at start of line
						ErrorReporter.reportSyntaxError("pinlist", currentLine[0], this);
					}
					
					currentBlock.connect(getNet(currentLine[1]), 1); //connect specified net to output pin of input block
					
				}
				else {
					ErrorReporter.reportInvalidTokenCount(2, this);
				}
					
				break;
				
			case OUTPUT_TOKEN:
				currentBlock= new IOBlock(currentLine[1], numberOfBlocks);
				
				currentLine= readLineAndTokenize();
				
				if(currentLine.length == 2) {	//check for correct number of arguments
					
					if(!"pinlist:".equals(currentLine[0])) { //check for correct token at start of line
						ErrorReporter.reportSyntaxError("pinlist", currentLine[0], this);
					}
					
					currentBlock.connect(getNet(currentLine[1]), 0); //connect specified net to input pin of output block
					
				}
				else {
					ErrorReporter.reportInvalidTokenCount(2, this);
				}
				
				break;
				
			case CLB_TOKEN:
				
				/**
				 * name of the current block
				 */
				String name= currentLine[1];
				
				/**
				 * connectivity status for each pin, default value is false
				 */
				boolean[] pinsConnected= new boolean[6];
				
				currentBlock= new LogicBlock(name, numberOfBlocks);
				
				
				//parse second line of entry
				currentLine= readLineAndTokenize();
				
				if(currentLine.length == 7) {	//check for correct number of arguments
					
					if(!"pinlist:".equals(currentLine[0])) { //check for correct token at start of line
						ErrorReporter.reportSyntaxError("pinlist", currentLine[0], this);
					}
					
					
					for(int i= 0; i < 6; i++) {		//connect pins
						
						if(!"open".equals(currentLine[i])) {	//ignore "open" pins
							pinsConnected[i]= true;
							currentBlock.connect(getNet(currentLine[i+1]), i); //connect specified net to the appropriate input pin of output block
						}
						
					}
					
				}
				else {
					ErrorReporter.reportInvalidTokenCount(7, this);
				}
				
				
				//parse third line of entry
				currentLine= readLineAndTokenize();
				
				if(currentLine.length == 8) {	//check for correct number of arguments
					
					if(!"subblock:".equals(currentLine[0])) { //check for correct token at start of line
						ErrorReporter.reportSyntaxError("pinlist", currentLine[0], this);
					}
					
					if(!name.equals(currentLine[1])) { //check for naming consistency inside block definition
						ErrorReporter.reportInconsistentNamingError(name, currentLine[1], this);
					}
					
					for(int i= 0; i < 6; i++) {		//check syntax of "subblock:" line
						
						if(pinsConnected[i]) {
							if(!String.valueOf(i).equals(currentLine[i+2])){
								ErrorReporter.reportSyntaxError(String.valueOf(i), currentLine[i+2], this);
							}
						}
						else {
							if(!"open".equals(currentLine[i+2])){
								ErrorReporter.reportSyntaxError("open", currentLine[i+2], this);
							}
						}
						
					}
					
				}
				else {
					ErrorReporter.reportInvalidTokenCount(8, this);
				}
				
				break;
				
			case GLOBAL_TOKEN:
				
				// TODO implement ".global" Net parsing
				
				break;
				
			default:
				
				ErrorReporter.reportSyntaxMultipleChoiceError(new String[] {INPUT_TOKEN, OUTPUT_TOKEN, CLB_TOKEN, GLOBAL_TOKEN}, currentLine[0], this);
		
		}
		
		numberOfBlocks++;
		return currentBlock;
		
	}

}
