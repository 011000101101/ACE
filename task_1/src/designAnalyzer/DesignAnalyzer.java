package designAnalyzer;

import java.io.FileNotFoundException;

import designAnalyzer.consistencyChecker.ConsistencyChecker;
import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.inputParser.ArchitectureParser;
import designAnalyzer.inputParser.NetlistParser;
import designAnalyzer.inputParser.PlacementParser;
import designAnalyzer.inputParser.RoutingParser;
import designAnalyzer.timingAnalyzer.TimingAnalyzer;

public class DesignAnalyzer {
	
	private static boolean routingFileProvided= false;

	private static NetlistParser netlistParser;
	private static ArchitectureParser architectureParser;
	private static PlacementParser placementParser;
	private static RoutingParser routingParser;
	
	private static ConsistencyChecker consistencyChecker;
	
	private static TimingAnalyzer timingAnalyzer;
	//public static int[] parameterInitialized;
	
	public static void main(String[] args) {

		
		String netlistFilePath= args[0];
		String architectureFilePath= args[1];
		String placementFilePath= args[2];
		String routingFilePath = null;
		if(args.length >= 4 && args[3].endsWith(".r")) {
			System.out.println("checkpoint");
			routingFileProvided= true;
			routingFilePath= args[3];
		}
		
		//T ODO is x and y obligated?
		//int xSize= null;
		//int ySize= null;
		
		
		//consistencyChecker= new ConsistencyChecker(xSize, ySize);
		
		try {
			int[] commandLineInput = parseCommandlineArguments(args); //array in form from int to initialize architectureParser with
			architectureParser= new ArchitectureParser(architectureFilePath, commandLineInput);

			architectureParser.parseAll();
			ParameterManager.initialize(netlistFilePath, architectureFilePath, placementFilePath, architectureParser.getAllParameters());

			netlistParser= new NetlistParser(netlistFilePath);
			placementParser= new PlacementParser(placementFilePath);
			if(routingFileProvided) {
				routingParser= new RoutingParser(routingFilePath);
			}
			
			parse();
			
			//consistencyChecker.checkConsistency(routingFileProvided);

			timingAnalyzer= new TimingAnalyzer();
			
			analyze();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ErrorReporter.reportFileNotFoundError(e.toString());
		} 
	}
	
	/**
	 * stores every argument beside args[0,1,2,3] (path arguments) for the initialization of the arch file in an array
	 * @param args 
	 * @return
	 */
	private static int[] parseCommandlineArguments(String[] args) {
		int[] parameterInitialized = new int[] {-1,-1,-1,-1,-1,-1,-1,-1,-1};
		for(int i = (routingFileProvided ? 4 : 3); i< args.length; i++) {
			switch(args[i]) {
			case "-X":
				i++;
				parameterInitialized[0]= Integer.valueOf(args[i]);
			break;
			
			case "-Y":
				i++;
				parameterInitialized[1]= Integer.valueOf(args[i]);
			break;
			
			case "-W":
				i++;
				parameterInitialized[2]= Integer.valueOf(args[i]);
			break;
			
			case "-Tipad":
				i++;
				parameterInitialized[3]= Integer.valueOf(args[i]);
			break;
			
			case "-Topad":
				i++;
				parameterInitialized[4]= Integer.valueOf(args[i]);
			break;
			
			case "-Tswitch":
				i++;
				parameterInitialized[5]= Integer.valueOf(args[i]);
			break;
			
			case "-Tcomb":
				i++;
				parameterInitialized[6]= Integer.valueOf(args[i]);
			break;
			
			case "-TFFin":
				i++;
				parameterInitialized[7]= Integer.valueOf(args[i]);
			break;
			
			case "-TFFout":
				i++;
				parameterInitialized[8]= Integer.valueOf(args[i]);
			break;
			
			}
		}
		return parameterInitialized;
	}
	

	/**
	 * parses all given input files<br>
	 * <b>requires:</b> routing parser is either marked as not used via routingFileProvided, or initialized, all other parsers are initialized<br>
	 * <b>post-state:</b> all provided input files have been parsed into consistent data structures managed by the StructureManager, or errors have been printed
	 */
	private static void parse() {
		
		netlistParser.parseAll();
		placementParser.parseAll();
		if(routingFileProvided) {
			routingParser.parseAll();
		}
		
	}
	
	private static void analyze() {
		
		
		timingAnalyzer.analyzeTiming(routingFileProvided);
		
	}

}
