package router.outputWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import designAnalyzer.ParameterManager;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.StructureManager;
import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.LogicBlock;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import router.structures.resourceWithCost.ChannelWithCost;
import router.structures.resourceWithCost.SinkWithCost;
import router.structures.resourceWithCost.SourceDummy;
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
	 * object managing all structural parameters 
	 */
	private ParameterManager parameterManager;

	/**
	 * the last line of routingFile, which will be duplicated for branching, saved as a string
	 */
	private String duplicateLineCache = null;
	
	public RoutingWriter() {
		parameterManager= ParameterManager.getInstance();
		StructureManager.getInstance();
	}
	
	/**
	 * sets the new file path, creates a file and writes it
	 * @param newFilePath the path for the file to be written
	 */
	public void write(String routingFilePath, Map<Net, NodeOfResource> finalRouting) {
		
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
	private void writeAll(Map<Net, NodeOfResource> finalRouting) {
		
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
	
	/**
	 * writes routing file til "Net:"
	 * @throws IOException
	 */
	protected void writeHeader() throws IOException {

		outputFileWriter.write("Array size: " + parameterManager.X_GRID_SIZE + " x " + parameterManager.Y_GRID_SIZE + " logic blocks\n\n");
		outputFileWriter.write("Routing: \n\n");
		
	}

	/**
	 * writes every net 
	 * @param finalRouting
	 * @throws IOException
	 */
	protected void writeAllBlocks(Map<Net, NodeOfResource> finalRouting) throws IOException {
		
		int netNumber = 0;
		for(Net n : finalRouting.keySet()) {
			outputFileWriter.write("\nNet " + netNumber + " (" + n.getName() + ")\n\n");
			writeOneBlock(n, finalRouting.get(n));
			netNumber++;
		}
		
	}

	/**
	 * writes only one net
	 * @param n
	 * @param nodeOfResource
	 * @throws IOException
	 */
	private void writeOneBlock(Net n, NodeOfResource nodeOfResource) throws IOException {
		
		nodeOfResource= nodeOfResource.getChild();
		
		/*List<Object[]>*/ List<NodeOfResource> branchingPoint = new LinkedList<NodeOfResource>(); //new LinkedList<Object[]>(); //list, which contains arrays of String and NodeOfResource
		
		outputFileWriter.write("SOURCE (" + n.getSource().getX() + "," + n.getSource().getY() + ") "); 
		if(n.getSource() instanceof LogicBlock) {
			outputFileWriter.write("Class: 1" + "\n");
		}
		else if(n.getSource() instanceof IOBlock){
			outputFileWriter.write("Pad: ");
			
			if(n.getSource().getSubblk_1()) {
				outputFileWriter.write("1" + "\n");
			} else {
				outputFileWriter.write("0" + "\n");
			}
		}
		
		while(nodeOfResource != null) {
			
			while(nodeOfResource != null) {
				
				writeLines(branchingPoint, nodeOfResource);
				
				nodeOfResource= nodeOfResource.getChild();
			}
			
			if(branchingPoint.size() > 0) {
				
				nodeOfResource= branchingPoint.remove(0);
			
			}
			else nodeOfResource = null;
			
		}
		
		
		
	}

	/**
	  * writes one line for channels and two lines for (IPIN and SINK)
	  * @param list
	  * @param currentNode
	  * @throws IOException
	  */
	private void writeLines(/*List<Object[]>*/ List<NodeOfResource> list, NodeOfResource currentNode) throws IOException {
		if(currentNode.getSibling() != null) {
			list.add(0, currentNode.getSibling());
		}
		if(currentNode.getData() instanceof SourceDummy) {
			NetlistBlock source= ((SourceDummy) currentNode.getData()).getBlock();
			if(source instanceof LogicBlock) {
				duplicateLineCache = ("OPIN (" + source.getX() + "," + source.getY() + ") " + "Pin: 4" +"\n");
				outputFileWriter.write(duplicateLineCache);
			}
			else if(source instanceof IOBlock){
				if(source.getSubblk_1()) {
					duplicateLineCache = ("OPIN (" + source.getX() + "," + source.getY() + ") " + "Pad: 1" +"\n");
					outputFileWriter.write(duplicateLineCache);
				} else {
					duplicateLineCache = ("OPIN (" + source.getX() + "," + source.getY() + ") " + "Pad: 0" +"\n");
					outputFileWriter.write(duplicateLineCache);
				}
			}
		}
		else if(currentNode.getData() instanceof ChannelWithCost) {//channel
			
			ChannelWithCost currentChannel = (ChannelWithCost)currentNode.getData();
			duplicateLineCache = ("CHAN" + (currentChannel.getHorizontal() ?"X (" : "Y (") + currentChannel.getX() + "," + currentChannel.getY() + ") Track: " + currentChannel.getTrackNum() + "\n");
			
			outputFileWriter.write(duplicateLineCache);
		}
		else {//instance of sink with cost 
		
			SinkWithCost currentSink = (SinkWithCost)currentNode.getData();
			duplicateLineCache = ("This should not be displayed");
			outputFileWriter.write("IPIN (" + currentSink.getX() + "," + currentSink.getY() + ") ");
			NetlistBlock currentBlock = currentSink.getSinkingBlock();
			if(currentBlock instanceof IOBlock) {
				outputFileWriter.write("Pad: " + (currentBlock.getSubblk_1()? 1 : 0) + "\n");
				outputFileWriter.write("SINK (" + currentSink.getX() + "," + currentSink.getY() + ") Pad: " + (currentBlock.getSubblk_1()? 1 : 0) +"\n");
			}
			else {
				outputFileWriter.write("Pin: " + currentSink.getPin() + "\n");
				outputFileWriter.write("SINK (" + currentSink.getX() + "," + currentSink.getY() + ") Class: 0" + "\n");
			}
		}
	}




	
}
