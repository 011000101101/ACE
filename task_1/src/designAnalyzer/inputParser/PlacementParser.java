package designAnalyzer.inputParser;

import java.io.FileNotFoundException;
import designAnalyzer.structures.StructureManager;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import designAnalyzer.errorReporter.ErrorReporter;

public class PlacementParser extends AbstractInputParser {

	
	public PlacementParser(String newFilePath, int width) throws FileNotFoundException {
		super(newFilePath);
		// TODO Auto-generated constructor stub
		
		currentLine= readLineAndTokenize();
	}
	
	/**
	 * parses two lines of the .p file and loads the first line starting with a block name
	 */
	protected void parseHeader() {
		
		//currentLine already loaded by the AbstractInputParser
		if(currentLine.length == 6) {
			
			if(!"Netlist".equals(currentLine[0])) {
				ErrorReporter.reportSyntaxError("Netlist", currentLine[0], this);	
			}
			
			if(!"file:".equals(currentLine[1])) {
				ErrorReporter.reportSyntaxError("file:", currentLine[1], this);
			}
			
			//checks whether the name of the netlist file is consistent
			String[] tmpArray = parameterManager.getNetlistPath().split("\\|/"); //string array with elements representing the path//TODO escape https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html#sum
			String tmpString = tmpArray[tmpArray.length-1];// string(name of netlist file) to be compared with
			if(!currentLine[2].equals(tmpString)) {
				ErrorReporter.reportInconsistentNamingError(tmpString, currentLine[2], this);
			}
			
			if(!"Architecture:".equals(currentLine[3])) {
				ErrorReporter.reportSyntaxError("Architecture", currentLine[3], this);
			}
			
			if(!"file:".equals(currentLine[4])) {
				ErrorReporter.reportSyntaxError("file:", currentLine[4], this);
			}
			
			if(!currentLine[5].contains(".arch")) {
				ErrorReporter.reportSyntaxError("*.arch:", currentLine[5], this);
			}
		} else {
			ErrorReporter.reportInvalidTokenCount(6, this);
		}
		
		currentLine = readLineAndTokenize();//load next 2nd line
		
		if(currentLine.length == 7) {
			
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
			if(!"logic".equals(currentLine[5])){
				ErrorReporter.reportSyntaxError("logic", currentLine[5], this);
			}
			if(!"block".equals(currentLine[6])){
				ErrorReporter.reportSyntaxError("block", currentLine[6], this);
			}
		} else {
			ErrorReporter.reportInvalidTokenCount(7, this);
		}
		
		currentLine = readLineAndTokenize(); //skip lines and load the next relevant line
		
	}
	
	/**
	 * this method gets called by AbstractInputParser repeatedly
	 * requires: first line already loaded
	 */
	protected void parseOneBlock() {
		
		if(currentLine.length == 5) {
			
			NetlistBlock tmp = structureManager.retrieveBlockByName(currentLine[0]); //netlistBlock with the name of the first word 
			
			if(tmp == null) {
				ErrorReporter.reportBlockNotFoundError(this);
			} 
			
			tmp.setCoordinates(Integer.valueOf(currentLine[1]), Integer.valueOf(currentLine[2])); 
			
			//set subblocknumber
			if(ONE_TOKEN.equals(currentLine[3])) {
				tmp.setSubblk_1(true);
			} else if(ZERO_TOKEN.equals(currentLine[3])){
				tmp.setSubblk_1(false);
			} else { //checks whether the argument is valid
				ErrorReporter.reportSyntaxMultipleChoiceError(new String[]{ONE_TOKEN, ZERO_TOKEN}, currentLine[3], this);
			}
			
			tmp.setBlockNumber(Integer.valueOf(currentLine[4].substring(1)));
			
			
		} else {
			ErrorReporter.reportInvalidTokenCount(5, this);
		}
		
		
	}

}
