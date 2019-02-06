package router;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.awt.Point;
import java.io.FileNotFoundException;

import designAnalyzer.ParameterManager;
import designAnalyzer.errorReporter.ErrorReporter;
import designAnalyzer.inputParser.ArchitectureParser;
import designAnalyzer.inputParser.NetlistParser;
import designAnalyzer.inputParser.PlacementParser;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.StructureManager;
import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.LogicBlock;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import designAnalyzer.timingAnalyzer.TimingAnalyzer;
import placer.Placer;
import placer.outputWriter.PlacementWriter;
import router.structures.resourceWithCost.ChannelWithCost;
import router.structures.resourceWithCost.ResourceWithCost;
import router.structures.resourceWithCost.SinkWithCost;
import router.structures.resourceWithCost.SourceDummy;
import router.structures.tree.NodeOfResource;
import router.structures.blockPinCost.BlockPinCost;
import router.structures.blockPinCost.IOBlockPinCost;
import router.structures.blockPinCost.LogicBlockPinCost;
import router.outputWriter.RoutingWriter;

public class Router {

	/**
	 * reference to instance of input parser
	 */
	private static NetlistParser netlistParser;
	
	/**
	 * reference to instance of input parser
	 */
	private static ArchitectureParser architectureParser;
	
	/**
	 * 
	 */
	private static PlacementParser placementParser;
	
	/**
	 * reference to instance of routing writer
	 */
	private static RoutingWriter routingWriter;
	
	/**
	 * iteration limit for globalRouter
	 */
	private static int limit;
	
	/**
	 * collection holding all nets to be routed
	 */
	private static List<Net> nets;
	
	private static ParameterManager parameterManager;
	private static StructureManager structureManager;
	
	
	private static int[][][] channelUsedCount;
	private static double[][][] channelCostIndex;
	private static boolean[][][] channelCostNotYetComputedFlagIndex;
	
	private static ChannelWithCost[][][][] channelIndex;
	
	private static Map<NetlistBlock, BlockPinCost> blockPinCosts;

	/**
	 * counter for iterations of the signal router during one pass of the global router
	 */
	private static int iterationCounter;

	private static Collection<ChannelWithCost> usedChannels;

	private static Collection<BlockPinCost> usedSinkPins;

	private static int currentChannelWidth;

	private static Map<Net, NodeOfResource> currentRouting;

	private static int pFak;

	private static Map<Net, NodeOfResource> finalRouting;

	private static Collection<ResourceWithCost> tmpUsedResources;

	private static Collection<SinkWithCost> tmpSinks;

	private static int globalIterationCounter;

	private static int innerIterationCounter;

