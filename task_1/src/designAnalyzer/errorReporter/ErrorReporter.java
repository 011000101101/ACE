package designAnalyzer.errorReporter;

import designAnalyzer.inputParser.AbstractInputParser;
import designAnalyzer.inputParser.ArchitectureParser;
import designAnalyzer.inputParser.NetlistParser;
import designAnalyzer.inputParser.PlacementParser;
import designAnalyzer.inputParser.RoutingParser;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.StructureManager;
import designAnalyzer.structures.pathElements.PathElement;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

public class ErrorReporter {

	
	public static void reportSyntaxError(String expectedToken, String readToken, AbstractInputParser parser) {
		
		System.err.println("Syntax error in " + parser.getFileName() + " at line " + parser.getLineNumber() + ": expected '" + expectedToken + "', read '" + readToken + "'.");

	}
	
	public static void reportDuplicatePinAssignmentError(NetlistBlock affectedBlock, int pinNumber, Net netToConnect) {
		
		System.err.println("Pin " + pinNumber + " in Block " + affectedBlock.getName() + " with number " + affectedBlock.getBlockNumber() + " has been already used. The net that wants to connect with this pin is " + netToConnect.getName() + " # " + netToConnect.getNumber() + ".");
	}

	public static void reportInvalidTokenCount(int i, AbstractInputParser parser) {
		
		System.err.println("The line has more or less tokens than expected. The expected length of the line " + parser.getLineNumber() + " is " + i + ".");
		
	}

	public static void reportInconsistentNamingError(String expectedName, String readName, AbstractInputParser parser) {
		
		System.err.println("Inconsistent name in " + parser.getFileName() + " at line " + parser.getLineNumber() + ". Expected: " + expectedName + " Read: " + readName);
		
	}

	public static void reportSyntaxMultipleChoiceError(String[] expectedTokens, String readToken, AbstractInputParser parser) {

		System.err.println("Syntax error in Netlist file at line " + parser.getLineNumber() + ": expected one of " + expectedTokens.toString() + ", read '" + readToken + "'.");

	}

	public static void reportClockNetConnectionError(Net temp, boolean isClockNet) {
		
		System.err.println("Whether the net " + temp.getName() + " #" + temp.getNumber() + " is a clock net or not is not consistent with the flag that has been already set. ");
		
	}

	public static void reportFileNotFoundError(String errorReport) {
		
		System.err.println(errorReport);
		
	}

	public static void reportDuplicateBlockError(NetlistBlock currentBlock) {
	
		System.err.println("The block " + currentBlock.getName() + " # " + currentBlock.getBlockNumber() + " already exists in the hashmap.");
		
	}

	public static void reportBlockPlacedOutOfBoundsError(NetlistBlock block) {
		
		System.err.println("The coordinates for the block " + block.getName() + " #" + block.getName() +" are invalid: (" + block.getX() + "," + block.getY() + ")");
		
	}

	public static void reportDuplicateBlockPlacementError(NetlistBlock block) { 
		
		System.err.println("The coordinates for the block " + block.getName() + " #" + block.getName() +" are already in use: (" + block.getX() + "," + block.getY() + ")");
				
	}

	public static void reportDuplicateSourceError(Net net, NetlistBlock currentBlock) {
	
		System.err.println("The net " + net.getName() +" #" + net.getNumber() + " wants to set the block " + currentBlock.getName() + " as a source, but already has another source");
		
	}

	public static void reportInvalidRoutingError(PathElement alreadyRouted, PathElement nextNode, String message, AbstractInputParser parser) {
		
		System.err.println("The last previous path element is " + alreadyRouted.getName() + "\n The current path element is " + nextNode.getName());
		System.err.println("File: " + parser.getFileName() + " Line: "+parser.getLineNumber());
		System.err.println(message);
		
	}

	public static void reportInconsistentArgumentError(int expected, String readToken, String message, AbstractInputParser parser) {
		
		System.err.println("The expected " + message + expected + " doesn't match with " + readToken + " in line " + parser.getLineNumber() );
		
	}

	public static void reportMissingBlockError(int xCoordinate, int yCoordinate, AbstractInputParser parser) {
		
		System.err.println("There is no block on coordinates (" + xCoordinate + "," + yCoordinate + "). Line " + parser.getLineNumber() + " File " + parser.getFileName());
		
	}

