package designAnalyzer.inputParser;

import java.io.FileNotFoundException;
import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.StructureManager;
import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.LogicBlock;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

/**
 * 
 * @author Dennis Grotz, Rumei Ma, Vincenz Mechler
 *
 */
public class NetlistParser extends AbstractInputParser {

	private static final String INPUT_TOKEN= ".input";
	private static final String OUTPUT_TOKEN= ".output";
	private static final String CLB_TOKEN= ".clb";
	private static final String GLOBAL_TOKEN= ".global";
	
	/**
	 * number of blocks already parsed
	 */
	private int numberOfBlocks=0;
	
	private String globalNet = "";
	
	public NetlistParser(String newFilePath) throws FileNotFoundException {
		super(newFilePath);
		// TODO Auto-generated constructor stub
		structureManager= StructureManager.getInstance();
		
		currentLine= readLineAndTokenize();
		
		
	}
	
	/**
	 * parses two lines of net file
	 */
	protected void parseOneBlock() {
		
		/**
		 * the block currently being parsed
		 */
		NetlistBlock currentBlock= null;
		
		boolean newBlock= false;
		
		switch(currentLine[0]) {
			
			case INPUT_TOKEN:
				
				currentBlock= parseInputBlock();
				if(currentBlock == null) newBlock= false;
				else newBlock= true;
				break;
				
			case OUTPUT_TOKEN:
				
				currentBlock= parseOutputBlock();
				if(currentBlock == null) newBlock= false;
				else newBlock= true;
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
		
		currentLine= readLineAndTokenize();
		
	}
	
	/**
	 * parses a single InputBlock <br>
	 * requires: the current line is the beginning of an InputBlock
	 * @return the newly created InputBlock
	 */
	private NetlistBlock parseInputBlock() {
		
		//parse clock input block, handle seperately in placer
		//if(!currentLine[1].equals(globalNet)) { 
			NetlistBlock currentBlock= new IOBlock(currentLine[1], numberOfBlocks);
			
			currentLine= readLineAndTokenize();
			
			if(currentLine.length == 2) {	//check for correct number of arguments
			
				if(!"pinlist:".equals(currentLine[0])) { //check for correct token at start of line
					ErrorReporter.reportSyntaxError("pinlist", currentLine[0], this);
				}
				
				Net currentNet= structureManager.retrieveNet(currentLine[1], false);
				currentBlock.connect(currentNet, 1); //connect specified net to output pin of input block
				if(!currentNet.getIsClocknNet()) { // ignore clock nets
					currentNet.setSource(currentBlock); //link block to net as only source
				}
				
			}
			else {
				ErrorReporter.reportInvalidTokenCount(2, this);
			}
			
			return currentBlock;
			
		//}

		//currentLine= readLineAndTokenize();
		//return null;
	}
	
	/**
	 * parses a single OutputBlock <br>
	 * requires: the current line is the beginning of an OutputBlock
	 * @return the newly created OutputBlock
	 */
	private NetlistBlock parseOutputBlock() {

		if(!currentLine[1].equals(globalNet)) {
			NetlistBlock currentBlock= new IOBlock(currentLine[1], numberOfBlocks);
			
			currentLine= readLineAndTokenize();
			
			if(currentLine.length == 2) {	//check for correct number of arguments
				
				if(!"pinlist:".equals(currentLine[0])) { //check for correct token at start of line
					ErrorReporter.reportSyntaxError("pinlist", currentLine[0], this);
				}
				
				Net currentNet= structureManager.retrieveNet(currentLine[1], false);
				currentBlock.connect(currentNet, 0); //connect specified net to input pin of output block
				currentNet.addSink(currentBlock);	//link block to net as sink
				
			}
			else {
				ErrorReporter.reportInvalidTokenCount(2, this);
			}
			
			return currentBlock;
		}

		currentLine= readLineAndTokenize();
		return null;
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
			
			if(!"pinlist:".equals(currentLine[0])) {	//check for correct token at start of line
				ErrorReporter.reportSyntaxError("pinlist", currentLine[0], this);
			}
			
			//connect input pins
			for(int i= 0; i < 4; i++) {	
				
				if(!"open".equals(currentLine[i+1])) {	//ignore "open" pins
					pinsConnected[i]= true;
					Net currentNet= structureManager.retrieveNet(currentLine[i+1], false);
					currentBlock.connect(currentNet, i);	//connect specified net to the appropriate input pin of logic block
					currentNet.addSink(currentBlock);	//link block to net as sink
				}
				
			}
			
			//connect output pin
			if(!"open".equals(currentLine[5])) {	//ignore "open" pins
				pinsConnected[4]= true;
				Net currentNet= structureManager.retrieveNet(currentLine[5], false);
				currentBlock.connect(currentNet, 4);	//connect specified net to the output pin of logic block
				currentNet.setSource(currentBlock);	//link block to net as only source
			}
			
			//connect clock pin
			if(!"open".equals(currentLine[6])) {	//ignore "open" pins
				pinsConnected[5]= true;
				Net currentNet= structureManager.retrieveNet(currentLine[6], false);
				currentNet.setIsClockNet(true);
				currentBlock.connect(currentNet, 5);	//connect specified net to the clock pin of logic block
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
		
		Net temp= structureManager.retrieveNet(currentLine[1], true);
		temp.setIsClockNet(true);
		globalNet = temp.getName();
		
	}

	@Override
	protected void parseHeader() {
		// empty method, no header
		
	}

}
