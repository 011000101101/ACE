package designAnalyzer;

import java.io.FileNotFoundException;

import designAnalyzer.consistencyChecker.ConsistencyChecker;
import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.inputParser.ArchitectureParser;
import designAnalyzer.inputParser.NetlistParser;
import designAnalyzer.inputParser.PlacementParser;
import designAnalyzer.inputParser.RoutingParser;

public class DesignAnalyzer {
	
	private static boolean routingFileProvided= false;

	private static NetlistParser netlistParser;
	private static ArchitectureParser architectureParser;
	private static PlacementParser placementParser;
	private static RoutingParser routingParser;
	
	private static ConsistencyChecker consistencyChecker;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//TODO parse command line arguments
		String netlistFilePath= null;
		String architectureFilePath= null;
		String placementFilePath= null;
		String routingFilePath= null;
		
		int xSize= null;
		int ySize= null;
		
		
		consistencyChecker= new ConsistencyChecker(xSize, ySize);
		
		try {
			
			netlistParser= new NetlistParser(netlistFilePath);
			architectureParser= new ArchitectureParser(architectureFilePath);
			placementParser= new PlacementParser(placementFilePath);
			if(routingFileProvided) {
				routingParser= new RoutingParser(routingFilePath);
			}
			
			parse();
			
			consistencyChecker.checkConsistency(routingFileProvided);
			
			analyze();
			
		} catch (FileNotFoundException e) {
			ErrorReporter.reportFileNotFoundError(e.toString());
		}
	}
	
	/**
	 * parses all given input files<br>
	 * <b>requires:</b> routing parser is either marked as not used via routingFileProvided, or initialized, all other parsers are initialized<br>
	 * <b>post-state:</b> all provided input files have been parsed into consistent data structures managed by the StructureManager, or errors have been printed
	 */
	private static void parse() {

		architectureParser.parseAll();
		
		netlistParser.parseAll();
		placementParser.parseAll();
		if(routingFileProvided) {
			routingParser.parseAll();
		}
		
	}
	
	private static void analyze() {
		
		
		analyzeTiming(routingFileProvided);
		
	}

}
