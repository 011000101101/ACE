package designAnalyzer.inputParser;

import java.io.FileNotFoundException;
import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.structures.blocks.IOBlock;
import designAnalyzer.structures.blocks.LogicBlock;
import designAnalyzer.structures.blocks.NetlistBlock;

public class NetlistParser extends AbstractInputParser {

	private static final String INPUT_TOKEN= ".input";
	private static final String OUTPUT_TOKEN= ".output";
	private static final String CLB_TOKEN= ".clb";
	private static final String GLOBAL_TOKEN= ".global";
	
	private int numberOfBlocks=0;
	
	public NetlistParser(String newFilePath) throws FileNotFoundException {
		super(newFilePath);
		// TODO Auto-generated constructor stub
		
		currentLine= readLineAndTokenize();
		
		
	}
	
	
	/**
	 * parses the complete netlist file, block by block until end of file (indicated by null)
	 * requires: the first line is already read
	 */
	public void parseNetlist() {
		
		
		while(currentLine != null) {	//check for end of file
			
			parseOneBlock();	
			currentLine= readLineAndTokenize();
			
		}
		
	}
	
	/**
	 * parses one block of lines in the netlist file <br>
	 * <br>
	 * (actually parsing the appropriate number of lines for the block to be parsed, ignoring empty lines)<br>
	 * <br>
	 * requires: the first line of the current block is already read (stored in currentLine)<br>
	 * post-state: currentLine contains the last line of the block that was just parsed
	 */
	private void parseOneBlock() {
		
		/**
		 * the block currently being parsed
		 */
		NetlistBlock currentBlock= null;
		
		boolean newBlock= false;
		
		switch(currentLine[0]) {
			
			case INPUT_TOKEN:
				
				currentBlock= parseInputBlock();
				newBlock= true;
					
				break;
				
			case OUTPUT_TOKEN:
				
				currentBlock= parseOutputBlock();
				newBlock= true;
				
				break;
				
			case CLB_TOKEN:
				
				currentBlock= parseLogicBlock();
				newBlock= true;
				
				break;
				
			case GLOBAL_TOKEN:
				
				parseGlobal();
				
				break;
				
			default:
				
				ErrorReporter.reportSyntaxMultipleChoiceError(new String[] {INPUT_TOKEN, OUTPUT_TOKEN, CLB_TOKEN, GLOBAL_TOKEN}, currentLine[0], this);
		
		}
		
		if(newBlock) {
			numberOfBlocks++;
			structureManager.insertBlock(currentBlock);
		}
		
	}
	
	/**
	 * parses a single InputBlock <br>
	 * requires: the current line is the beginning of an InputBlock
	 * @return the newly created InputBlock
	 */
	private NetlistBlock parseInputBlock() {
		
		NetlistBlock currentBlock= new IOBlock(currentLine[1], numberOfBlocks);
		
		currentLine= readLineAndTokenize();
		
		if(currentLine.length == 2) {	//check for correct number of arguments
		
			if(!"pinlist:".equals(currentLine[0])) { //check for correct token at start of line
				ErrorReporter.reportSyntaxError("pinlist", currentLine[0], this);
			}
			
			currentBlock.connect(structureManager.retrieveNet(currentLine[1], false), 1); //connect specified net to output pin of input block
			
		}
		else {
			ErrorReporter.reportInvalidTokenCount(2, this);
		}
		
		return currentBlock;
		
	}
	
	/**
	 * parses a single OutputBlock <br>
	 * requires: the current line is the beginning of an OutputBlock
	 * @return the newly created OutputBlock
	 */
	private NetlistBlock parseOutputBlock() {
		
		NetlistBlock currentBlock= new IOBlock(currentLine[1], numberOfBlocks);
		
		currentLine= readLineAndTokenize();
		
		if(currentLine.length == 2) {	//check for correct number of arguments
			
			if(!"pinlist:".equals(currentLine[0])) { //check for correct token at start of line
				ErrorReporter.reportSyntaxError("pinlist", currentLine[0], this);
			}
			
			currentBlock.connect(structureManager.retrieveNet(currentLine[1], false), 0); //connect specified net to input pin of output block
			
		}
		else {
			ErrorReporter.reportInvalidTokenCount(2, this);
		}
		
		return currentBlock;
		
	}
	
	/**
	 * parses a single OutputBlock <br>
	 * requires: the current line is the beginning of an OutputBlock
	 * @return the newly created OutputBlock
	 */
	private NetlistBlock parseLogicBlock() {
		
		/**
		 * name of the current block
		 */
		String name= currentLine[1];
		
		/**
		 * connectivity status for each pin, default value is false
		 */
		boolean[] pinsConnected= new boolean[6];
		
		NetlistBlock currentBlock= new LogicBlock(name, numberOfBlocks);
		
		
		//parse second line of entry
		currentLine= readLineAndTokenize();
		
		if(currentLine.length == 7) {	//check for correct number of arguments
			
			if(!"pinlist:".equals(currentLine[0])) { //check for correct token at start of line
				ErrorReporter.reportSyntaxError("pinlist", currentLine[0], this);
			}
			
			//connect input and output pins
			for(int i= 0; i < 5; i++) {		//connect pins
				
				if(!"open".equals(currentLine[i+1])) {	//ignore "open" pins
					pinsConnected[i]= true;
					currentBlock.connect(structureManager.retrieveNet(currentLine[i+1], false), i); //connect specified net to the appropriate input pin of output block
				}
				
			}
			
			//connect clock pin
			if(!"open".equals(currentLine[6])) {	//ignore "open" pins
				pinsConnected[5]= true;
				currentBlock.connect(structureManager.retrieveNet(currentLine[6], true), 5); //connect specified net to the appropriate input pin of output block
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
		
		return currentBlock;
		
	}
	
	/**
	 * insert clock net into net collection if it does not already exist, else do nothing
	 */
	private void parseGlobal() {
		
		structureManager.retrieveNet(currentLine[1], true);
		
	}

}
