package designAnalyzer.structures.pathElements.blocks;

import designAnalyzer.structures.Net;

public class IOBlock extends NetlistBlock {

	 
	
	public IOBlock(String newName, int newAssignedIdentifier) {
		
		super(newName, newAssignedIdentifier);
		
		pinAssignments= new Net[2];
		
	}
	
}