	public static void main(String[] args) {
		
		
		String netlistFilePath= args[0];
		String architectureFilePath= args[1];
		String placementFilePath= args[2];
		String routingFilePath = args[3];
		int[] commandLineInput = parseCommandlineArguments(args);
		try {
			architectureParser= new ArchitectureParser(architectureFilePath, commandLineInput);

			architectureParser.parseAll();
			ParameterManager.initialize(netlistFilePath, architectureFilePath, placementFilePath, architectureParser.getAllParameters());

			netlistParser= new NetlistParser(netlistFilePath);
			netlistParser.parseAll();
			
			placementParser = new PlacementParser(placementFilePath);
			placementParser.parseAll();
			
			routingWriter= new RoutingWriter();
			
			structureManager= StructureManager.getInstance();
			parameterManager= ParameterManager.getInstance();			
			
			channelUsedCount= new int[parameterManager.X_GRID_SIZE + 1][parameterManager.Y_GRID_SIZE + 1][2];
			channelCostIndex= new double[parameterManager.X_GRID_SIZE + 1][parameterManager.Y_GRID_SIZE + 1][2];
			channelCostNotYetComputedFlagIndex= new boolean[parameterManager.X_GRID_SIZE + 1][parameterManager.Y_GRID_SIZE + 1][2];
			
			channelIndex= new ChannelWithCost[1][1][1][0];
			
			usedChannels= new LinkedList<ChannelWithCost>();
			usedSinkPins= new LinkedList<BlockPinCost>();
			nets= new LinkedList<Net>();
			for(Net n : structureManager.getNetCollection()) { //generate all paths
				if(! n.getIsClocknNet()) {
					nets.add(n);
				}
			}
			tmpUsedResources= new LinkedList<ResourceWithCost>();
			tmpSinks= new LinkedList<SinkWithCost>();
			//TODO
			limit= 60;
			globalIterationCounter= 0;
			finalRouting= new HashMap<Net, NodeOfResource>(structureManager.getNetCollection().size());
			
			currentRouting= new HashMap<Net, NodeOfResource>(structureManager.getNetCollection().size());
			
			//channelIndex= new ChannelWithCost[parameterManager.X_GRID_SIZE + 1][parameterManager.Y_GRID_SIZE + 1][2];
			
			blockPinCosts= new HashMap<NetlistBlock, BlockPinCost>(structureManager.getBlockMap().values().size()); //TODO check hashmap initialization
			
			//TODO check if possible to start algorithm with real pFak of 1 instead of 0.5, would eliminate need to multiply every time
			//pFak halved every time it is used to be able to use int and shifting, instead of double and multiplication
			pFak= 1;
			
			int upperBoundInitial= 10;
			
			int upperBound= upperBoundInitial;
			int lowerBound= 0;
			currentChannelWidth = upperBound;
			int binarySearchCounter= 0;
			
			
			while(!globalRouter()) {
				lowerBound= upperBound;
				upperBound= 2 * upperBound;
				currentChannelWidth = upperBound;
			}
			
			while(lowerBound < upperBound -1) {
				currentChannelWidth = ((upperBound) + lowerBound)/2;
				if(globalRouter()) {
					upperBound = currentChannelWidth;
					for(Net n : currentRouting.keySet()) {
						finalRouting.put(n, currentRouting.get(n));
					}
				}
				else {
					lowerBound = currentChannelWidth;
				}
				globalIterationCounter++;

				pFak= pFak<<1;
			}
			
			
			
			
//			while(finalRouting.size() == 0) {
//				//TODO check if all possible ChannelWidths are tested...
//				lowerBound= ( upperBoundInitial * binarySearchCounter ) - binarySearchCounter;
//				upperBound= ( upperBoundInitial * (binarySearchCounter + 1) ) - binarySearchCounter;
//				while(lowerBound < upperBound -1) {
//					currentChannelWidth = (upperBound + lowerBound)/2;
//					if(globalRouter()) {
//						upperBound = currentChannelWidth;
//						for(Net n : currentRouting.keySet()) {
//							finalRouting.put(n, currentRouting.get(n));
//						}
//					}
//					else {
//						lowerBound = currentChannelWidth;
//					}
//					globalIterationCounter++;
//	
//					pFak= pFak<<1;
//				}
//				binarySearchCounter++;
//			}
			//T ODO binary search for minimal channel width, set global variable channelWidth to new value and execute globalRouter,
			//if returns false -> vergrößere channelWidth, else verkleinere, bis wert eindeutig festgelegt (upper and lower bound lokal speichern und in jeder iteration aufeinander zu bewegen...)
			//parameterManager.setChannelWidth(...); //set new channel width before execution of global router
//			globalRouter();
//			pFak= pFak<<1;

			
			routingWriter.write(routingFilePath, finalRouting);

			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ErrorReporter.reportFileNotFoundError(e.toString());
		} catch (Exception e) {
			System.err.println("Execution aborted as a result of previously detected errors.");
			e.printStackTrace();
		}
	}
	

	
	/**
	 * stores every argument beside args[0,1,2,3] (path arguments) for the initialization of the arch file in an array
	 * @param args 
	 * @return
	 */
	private static int[] parseCommandlineArguments(String[] args) {
		int[] parameterInitialized = new int[] {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
		for(int i = 3; i< args.length; i++) {
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
	 * global routing algorithm routing all nets repeatedly for up to [limit] number of times or until the placement has been routed validly
	 */
	private static boolean globalRouter() {
		

		//TODO remove
//		nukeAllResources(true, true);
		//TODO remove
		System.out.println("starting Global Router,                         currentChannelWidth: " + currentChannelWidth);
		
		if(channelIndex[0][0][0].length < currentChannelWidth) {
			ChannelWithCost[][][][] alreadyCreatedChannels= channelIndex;
			channelIndex= new ChannelWithCost[parameterManager.X_GRID_SIZE + 1][parameterManager.Y_GRID_SIZE + 1][2][currentChannelWidth];
			for(int j= 0; j < parameterManager.X_GRID_SIZE + 1; j++){
				for(int k= 0; k < parameterManager.Y_GRID_SIZE + 1; k++){
					for(int l= 0; l < 2; l++){
						for(int m= 0; m < alreadyCreatedChannels[0][0][0].length; m++){
							channelIndex[j][k][l][m]= alreadyCreatedChannels[j][k][l][m];
							alreadyCreatedChannels[j][k][l][m].invalidateCaches();
						}
						for(int m= alreadyCreatedChannels[0][0][0].length; m < currentChannelWidth; m++){
							channelIndex[j][k][l][m]= new ChannelWithCost(j, k, l == 1 ? true : false, m);
						}
					}
				}
			}
		}
		
		iterationCounter= 0 ; 
		boolean sharedResources= true;
		while(sharedResources && iterationCounter < limit) {
			currentRouting.clear();
			usedChannels.clear();
			usedSinkPins.clear();
			innerIterationCounter= 0;
			//TODO remove
//			nukeAllResources(true, false);
			for( Net n : nets) { 
				currentRouting.put(n, signalRouter(n)); 
				innerIterationCounter++;
			} 
			//foreach r in RRG.Edges do TODO implement this
				//r.updateHistory() ;
				//r.updateWith(pv) ;
			
			// no history for input pins, because there is no need (no blocking by 3rd party possible) 
			for(ChannelWithCost c : usedChannels) {
				c.updateHistoryCongestion(iterationCounter, globalIterationCounter);
			}
			sharedResources= sharedressources();
			iterationCounter++ ;
		}
		if (sharedResources) {
			return false ;
		}
		else {
			System.err.println("successfully routed, iterationCounter:" + iterationCounter );
			return true; //results of routing stored in global variable
		}
	}

	/**
	 * checks if the current routing complies to limits on shared resources or violates at least one of them
	 * @return true if a limit has been violated, false if the routing is valid
	 */
	private static boolean sharedressources() {
		boolean violated= false;
		for(ChannelWithCost c : usedChannels) {
			if(c.getUsedCounter(iterationCounter) > /*currentChannelWidth*/ 1) {
				System.err.println("violating Channel: " + c.toString() + ", counter: " + c.getUsedCounter(iterationCounter) + ", cost: " + c.getCost());
				violated= true;
			}
			c.resetCounters();
		}
		for(BlockPinCost p : usedSinkPins) {
			if(p.limitExceeded(iterationCounter)) {
				System.err.println("violating IPin: " + p.getBlock().toString() + ", counter: " + p.getUsedCounters(iterationCounter));
				violated= true;
			}
			p.resetCounters();
		}
		return violated; //no usage limit exceeded
	}

	/**
	 * signal routing algorithm routing a single Net
	 * @param currentNet the Net to be routed
	 * @return 
	 */
	private static NodeOfResource signalRouter(Net currentNet) {

		//TODO remove
//		nukeAllResources(false, false);
		
		//TODO remove
		System.out.println("starting Signal Router, source: " + currentNet.getSource().toString() + "innerIterationCounter: " + innerIterationCounter);
		
		//RtgRsrc i, j, w, v := nil ; (just coordinates, no seperate objects)
		PriorityQueue<ResourceWithCost> pQ= new PriorityQueue<ResourceWithCost>(
			parameterManager.X_GRID_SIZE * 2 + parameterManager.Y_GRID_SIZE * 2, 
			new Comparator<ResourceWithCost>() {

				@Override
				public int compare(ResourceWithCost point1, ResourceWithCost point2) {
					if(point1.getCost() < point2.getCost()) return -1; //verified
					if(point2.getCost() < point1.getCost()) return 1;
					return 0;
				}
			
			}
		); 
		
		NetlistBlock source= currentNet.getSource(); 
		SourceDummy sourceDummy= new SourceDummy(source, "" + innerIterationCounter, "" + iterationCounter, "" + globalIterationCounter, "" + currentChannelWidth);
		NodeOfResource routingTreeRoot= new NodeOfResource(sourceDummy); 
		
		initializeSourceCosts(source, sourceDummy, pQ);
		
		ResourceWithCost currentChannel;
		ChannelWithCost[] neighbouringChannels= new ChannelWithCost[6];
		
		for( NetlistBlock sink : currentNet.getSinks() ) { //loop over sinks

			
			
			
			
			
			
			//TODO remove
			System.out.println("routing path to sink: " + sink.getName() + " in Net: " + currentNet.getName() + ", size of pQ: " + pQ.size());
			
			/* route Verbindung zur Senke sink */
			
			
			
			ChannelWithCost[] inputChannels= getInputChannels(sink, pQ);
			
			BlockPinCost sinkPins= retrieveFromBlockPinCosts(sink);
			initializeSinkCosts(sink, inputChannels, sinkPins);
			
//			if(routingTreeRoot != null) {
//				routingTreeRoot.addAllChannelsToPriorityQueue(pQ);
//			}
			
			
			
			
			
			
			
			
			
			
			currentChannel = pullFromPQ(pQ); 
			
			while(currentChannel instanceof ChannelWithCost) {
				
				getNeighbouringChannels(((ChannelWithCost) currentChannel), neighbouringChannels);
				
				for(int j= 0; j < neighbouringChannels.length; j++) {
					
					if(neighbouringChannels[j] == null) break;
					
					//saves one method call...
					neighbouringChannels[j].setPathCostAndPreviousIfNotYetComputedInThisIteration(((ChannelWithCost) currentChannel), pFak, currentChannelWidth, innerIterationCounter, iterationCounter, globalIterationCounter);
					addToPQ(neighbouringChannels[j], pQ);
					
				}
				
				if(containsChannelWithCost(inputChannels, ((ChannelWithCost) currentChannel))) {
				
					SinkWithCost tmp= new SinkWithCost(sinkPins, (ChannelWithCost) currentChannel);
					
					tmp.setPathCostAndPreviousIfNotYetComputedInThisIteration(((ChannelWithCost) currentChannel), pFak, currentChannelWidth, innerIterationCounter, iterationCounter, globalIterationCounter);
//					System.err.println("cost of IPin: " + tmp.getCost());
					addToPQ(tmp, pQ);
					tmpSinks.add(tmp);
				}

				currentChannel = pullFromPQ(pQ); 
			}

			//sink reached
			
			//currentChannel.setSinkPinUsed(sink, iterationCounter);
			
			
			
			
			
			
			
			
			
			
			
			resetSinkCosts(inputChannels, pQ);

			
			ResourceWithCost tmpChannel;
			
			//currentChannel is IPIN/Sink
			NodeOfResource branchingPoint;
			
			NodeOfResource currentBranch= new NodeOfResource(currentChannel); //create new branch
			
			//TODO remove
			System.err.println(currentChannelWidth + ": " + iterationCounter + ": used Pin: " + currentChannel.toString() + ", counter is: " + currentChannel.getUsedCounter(iterationCounter));
			currentChannel.setUsed(iterationCounter); //set IPIN as used
			usedSinkPins.add(sinkPins);
			
//			System.err.println("IPin :" + currentChannel.toString() + ", cost: " + currentChannel.getCost() + ", currentCHannelWidth: " + currentChannelWidth + ", track: " + ((ChannelWithCost) currentChannel.getPrevious()).getTrackNum() + ", usedCounter: " + currentChannel.getUsedCounter(globalIterationCounter));
			
			
			//sink added to branch
			
			tmpChannel= currentChannel.getPrevious();
			branchingPoint= routingTreeRoot.findBranchingPoint(tmpChannel); //not null if tmpChannel is already part of routingTree (already used by different path of same net)
			currentBranch= new NodeOfResource(tmpChannel, currentBranch); //append tmpChannel to front of current branch
			//TODO remove
			System.err.println(currentChannelWidth + ": " + iterationCounter + ": used channel: " + tmpChannel.toString() + ", counter is: " + tmpChannel.getUsedCounter(iterationCounter));
			if(branchingPoint == null) { //branching point reached, tmpChannel holds branching point which is already used by this net, do not iterate usedCounter, while will be skipped
				tmpChannel.setUsed(iterationCounter);
				if(tmpChannel instanceof ChannelWithCost) {
					usedChannels.add((ChannelWithCost) tmpChannel);
				}
			}
//			System.err.println("tmpChannel :" + tmpChannel.toString() + ", usedCounter: " + tmpChannel.getUsedCounter(globalIterationCounter));
			
			
			//first channel before sink added to branch
			
			tmpChannel= tmpChannel.getPrevious();
			
			while (branchingPoint == null){

				branchingPoint= routingTreeRoot.findBranchingPoint(tmpChannel); //not null if tmpChannel is already part of routingTree (already used by different path of same net)
				currentBranch= new NodeOfResource(tmpChannel, currentBranch); //append tmpChannel to front of current branch
				//TODO remove
				System.err.println(currentChannelWidth + ": " + iterationCounter + ": used channel: " + tmpChannel.toString() + ", counter is: " + tmpChannel.getUsedCounter(iterationCounter));
				if(branchingPoint == null) { //branching point reached, tmpChannel holds branching point which is already used by this net, do not iterate usedCounter, while will be exited
					tmpChannel.setUsed(iterationCounter);
					if(tmpChannel instanceof ChannelWithCost) {
						usedChannels.add((ChannelWithCost) tmpChannel);
					}
				}
//				System.err.println("tmpChannel :" + tmpChannel.toString() + ", branchingPoint: " + branchingPoint);
				tmpChannel= tmpChannel.getPrevious();
				
			}
			
			
			//branch completed
			
			branchingPoint.addChild(currentBranch); //add branch to tree
			
			
			//branch added to tree
		
			for(SinkWithCost s : tmpSinks) {
				pQ.remove(s);
			}
			tmpSinks.clear();
			
			//remaining IPINs removed from pQ
			
			
		}
		
		return routingTreeRoot;

	}



	private static void nukeAllResources(boolean wipeUsage, boolean wipeHistory) {
		for(int j= 0; j < channelIndex.length; j++) {
			for(int k= 0; k < channelIndex[0].length; k++) {
				for(int l= 0; l < channelIndex[0][0].length; l++) {
					for(int m= 0; m < channelIndex[0][0][0].length; m++) {
						channelIndex[j][k][l][m].setAlreadyAdded(false, innerIterationCounter, iterationCounter);
						if(wipeUsage) {
							channelIndex[j][k][l][m].invalidateCaches();
						}
						else channelIndex[j][k][l][m].invalidateCostCache();
						if(wipeHistory) {
							channelIndex[j][k][l][m].resetHistory();
						}
					}
				}
			}
		}
	}



	private static BlockPinCost retrieveFromBlockPinCosts(NetlistBlock block) {
		BlockPinCost tmp= blockPinCosts.get(block);
		if(tmp == null) {
			if(block instanceof IOBlock) {
				tmp= new IOBlockPinCost(block, parameterManager.X_GRID_SIZE);
			}
			else{
				tmp= new LogicBlockPinCost(block);
			}
			blockPinCosts.put(block, tmp);
		}
		return tmp;
	}



	private static void getNeighbouringChannels(ChannelWithCost currentChannel, ChannelWithCost[] neighbouringChannels) {
		int channelsAdded= 0;
		int trackNum= currentChannel.getTrackNum();
		if(currentChannel.getHorizontal()) { //is a horizontal channel
			//no horizontal channels at x = 0
			if(currentChannel.getX() > 1) { //add left neighbor
			neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX() - 1][currentChannel.getY()][1][trackNum];
				channelsAdded++;
			}
			if(currentChannel.getX() < parameterManager.X_GRID_SIZE) { //add right neighbor
				neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX() + 1][currentChannel.getY()][1][trackNum];
				channelsAdded++;
			}
			if(currentChannel.getY() > 0) { //add bottom neighbors
				neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX()][currentChannel.getY()][0][trackNum];
				channelsAdded++;
				//no horizontal channels at x = 0
				//if(currentChannel.getX() > 0) {
				neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX() - 1][currentChannel.getY()][0][trackNum];
					channelsAdded++;
				//}
			}
			if(currentChannel.getY() < parameterManager.Y_GRID_SIZE) { //add top neighbors
				neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX()][currentChannel.getY() + 1][0][trackNum];
				channelsAdded++;
				//no horizontal channels at x = 0
				//if(currentChannel.getX() > 0) {
				neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX() - 1][currentChannel.getY() + 1][0][trackNum];
					channelsAdded++;
				//}
			}
		}
		else { //is a vertical channel
			//no vertical channels at y = 0
			if(currentChannel.getY() > 1) {  //add bottom neighbor
			neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX()][currentChannel.getY() - 1][0][trackNum];
				channelsAdded++;
			}
			if(currentChannel.getY() < parameterManager.Y_GRID_SIZE) {  //add top neighbor
				neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX()][currentChannel.getY() + 1][0][trackNum];
				channelsAdded++;
			}
			if(currentChannel.getX() > 0) {  //add left neighbors
				neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX()][currentChannel.getY()][1][trackNum];
				channelsAdded++;
				//no vertical channels at y = 0
				//if(currentChannel.getX() > 0) {
				neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX()][currentChannel.getY() - 1][1][trackNum];
					channelsAdded++;
				//}
			}
			if(currentChannel.getX() < parameterManager.X_GRID_SIZE) {  //add right neighbors
				neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX() + 1][currentChannel.getY()][1][trackNum];
				channelsAdded++;
				//no vertical channels at y = 0
				//if(currentChannel.getX() > 0) {
				neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX() + 1][currentChannel.getY() - 1][1][trackNum];
					channelsAdded++;
				//}
			}
			
		}
		
		for(int j= channelsAdded; j < neighbouringChannels.length; j++) {
			neighbouringChannels[j]= null;
		}
	}



	private static ChannelWithCost[] getInputChannels(NetlistBlock sink, PriorityQueue<ResourceWithCost> pQ) {
		
		BlockPinCost tmp= retrieveFromBlockPinCosts(sink);
		ChannelWithCost[] outputChannels;
		
		if(tmp instanceof IOBlockPinCost) { //IO Block
			outputChannels= new ChannelWithCost[currentChannelWidth];
			if(((IOBlockPinCost) tmp).getLeftOrRight()) { //left ot right io
				if(((IOBlockPinCost) tmp).getLeftOrTop()) { //left io
					for(int j= 0; j < currentChannelWidth; j++) {
						outputChannels[j]= channelIndex[sink.getX()][sink.getY()][0][j];
					}
				}
				else { //right io
					for(int j= 0; j < currentChannelWidth; j++) {
						outputChannels[j]= channelIndex[sink.getX() - 1][sink.getY()][0][j];
					}
				}
			}
			else { //top or bottom io
				if(((IOBlockPinCost) tmp).getLeftOrTop()) { //top io
					for(int j= 0; j < currentChannelWidth; j++) {
						outputChannels[j]= channelIndex[sink.getX()][sink.getY() - 1][1][j];
					}
				}
				else { //bottom io
					for(int j= 0; j < currentChannelWidth; j++) {
						outputChannels[j]= channelIndex[sink.getX()][sink.getY()][1][j];
					}
				}
			}
		}
		else { //logic Block
			outputChannels= new ChannelWithCost[4 * currentChannelWidth];
			for(int j= 0; j < currentChannelWidth; j++) { //return all four surrounding channels
				outputChannels[j]= channelIndex[sink.getX()][sink.getY()][1][j];
				outputChannels[j + currentChannelWidth]= channelIndex[sink.getX()][sink.getY()][0][j];
				outputChannels[j + 2*currentChannelWidth]= channelIndex[sink.getX()][sink.getY() - 1][1][j];
				outputChannels[j + 3*currentChannelWidth]= channelIndex[sink.getX() - 1][sink.getY()][0][j];
			}
		}
		for(int j= 0; j < outputChannels.length; j++) {
			if(outputChannels[j].alreadyAdded(innerIterationCounter, iterationCounter)) pQ.add(outputChannels[j]); //reenter already pulled channels if they are directly adjacent to the sink
//			System.out.println("err 013 " + outputChannels[j].toString());
		}
		return outputChannels;
	}

	private static boolean containsChannelWithCost(ChannelWithCost[] inputChannels, ChannelWithCost currentChannel) {
		for(int j= 0; j < inputChannels.length; j++) {
			if(currentChannel.equals(inputChannels[j])) return true;
		}
		return false;
	}



	private static void initializeSinkCosts(NetlistBlock sink, ChannelWithCost[] inputChannels, BlockPinCost sinkPins) {
		for(int j= 0; j < inputChannels.length; j++) {
			inputChannels[j].setLastChannel(sinkPins);
		}
	}

	private static void initializeSourceCosts(NetlistBlock source, SourceDummy sourceDummy, PriorityQueue<ResourceWithCost> pQ) {
		ChannelWithCost[] outputChannels= getOutputChannels(source);
		
		for(int j= 0; j < outputChannels.length; j++) {
			outputChannels[j].setPathCostAndPreviousIfNotYetComputedInThisIteration(sourceDummy, pFak, currentChannelWidth, innerIterationCounter, iterationCounter, globalIterationCounter); //initialize cost as first channel of path
			addToPQ(outputChannels[j], pQ); //add to priority queue
		}
	}

	private static void resetSinkCosts(ChannelWithCost[] inputChannels, PriorityQueue<ResourceWithCost> pQ) {
		for(int j= 0; j < inputChannels.length; j++) {
			//no need for guard: remove(...) is optional operation
			//if(pQ.contains(inputChannels[j])) {
				pQ.remove(inputChannels[j]);
			//}
			inputChannels[j].resetLastChannel();
		}
	}

	private static ChannelWithCost[] getOutputChannels(NetlistBlock source) {
		BlockPinCost tmp= retrieveFromBlockPinCosts(source);
		if(tmp instanceof IOBlockPinCost) { //IO Block
			ChannelWithCost[] outputChannels= new ChannelWithCost[currentChannelWidth];
			if(((IOBlockPinCost) tmp).getLeftOrRight()) { //left ot top io
				if(((IOBlockPinCost) tmp).getLeftOrTop()) { //left io
					for(int j= 0; j < currentChannelWidth; j++) {
						outputChannels[j]= channelIndex[source.getX()][source.getY()][0][j];
					}
				}
				else { //top io
					for(int j= 0; j < currentChannelWidth; j++) {
						outputChannels[j]= channelIndex[source.getX()][source.getY() - 1][1][j];
					}
				}
			}
			else { //right or bottom io
				if(((IOBlockPinCost) tmp).getLeftOrTop()) { //right io
					for(int j= 0; j < currentChannelWidth; j++) {
						outputChannels[j]= channelIndex[source.getX() - 1][source.getY()][0][j];
					}
				}
				else { //bottom io
					for(int j= 0; j < currentChannelWidth; j++) {
						outputChannels[j]= channelIndex[source.getX()][source.getY()][1][j];
					}
				}
			}
			return outputChannels;
		}
		else { //logic Block
			ChannelWithCost[] outputChannels= new ChannelWithCost[2 * currentChannelWidth];
			for(int j= 0; j < currentChannelWidth; j++) {
				outputChannels[j]= channelIndex[source.getX()][source.getY()][0][j];
				outputChannels[j + currentChannelWidth]= channelIndex[source.getX()][source.getY() - 1][1][j];
			}
			return outputChannels;
		}
	}



	/**
	 * updates the resource use counter to include currentChannel
	 * @param currentChannel the newly selected Channel
	 */
	private static void setChannelUsed(ChannelWithCost currentChannel) {
		channelUsedCount[currentChannel.getX()][currentChannel.getY()][currentChannel.getHorizontal() ? 1 : 0]= channelUsedCount[currentChannel.getX()][currentChannel.getY()][currentChannel.getHorizontal() ? 1 : 0] + 1;
	}
	
	private static void addToPQ(ResourceWithCost resource, PriorityQueue<ResourceWithCost> pQ) {
		if(!resource.alreadyAdded(innerIterationCounter, iterationCounter)) {
			resource.setAlreadyAdded(true, innerIterationCounter, iterationCounter);
			pQ.add(resource);
		}
//		else System.err.println("err 011: channel already added: " + resource.toString());
	}

	private static ResourceWithCost pullFromPQ(PriorityQueue<ResourceWithCost> pQ) {
		ResourceWithCost tmp= pQ.poll();
		tmpUsedResources.add(tmp);
		return tmp;
	}

}
