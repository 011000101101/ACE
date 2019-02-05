package router.outputWriter;

import java.io.IOException;

import designAnalyzer.structures.Net;
import designAnalyzer.structures.pathElements.PathElement;
import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.LogicBlock;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import designAnalyzer.structures.pathElements.pins.OPin;
import placer.outputWriter.AbstractWriter;

public class RoutingWriter extends AbstractWriter {


	@Override
	protected void writeHeader() throws IOException {

		outputFileWriter.write("Array size: " + parameterManager.X_GRID_SIZE + " x " + parameterManager.Y_GRID_SIZE + " logic blocks");
		outputFileWriter.write("\n");
		outputFileWriter.write("\n" + "Routing: " + "\n");
		
	}

	@Override
	protected void writeAllBlocks() throws IOException {

		for(Net n : structureManager.getNetCollection()) {
			writeOneBlock(n);
		}
		
	}

	private void writeOneBlock(Net n) throws IOException {
		
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
		writePath(n.getSource());
	}

	private void writePath(PathElement currentPathElement) throws IOException {
		if(!(currentPathElement instanceof OPin)) {
			outputFileWriter.write(currentPathElement.forRoutingFile()); //TODO how is next for each channel saved, and how can we access them. Idea: maybe also create a variable for the class ChannelX and Y, so the access is easier
		}else
		
	}

	
}
