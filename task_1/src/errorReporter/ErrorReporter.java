package errorReporter;

import designAnalyzer.inputParser.AbstractInputParser;
import designAnalyzer.inputParser.NetlistParser;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.NetlistBlock;

public class ErrorReporter {

	
	public static void reportSyntaxError(String expectedToken, String readToken, AbstractInputParser parser) {
		
		System.err.println("Syntax error in Netlist file at line " + parser.getLineNumber() + ": expected '" + expectedToken + "', read '" + readToken + "'.");

	}
	
	public static void reportDuplicatePinAssignmentError(NetlistBlock affectedBlock, int pinNumber, Net netToConnect) {
		// TODO implement error reporting
	}

	public static void reportInvalidTokenCount(int i, NetlistParser netlistParser) {
		// TODO Auto-generated method stub
		
	}

	public static void reportInconsistentNamingError(String name, String string, AbstractInputParser parser) {
		// TODO Auto-generated method stub
		
	}

	public static void reportSyntaxMultipleChoiceError(String[] expectedTokens, String readToken, AbstractInputParser parser) {

		System.err.println("Syntax error in Netlist file at line " + parser.getLineNumber() + ": expected one of " + expectedTokens.toString() + ", read '" + readToken + "'.");

	}
}
