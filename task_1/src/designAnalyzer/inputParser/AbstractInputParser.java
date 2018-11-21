package designAnalyzer.inputParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import designAnalyzer.structures.*;
import errorReporter.ErrorReporter;

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
	 * HashMap containing all Nets, with name as key
	 */
	private HashMap<String, Net> netMap;
	
	private int numberOfNets;
	
	
	
	public AbstractInputParser(String newFilePath, HashMap<String, Net> newNetMap) throws FileNotFoundException {
		
		filePath= newFilePath;
		inputFile= new File(filePath);
		inputFileReader= new BufferedReader(new FileReader(inputFile));
		currentLineNumber= 0;
		
		netMap= newNetMap;
		numberOfNets= 0;
		
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
			
		while(currentLine.charAt(0) == '#') {	//ignore comment lines starting with '#'
			currentLine= readLine();
		}
		
		if(currentLine == null) {	//end of file
				
			return null;
		
		}
		else {
				
			return currentLine.split("\\s+");	//return tokenized line
	
		}
		
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ""; //return empty line if IOException occurred
	}
	
	public int getLineNumber() {
		return currentLineNumber;
	}
	
	/**
	 * get Net by name reference if it already exists, else create new Net, insert into netMap and return it
	 * @param name name of the Net to get the instance of
	 * @return instance of Net belonging to the given name
	 */
	protected Net getNet(String name) {
		if(netMap.containsKey(name)) {
			return netMap.get(name);
		}
		else {
			numberOfNets++;
			Net newNet= new Net(name, numberOfNets);
			netMap.put(name,  newNet);
			return newNet;
		}
	}
	
	
}
