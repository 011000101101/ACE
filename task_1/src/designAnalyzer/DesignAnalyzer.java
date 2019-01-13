package designAnalyzer;

import java.io.FileNotFoundException;

import designAnalyzer.consistencyChecker.ConsistencyChecker;
import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.inputParser.ArchitectureParser;
import designAnalyzer.inputParser.NetlistParser;
import designAnalyzer.inputParser.PlacementParser;
import designAnalyzer.inputParser.RoutingParser;
import designAnalyzer.timingAnalyzer.TimingAnalyzer;

/**
 * 
 * @author Dennis Grotz, Rumei Ma, Vincenz Mechler
 *
 */
public class DesignAnalyzer {
	
	/**
	 * flag indicating if a routing file has been provided by the user
	 */
	private static boolean routingFileProvided= false;

	/**
	 * reference to instance of input parser
	 */
	private static NetlistParser netlistParser;
	
	/**
	 * reference to instance of input parser
	 */
	private static ArchitectureParser architectureParser;
	
	/**
	 * reference to instance of input parser
	 */
	private static PlacementParser placementParser;
	
	/**
	 * reference to instance of input parser
	 */
	private static RoutingParser routingParser;
	
	
	/**
	 * reference to instance of consistency checker
	 */
	private static ConsistencyChecker consistencyChecker;
	
	
	/**
	 * reference to instance of timing analyzer
	 */
	private static TimingAnalyzer timingAnalyzer;
	//public static int[] parameterInitialized;
	
	
	/**
	 * main method
	 * @param args
	 */
	public static void main(String[] args) {

		
		String netlistFilePath= args[0];
		String architectureFilePath= args[1];
		String placementFilePath= args[2];
		String routingFilePath = null;
		
		if(args.length >= 4 && args[3].endsWith(".r")) {
			routingFileProvided= true;
			routingFilePath= args[3];
		}
		
		
		
		
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

			consistencyChecker= new ConsistencyChecker();
			consistencyChecker.checkConsistency(routingFileProvided);

			timingAnalyzer= TimingAnalyzer.getInstance();
			
			analyze();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ErrorReporter.reportFileNotFoundError(e.toString());
		} catch (Exception e) {
			System.err.println("Execution aborted as a result of previously detected errors.");
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
	
	/**
	 * analyze or estimate timing depending on rountingFileProvided and output results
	 */
	private static void analyze() {
		
		
		timingAnalyzer.analyzeTiming(routingFileProvided);
		
	}

}
