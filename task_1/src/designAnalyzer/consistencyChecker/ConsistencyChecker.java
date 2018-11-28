package designAnalyzer.consistencyChecker;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import designAnalyzer.ParameterManager;
import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.StructureManager;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;

public class ConsistencyChecker {
	
	StructureManager structureManager;
	ParameterManager parameterManager;
	
	public ConsistencyChecker(int newXSize, int newYSize) {
		
		structureManager= StructureManager.getInstance();
		parameterManager= ParameterManager.getInstance();
		
	}

	public void checkConsistency(boolean routingFileProvided) {
		
		checkPlacement();
		
		checkNets(routingFileProvided);
		
	}
	
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

	private void checkAllSinksRouted(Net n) {

		Map<NetlistBlock, Boolean> sinks= n.getSinkMap();
		
		for(NetlistBlock b : sinks.keySet()) {
			if(!sinks.get(b)) {
				ErrorReporter.reportSinkNotRoutedError(n, b);
			}
		}
		
	}

	private void checkAtLeastOneSink(Net n) {

		if(n.getSinks().isEmpty()) {
			ErrorReporter.reportNoSinkError(n);
		}
		
	}

	private void checkAtLeastOneSource(Net n) {

		if(n.getSource() == null) {
			ErrorReporter.reportNoSourceError(n);
		}
		
	}

	private void checkPlacement() {
		
		Collection<NetlistBlock> blocks=  structureManager.getBlockMap().values();
		
		for( NetlistBlock b : blocks) {
			checkIfPlaced(b);
		}
		
	}

	private void checkIfPlaced(NetlistBlock b) {

		if(b.getX() == -1 || b.getY() == -1) {
			ErrorReporter.reportBlockNotPlacedError(b);
		}
		
	}

}
