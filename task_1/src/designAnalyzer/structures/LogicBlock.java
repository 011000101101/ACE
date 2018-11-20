package designAnalyzer.structures;

public class LogicBlock extends NetlistBlock {

	
	public LogicBlock(String newName) {
		
		name= newName;
		pinAssignments= new Net[5]; //save all net assignments to pins except for clock pin
		subblk_1= false;
		
		
	}
	
	
	
	
	
	
}
