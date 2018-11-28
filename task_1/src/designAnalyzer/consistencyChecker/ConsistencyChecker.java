package designAnalyzer.consistencyChecker;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import designAnalyzer.ParameterManager;
import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.StructureManager;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

/**
 * 
 * @author Dennis Grotz, Rumei Ma, Vincenz Mechler
 *
 */
public class ConsistencyChecker {
	
	
	/**
	 * object managing all datastructure instances <br>
	 * -handling insertion, retrieval and others
	 */
	StructureManager structureManager;
	
	/**
	 * object managing all structural parameters 
	 */
	ParameterManager parameterManager;
	
	/**
	 * constructor
	 */
	public ConsistencyChecker() {
		
		structureManager= StructureManager.getInstance();
		parameterManager= ParameterManager.getInstance();
		
	}

	/**
	 * execute remaining consistency checks on design that weren't already done by parsers
	 * @param routingFileProvided flag if routing file has been provided by the user
	 */
	public void checkConsistency(boolean routingFileProvided) {
		
		checkPlacement();
		
		checkNets(routingFileProvided);
		
	}
	
	
	/**
	 * perform necessary checks on all nets
	 * @param routingFileProvided flag if routing file has been provided by the user
	 */
	private void checkNets(boolean routingFileProvided) {

		Collection<Net> nets= structureManager.getNetCollection();
		
		for(Net n : nets) {
			checkAtLeastOneSource(n);
			checkAtLeastOneSink(n);
			if(routingFileProvided) {
				checkAllSinksRouted(n);
			}
		}
		
	}

	
	/**
	 * checks if all sinks of this net have been routed
	 * @param n net to be checked
	 */
	private void checkAllSinksRouted(Net n) {

		Map<NetlistBlock, Boolean> sinks= n.getSinkMap();
		
		for(NetlistBlock b : sinks.keySet()) {
			if(!sinks.get(b)) {
				ErrorReporter.reportSinkNotRoutedError(n, b);
			}
		}
		
	}

	
	/**
	 * checks that the net has at least one sink
	 * @param n net to be checked
	 */
	private void checkAtLeastOneSink(Net n) {

		if(n.getSinks().isEmpty()) {
			ErrorReporter.reportNoSinkError(n);
		}
		
	}

	
	/**
	 * checks that the net has a source
	 * @param n net to be checked
	 */
	private void checkAtLeastOneSource(Net n) {

		if(n.getSource() == null) {
			ErrorReporter.reportNoSourceError(n);
		}
		
	}
	

	/**
	 * checks that all blocks have been placed
	 */
	private void checkPlacement() {
		
		Collection<NetlistBlock> blocks=  structureManager.getBlockMap().values();
		
		for( NetlistBlock b : blocks) {
			checkIfPlaced(b);
		}
		
	}
	

	/**
	 * checks if the block has been placed
	 * @param b block to be checked
	 */
	private void checkIfPlaced(NetlistBlock b) {

		if(b.getX() == -1 || b.getY() == -1) {
			ErrorReporter.reportBlockNotPlacedError(b);
		}
		
	}

}
