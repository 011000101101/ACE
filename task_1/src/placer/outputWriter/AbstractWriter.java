package placer.outputWriter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import designAnalyzer.ParameterManager;
import designAnalyzer.structures.StructureManager;

/**
 * 
 * @author Dennis Grotz, Rumei Ma, Vincenz Mechler
 *
 */
public abstract class AbstractWriter {

	/**
	 * path to the file to be parsed
	 */
	protected String filePath;
	
	/**
	 * file to be written
	 */
	protected File outputFile;
	
	/**
	 * buffered writer for efficient file io
	 */
	protected BufferedWriter outputFileWriter;
	
	/**
	 * object managing all datastructure instances <br>
	 * -handling insertion, retrieval and others
	 */
	protected StructureManager structureManager;
	
	/**
	 * object managing all structural parameters 
	 */
	protected ParameterManager parameterManager;

	public AbstractWriter() {
		
		parameterManager= ParameterManager.getInstance();
		
	}
	
	/**
	 * sets the new file path, creates a file and writes it
	 * @param newFilePath the path for the file to be written
	 */
	public void write(String newFilePath) {
		
		filePath= newFilePath;
		outputFile= new File(filePath);
		try {
			outputFileWriter= new BufferedWriter(new FileWriter(outputFile));
		} catch (IOException e) {
			System.err.println("Error occured trying to create a new file at path '" + filePath + "'");
			e.printStackTrace();
		}
		writeAll();
		
	}
	
	/**
	 * writes the complete file, block by block until end of file (indicated by null)
	 */
	private void writeAll() {
		
		try {
			writeHeader();
			writeAllBlocks();
		} catch (IOException e1) {
			System.err.println("Error occured trying to write to buffered writer");
			e1.printStackTrace();
		}
		
		
		try {
			outputFileWriter.close();
		} catch (IOException e) {
			System.err.println("Error occured trying to create a new file: unable to close bufferedWriter");
			e.printStackTrace();
		}
		
	}

	/**
	 * writes header of the file
	 * @throws IOException 
	 */
	protected abstract void writeHeader() throws IOException;
	
	/**
	 * writes all blocks of data into the output file
	 */
	protected abstract void writeAllBlocks() throws IOException;
	
	
}
