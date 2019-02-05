package router.outputWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import designAnalyzer.ParameterManager;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.StructureManager;
import designAnalyzer.structures.pathElements.PathElement;
import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.LogicBlock;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import designAnalyzer.structures.pathElements.pins.OPin;
import router.structures.tree.NodeOfResource;

public class RoutingWriter{

	/**
	 * path to the file to be parsed
	 */
	private String filePath;
	
	/**
	 * file to be written
	 */
	private File outputFile;
	
	/**
	 * buffered writer for efficient file io
	 */
	private BufferedWriter outputFileWriter;
	
	/**
	 * object managing all datastructure instances <br>
	 * -handling insertion, retrieval and others
	 */
	private StructureManager structureManager;
	
	/**
	 * object managing all structural parameters 
	 */
	private ParameterManager parameterManager;

	
	public RoutingWriter() {
		parameterManager= ParameterManager.getInstance();
		structureManager= StructureManager.getInstance();
	}
	
	/**
	 * sets the new file path, creates a file and writes it
	 * @param newFilePath the path for the file to be written
	 */
	public void write(String routingFilePath, Collection<NodeOfResource> finalRouting) {
		
		filePath= routingFilePath;
		outputFile= new File(filePath);
		try {
			outputFileWriter= new BufferedWriter(new FileWriter(outputFile));
		} catch (IOException e) {
			System.err.println("Error occured trying to create a new file at path '" + filePath + "'");
			e.printStackTrace();
		}
		writeAll(finalRouting);
	}

	
	/**
	 * writes the complete file, block by block until end of file (indicated by null)
	 */
	private void writeAll(Collection<NodeOfResource> finalRouting) {
		
		try {
			writeHeader();
			writeAllBlocks(finalRouting);
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
	
	protected void writeHeader() throws IOException {

		outputFileWriter.write("Array size: " + parameterManager.X_GRID_SIZE + " x " + parameterManager.Y_GRID_SIZE + " logic blocks");
		outputFileWriter.write("\n");
		outputFileWriter.write("\n" + "Routing: " + "\n");
		
	}

	protected void writeAllBlocks(Collection<NodeOfResource> finalRouting) throws IOException {

		for(Net n : structureManager.getNetCollection()) {
			writeOneBlock(n, finalRouting);
		}
		
	}

	private void writeOneBlock(Net n, Collection<NodeOfResource> finalRouting) throws IOException {
		
		outputFileWriter.write("SOURCE (" + n.getSource().getX() + "," + n.getSource().getY() + ") "); //TODO fix red underlining 
		if(n.getSource() instanceof LogicBlock) {
			outputFileWriter.write("Class: 1");
		}
		else if(n.getSource() instanceof IOBlock){
			outputFileWriter.write("Pad: ");
			
			if(n.getSource().getSubblk_1()) {
				outputFileWriter.write("1");
			} else outputFileWriter.write("0");
		}
	}


	
}