	public static void reportMissingNetError(String name, AbstractInputParser parser) {
		
		System.err.println("The net " + name +" is not stored in hashmap. Line " + parser.getLineNumber() + " File " + parser.getFileName());
		
	}

	public static void reportBlockNotFoundError(AbstractInputParser parser) {
		
		System.err.println("The block couldn't be found: Line " + parser.getLineNumber() + " File " + parser.getFileName());
		
	}

	public static void reportInvalidSourceRoutingNetError(Net currentNet, NetlistBlock invalidSource,
			AbstractInputParser parser) {
		
		System.err.println("The source of net " + currentNet.getName() + " #" + currentNet.getNumber() + " doesn't match with "+ invalidSource.getName() + ". Line " + parser.getLineNumber() + " File " + parser.getFileName());
		
	}

	public static void reportExcessSinkRoutingNetError(Net currentNet, NetlistBlock excessSink,
			AbstractInputParser parser) {
		
		System.err.println("The net " + currentNet.getName() + " #" + currentNet.getNumber() + " already has the sink " + excessSink.getName() + " #" + excessSink.getBlockNumber() + ". Line " + parser.getLineNumber() + " File " + parser.getFileName());
		
	}

	public static void reportPinConnectionRoutingError(NetlistBlock lastBlock, String pinType, AbstractInputParser parser) {
		
		System.err.println("In Line " + parser.getLineNumber() + " File " + parser.getFileName() + " is an error. The block " + lastBlock.getName() + " #" + lastBlock.getBlockNumber() + " has a problem with his type \"" + pinType + "\".");
	}

	public static void reportInvalidConnectionPointRoutingError(Net currentNet, int xCoordinate,
			int yCoordinate, String trackNumber, boolean isChannelX, AbstractInputParser parser) {
		
		System.err.println("In Line " + parser.getLineNumber() + " File " + parser.getFileName() + " is an error. The net " + currentNet.getName() + " #" + currentNet.getNumber() +" does not have a channel" + (isChannelX ? "X" : "Y") + " at ("+ xCoordinate + "," + yCoordinate + ")" + " and the track: " + trackNumber);
		
	}

	public static void reportArchFileNotComplete(int numberOfExpectedParameter, AbstractInputParser parser) {
		
		System.err.println("There are more or less lines or information given than the expected amount of parameter: " + numberOfExpectedParameter  +" defined in *.arch. In Line " + parser.getLineNumber() + " File " + parser.getFileName() + "");
		
	}

	public static void reportParameterOutOfBoundsError(int bound, int actualValue, String message,
			AbstractInputParser parser) {
		
		System.err.println("Out of bounds error: " + message + "\n the boundary is between 0(y) or 1(x) and " + bound +". The actual value: "+actualValue);
		System.err.println("In Line " + parser.getLineNumber() + " File " + parser.getFileName());
		
	}

	public static void reportDuplicateChannelUsageError(int xCoordinate, int yCoordinate, int trackNum, boolean isChanX,
			AbstractInputParser parser) {
		
		System.err.println("The channel"+ (isChanX ? "X at (" : "Y at (") + xCoordinate + "," + yCoordinate + ") with the track number " + trackNum);
		System.err.println("In Line " + parser.getLineNumber() + " File " + parser.getFileName());
		
	}

	 /**
	  * pin not on same coordinates as owning block
	  * @param lastBlock owner of this pin
	  * @param xCoordinate X coordinate of this pin
	  * @param yCoordinate Y coordinate of this pin
	  * @param parser instance of calling parser for retrieval of current line number etc.
	  */
	public static void reportPinPlacementRoutingError(NetlistBlock lastBlock, int xCoordinate, int yCoordinate,
			AbstractInputParser parser) {
		
		System.err.println("In Line " + parser.getLineNumber() + " File " + parser.getFileName() + ": The coordinates of the current Line ( " + lastBlock.getX() + ", " + lastBlock.getY() + ")" + " do not match with the coordinates of the previous line ( " + xCoordinate + "," + yCoordinate + "). ");
				
	}
}
