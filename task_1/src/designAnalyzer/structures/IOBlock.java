package designAnalyzer.structures;

public class IOBlock extends NetlistBlock {

	 
	
	public IOBlock(String newName, boolean subblk) {
		
		name= newName;
		pinAssignments= new Net[1]; ////save single net assignment to pin
		subblk_1= subblk;
		
	}
	
}
