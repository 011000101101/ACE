package designAnalyzer.structures.blocks;

import designAnalyzer.structures.nets.Net;

public class LogicBlock extends NetlistBlock {

	
	public LogicBlock(String newName, int newAssignedIdentifier) {

		super(newName, newAssignedIdentifier);
		
		/**
		 * save all net assignments to pins
		 */
		pinAssignments= new Net[6];
		
		subblk_1= false;
		
		
		
	}
	
	
	
	
	
	
}
