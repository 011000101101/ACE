package designAnalyzer.errorReporter;

import designAnalyzer.inputParser.AbstractInputParser;
import designAnalyzer.inputParser.NetlistParser;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

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

	public static void reportClockNetConnectionError(Net temp, boolean isClockNet) {
		// TODO Auto-generated method stub
		
	}

	public static void reportFileNotFoundError(String string) {
		// TODO Auto-generated method stub
		
	}

	public static void reportDuplicateBlockError(NetlistBlock currentBlock) {
		// TODO Auto-generated method stub
		
	}

	public static void reportBlockNotPlacedError(NetlistBlock block) {
		// TODO Auto-generated method stub
		
	}

	public static void reportBlockPlacedOutOfBoundsError(NetlistBlock block) {
		// TODO Auto-generated method stub
		
	}

	public static void reportDuplicateBlockPlacementError(NetlistBlock block) {
		// TODO Auto-generated method stub
		
	}

	public static void reportDuplicateSourceError(Net net, NetlistBlock currentBlock) {
		// TODO Auto-generated method stub
		
	}
}
