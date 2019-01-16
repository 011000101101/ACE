package placer.outputWriter;

import java.io.IOException;

import designAnalyzer.structures.StructureManager;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

/**
 * 
 * @author Dennis Grotz, Rumei Ma, Vincenz Mechler
 *
 */
public class PlacementWriter extends AbstractWriter {

	private String referencedNetlistFileName;
	private String referencedArchFileName;
	
	public PlacementWriter(String newReferencedNetlistFileName, String newReferencedArchFileName) {
		super();
		structureManager= StructureManager.getInstance();
		referencedNetlistFileName= newReferencedNetlistFileName;
		referencedArchFileName= newReferencedArchFileName;
	}

	@Override
	protected void writeHeader() throws IOException {

		outputFileWriter.write("Netlist file: " + referencedNetlistFileName + " Architecture file: " + referencedArchFileName);
		outputFileWriter.write("\n" + "Array size: " + parameterManager.X_GRID_SIZE + " x " + parameterManager.Y_GRID_SIZE + " logic blocks");
		outputFileWriter.write("\n");
		
	}

	@Override
	protected void writeAllBlocks() throws IOException {

		for(NetlistBlock b : structureManager.getBlockMap().values()) {
			writeOneBlock(b);
		}

	}

	/**
	 * writes one block of lines into the output file
	 */
	private void writeOneBlock(NetlistBlock b) throws IOException {

		outputFileWriter.write("\n" + b.getName() + " " + b.getX() + " " + b.getY() + " " + (b.getSubblk_1() ? "1" : "0" ) + " #" + b.getBlockNumber() ); //no space between '#' and block number to be compatible with placement parser

	}

}
