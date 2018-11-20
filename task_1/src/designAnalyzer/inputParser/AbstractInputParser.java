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
	
	
	
	public AbstractInputParser(String newFilePath) throws FileNotFoundException {
		
		filePath= newFilePath;
		inputFile= new File(filePath);
		inputFileReader= new BufferedReader(new FileReader(inputFile));
		
	}
	
	/**
	 * reads a whole line and splits it into tokens (at any number of spaces)
	 * @return an array filled with the tokens (single "words") of the line read
	 */
	protected String[] readLineAndTokenize() {
		
		try {
			
			return inputFileReader.readLine().split("\\s+");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] temp= new String[1];
		temp[0]= "\n";
		
		return temp;
		
	}
	
	
}
