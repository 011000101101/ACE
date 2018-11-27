package designAnalyzer.inputParser;

import java.io.FileNotFoundException;

import designAnalyzer.errorReporter.ErrorReporter;

public class ArchitectureParser extends AbstractInputParser {

	private int[] allParameters; //array with every parameter
	
	public ArchitectureParser(String newFilePath, int[] commandLineInput) throws FileNotFoundException {
		super(newFilePath);
		allParameters = commandLineInput;
		currentLine = readLineAndTokenize();
	}




	/**
	 * parses the whole arch file
	 * - in the locally stored array _ will be elements with either -1 for not included in command line or a value,
	 * that should be selected over data in the arch file
	 */
	@Override
	protected void parseHeader() {
		
		for(int i = 0; i < 9; i++) {
			if(currentLine == null) {
				ErrorReporter.reportArchFileNotComplete(i, this);
			}
			if(allParameters[i] == -1) {
				allParameters[i] = Integer.valueOf(currentLine[0]); 
			}
			currentLine = readLineAndTokenize();
		}
		//maybe check if currentline is null and throw error
	}

	

	@Override
	protected void parseOneBlock() {
		// TODO Auto-generated method stub
	}


	public int[] getAllParameters() {
		return allParameters;
	}

}
