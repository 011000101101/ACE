package designAnalyzer.consistencyChecker;

import java.util.HashMap;

import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.structures.StructureManager;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

public class ConsistencyChecker {
	
	/**
	 * contains a flag for each possible block position on the FPGA if a block has already been placed there <br>
	 * also contains possible IOBlock positions
	 */
	boolean[][] usedCells;
	
	int xSize;
	
	int ySize;
	
	StructureManager structureManager;
	
	public ConsistencyChecker(int newXSize, int newYSize) {

		xSize= newXSize;
		ySize= newYSize;
		usedCells= new boolean[xSize+2][ySize+2];
		
		structureManager= StructureManager.getInstance();
		
	}

	public void checkConsistency(boolean routingFileProvided) {
		
		checkPlacement();
		
		if(routingFileProvided) {
			checkRouting();
		}
		
	}
	
	private void checkPlacement() {
		
		HashMap<String, NetlistBlock> blockMap= structureManager.getBlockMap();
		
		blockMap.values().forEach((NetlistBlock block) -> checkIfPlacedCorrectly(block)); 
		
	}

	private void checkIfPlacedCorrectly(NetlistBlock block) {
		
		int xCoordinate= block.getX();
		int yCoordinate= block.getY();

		if(xCoordinate == -1 || yCoordinate == -1) {
			ErrorReporter.reportBlockNotPlacedError(block);
		}
		
		/*//TODO remove in final version
			//redundant to checks performed when placing a block (in class StructureManager)
		
		else if(xCoordinate < 0 || xCoordinate > (xSize + 1) || yCoordinate < 0 || yCoordinate > (ySize + 1) ) {
			ErrorReporter.reportBlockPlacedOutOfBoundsError(block);
		}
		else if(usedCells[xCoordinate][yCoordinate]) {
			ErrorReporter.reportDuplicateBlockPlacementError(block);
		}
		else {
			usedCells[xCoordinate][yCoordinate]= true;
		}
		*/
		
	}

}
