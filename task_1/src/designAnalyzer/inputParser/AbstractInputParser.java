package designAnalyzer.inputParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import designAnalyzer.structures.*;

public abstract class AbstractInputParser {

	/**
	 * path to the file to be parsed
	 */
	protected String filePath;
	
	/**
	 * file to be parsed
	 */
	protected File inputFile;
	
	/**
	 * buffered reader for efficient file access
	 */
	protected BufferedReader inputFileReader;
	
	/**
	 * current line number in input file for error output
	 */
	protected int currentLineNumber;
	
	/**
	 * variable holding the line currently being parsed
	 */
	protected String[] currentLine;
	
	/**
	 * object managing all datastructure instances <br>
	 * -handling insertion, retrieval and others
	 */
	protected StructureManager structureManager;
	
	
	
	
	
	public AbstractInputParser(String newFilePath) throws FileNotFoundException {
		
		filePath= newFilePath;
		inputFile= new File(filePath);
		inputFileReader= new BufferedReader(new FileReader(inputFile));
		currentLineNumber= 0;
		structureManager= StructureManager.getInstance();
		
	}
	
	/**
	 * reads a whole line and splits it into tokens (at any number of spaces)
	 * @return an array filled with the tokens (single "words") of the line read
	 */
	protected String[] readLineAndTokenize() {
		
		/**
		 * temporary variable for checking for end of file
		 */
		String currentLine= readLine(); // read the line
		
		if(currentLine == null) {	//end of file
				
			return null;
		
		}
			
		while(currentLine.charAt(0) == '#' || currentLine.length() == 0) {	//ignore comment lines starting with '#' and empty lines
			currentLine= readLine();
		}

		return currentLine.split("\\s+");	//return tokenized line
		
	}
	
	/**
	 * read a line from the input file and update currentLineNumber
	 * @return the line read, unaltered in the format BufferedReader.readLine() returns it, or an empty line if an IOException occurs
	 * @see java.io.BufferedReader#readLine()
	 */
	private String readLine() {
		
		try {
			
			currentLineNumber+= 1;
			return inputFileReader.readLine();
			
		} catch (IOException e) {
			System.out.println("WARNING: IOException occured while trying to read a line from the input file, trying to resume execution normally...");
			e.printStackTrace();
		}
		return ""; //return empty line if IOException occurred
	}
	

	
	
	/**
	 * parses the complete netlist file, block by block until end of file (indicated by null)
	 * requires: the first line is already read
	 */
	public void parseAll() {
		
		parseHeader();
		
		while(currentLine != null) {	//check for end of file
			
			parseOneBlock();	
			currentLine= readLineAndTokenize();
			
		}
		
	}
	
	protected abstract void parseHeader();
	
	
	/**
	 * parses one block of lines in the input file <br>
	 * <br>
	 * (actually parsing the appropriate number of lines for the block to be parsed, ignoring empty and comment lines)<br>
	 * <br>
	 * requires: the first line of the current block is already read (stored in currentLine)<br>
	 * post-state: currentLine contains the last line of the block that was just parsed
	 */
	protected abstract void parseOneBlock();
	
	
	/**
	 * standard getter
	 * @return current line number of the BufferedReader in the input file
	 */
	public int getLineNumber() {
		return currentLineNumber;
	}
	
	
}
