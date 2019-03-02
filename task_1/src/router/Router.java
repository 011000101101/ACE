package router;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
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
import router.structures.resourceWithCost.ChannelWithCost;
import router.structures.resourceWithCost.ResourceWithCost;
import router.structures.resourceWithCost.SinkWithCost;
import router.structures.resourceWithCost.SourceDummy;
import router.structures.tree.NodeOfResource;
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
	
	/**
	 * object managing all structural parameters 
	 */
	private static ParameterManager parameterManager;
	
	/**
	 * object managing all datastructure instances 
	 * -handling insertion, retrieval and others
	 */
	private static StructureManager structureManager;
	
	/**
	 * stores for every track of the fpga with the width currentChannelWidth a ChannelWithCost Object
	 */
	private static ChannelWithCost[][][][] channelIndex;

	/**
	 * counter for iterations of the signal router during one pass of the global router
	 */
	private static int iterationCounter;
	
	/**
	 * variable storing current channel width, which depending on the current global router iteration
	 */
	private static int currentChannelWidth;

	/**
	 * current routing is stored in a hashmap with a net as key and the root node as value
	 */
	private static Map<Net, NodeOfResource> currentRouting;

	/**
	 * p factor depending on global router iteration. used for cost calculation
	 */
	private static double pFak;

	/**
	 * final routing is stored in a hashmap with a net as key and the root node as value. used for RoutingFileWriter
	 */
	private static Map<Net, NodeOfResource> finalRouting;

	/**
	 * count increases every time global iteration is called
	 */
	private static int globalIterationCounter;

	/**
	 * increases whenever signal router run through. reset when every net used by signal router.
	 */
	private static int innerIterationCounter;
	
	
	
	private static Map<NetlistBlock, SinkWithCost>[] sinkWithCostMaps;

	private static Collection<ResourceWithCost> polledResources;
	
	private static NodeOfResource nodeOfResourceDummy;

	public static void main(String[] args) {
		
		
		long startTime = System.currentTimeMillis();
		
		String netlistFilePath= args[0];
		String architectureFilePath= args[1];
		String placementFilePath= args[2];
		String routingFilePath = args[3];
		int[] commandLineInput = parseCommandlineArguments(args);
		try {
			int w= commandLineInput[2];
			
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
			
			channelIndex= new ChannelWithCost[1][1][1][0];
			nets= new LinkedList<Net>();
			for(Net n : structureManager.getNetCollection()) { //generate all paths
				if(! n.getIsClocknNet()) {
					nets.add(n);
				}
			}
			//TODO
			limit= 45;
			globalIterationCounter= 0;
			finalRouting= new HashMap<Net, NodeOfResource>((int) (structureManager.getNetCollection().size() / 0.75) + 1);
			
			currentRouting= new HashMap<Net, NodeOfResource>((int) (structureManager.getNetCollection().size() / 0.75) + 1);
			
			polledResources= new LinkedList<ResourceWithCost>();
			
			//create dummy routing for each net to be able to unroute each net before its first routing
			nodeOfResourceDummy= new NodeOfResource(new SourceDummy(null));
			for(Net n : nets) {
				currentRouting.put(n, nodeOfResourceDummy);
			}
			
			//TODO comments
			sinkWithCostMaps= new Map[5];
			for(int j= 0; j < 5; j++) {
				sinkWithCostMaps[j]= new HashMap<NetlistBlock, SinkWithCost>(structureManager.getBlockMap().size()); //more overall blocks than blocks of a specific type
			}
			for(NetlistBlock b : structureManager.getBlockMap().values()) {
				if(b instanceof LogicBlock) {
					sinkWithCostMaps[0].put(b, new SinkWithCost(b, null, 0));
					sinkWithCostMaps[1].put(b, new SinkWithCost(b, null, 1));
					sinkWithCostMaps[2].put(b, new SinkWithCost(b, null, 2));
					sinkWithCostMaps[3].put(b, new SinkWithCost(b, null, 3));
				}
				else {
					sinkWithCostMaps[4].put(b, new SinkWithCost(b, null, findIOBlockInputPin(b)));
				}
			}
			
			
			if(w == -1) {//-w not set
				int upperBoundInitial= 16;
			
				int upperBound= upperBoundInitial;
				int lowerBound= 0;
				currentChannelWidth = upperBound;
				
				
				while(!globalRouter()) {
					//T ODO reactivate once algorithm converges
					lowerBound= upperBound;
					upperBound+= 8;
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
	
//					pFak= pFak<<1;
					
					
				}
				
				routingWriter.write(routingFilePath, finalRouting);
				
			}
			else {//-w set manually
				currentChannelWidth = w;
				if(globalRouter()) { //successfully routed
					for(Net n : currentRouting.keySet()) {
						finalRouting.put(n, currentRouting.get(n));
					}

					routingWriter.write(routingFilePath, finalRouting);
					
				}
				else {
					System.out.println("Set channel width [" +  w + "] is too small");
				}
				
				
			}
			
			long stopTime = System.currentTimeMillis();
		    long elapsedTime = stopTime - startTime;
		    System.out.println("Execution time (without File IO) in s: "+elapsedTime/1000);
		    System.out.println("Execution time (without File IO) in ms: "+elapsedTime);

			
			routingWriter.write(routingFilePath, finalRouting);

			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ErrorReporter.reportFileNotFoundError(e.toString());
		} catch (Exception e) {
			System.err.println("Execution aborted as a result of previously detected errors.");
			e.printStackTrace();
		}
	}
	

	
	private static int findIOBlockInputPin(NetlistBlock b) {
		if(b.getX() == 0) return 3; //left io, right pin
		else if(b.getY() == 0) return 2; //bottom io, top pin
		else if(b.getX() == parameterManager.X_GRID_SIZE + 1) return 1; //right io, left pin
		else return 0; //top io, bottom pin
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
		
		
		System.out.println("starting Global Router,                         currentChannelWidth: " + currentChannelWidth);
		
		//start setup
		
		if(channelIndex[0][0][0].length < currentChannelWidth) {
			ChannelWithCost[][][][] alreadyCreatedChannels= channelIndex;
			channelIndex= new ChannelWithCost[parameterManager.X_GRID_SIZE + 1][parameterManager.Y_GRID_SIZE + 1][2][currentChannelWidth];
			for(int j= 0; j < parameterManager.X_GRID_SIZE + 1; j++){
				for(int k= 0; k < parameterManager.Y_GRID_SIZE + 1; k++){
					for(int l= 0; l < 2; l++){
						for(int m= 0; m < alreadyCreatedChannels[0][0][0].length; m++){
							channelIndex[j][k][l][m]= alreadyCreatedChannels[j][k][l][m];
						}
						for(int m= alreadyCreatedChannels[0][0][0].length; m < currentChannelWidth; m++){
							channelIndex[j][k][l][m]= new ChannelWithCost(j, k, l == 1 ? true : false, m);
						}
					}
				}
			}
		}
		
		//create dummy routing for each net to be able to unroute each net before its first routing
		for(Net n : nets) {
			currentRouting.replace(n, nodeOfResourceDummy);
		}
	
		iterationCounter= 0 ; 
		boolean sharedResources= true;
		pFak= 0.5;
		
		//end setup
		
		//start loop
		
		while(sharedResources && iterationCounter < limit) {
			
			innerIterationCounter= 0;

			for( Net n : nets) { 
				currentRouting.get(n).decUsedCounters(); //unroute
				currentRouting.replace(n, signalRouter(n)); 
				innerIterationCounter++;
			} 

			
			//update hv
			for(NodeOfResource n : currentRouting.values()) {
				n.updateHistoryCongestions();
				//r.updateWith(pv) ; //TODO check
			}
			
			//check for resource limit violations
			sharedResources= sharedressources();
			
			//iterate counter and update pFak
			iterationCounter++ ;
			pFak *= 2;
			
		}
		
		//end loop
		
		for( Net n : nets) { //reset resource counters
			//unroute to decrease used counter before exiting
			currentRouting.get(n).decUsedCounters();
		}
		
		//get result
		
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
		
		for(NodeOfResource n : currentRouting.values()) { //in the RRG of each net...
			if(n.checkForResourceLimitViolations()) { //...recursively check for limit violations
				violated= true;
			}
		}
		
		return violated;
	}

	/**
	 * signal routing algorithm routing a single Net
	 * @param currentNet the Net to be routed
	 * @return 
	 */
	private static NodeOfResource signalRouter(Net currentNet) {

		
		//TODO remove
//		System.out.println("starting Signal Router, source: " + currentNet.getSource().toString() + "innerIterationCounter: " + innerIterationCounter);
		
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
		
		//initialize source: set cost of adjacent channels and add to pQ
		ChannelWithCost[] outputChannels= getOutputChannels(source);
		for(int j= 0; j < outputChannels.length; j++) {
			outputChannels[j].setPathCostAndPreviousIfNotYetComputedInThisIteration(sourceDummy, pFak, currentChannelWidth); //initialize cost as first channel of path
			pQ.add(outputChannels[j]); //add to priority queue
		}
		
		ResourceWithCost currentChannel;
		ChannelWithCost[] neighbouringChannels= new ChannelWithCost[6];
		
		for( NetlistBlock sink : currentNet.getSinks() ) { //loop over sinks


			/* route Verbindung zur Senke sink */
			
			
			//TODO remove
//			System.out.println("routing path to sink: " + sink.getName() + "@ (" + sink.getX() + "," + sink.getY() + ")" + " in Net: " + currentNet.getName() + ", size of pQ: " + pQ.size());
			
			if(routingTreeRoot != null) {
				routingTreeRoot.addAllToPriorityQueue(pQ);
			}
			//reenter all not yet used output channels to pQ (only those which have not been added / recomputed with the command above...)
			for(int j= 0; j < outputChannels.length; j++) {
				if(outputChannels[j].getCost() < 0) {
					outputChannels[j].setPathCostAndPreviousIfNotYetComputedInThisIteration(sourceDummy, pFak, currentChannelWidth); //initialize cost as first channel of path
					pQ.add(outputChannels[j]); //add to priority queue
				}
			}

			//get channels adjacent to sink
			ChannelWithCost[][] inputChannelsLogicBlock= null;
			ChannelWithCost[] inputChannelsIOBlock= null;
			
			if(sink instanceof LogicBlock) {
				inputChannelsLogicBlock= getInputChannelsLogicBlock(sink, pQ);
			}
			else {
				inputChannelsIOBlock= getInputChannelsIOBlock(sink, pQ);
			}
			//
			
			currentChannel = pQ.poll(); 
			polledResources.add(currentChannel);
			
			while(currentChannel instanceof ChannelWithCost) {
				
				getNeighbouringChannels(((ChannelWithCost) currentChannel), neighbouringChannels);
				
				for(int j= 0; j < neighbouringChannels.length; j++) {
					
					if(neighbouringChannels[j] == null) break;
					
					if(neighbouringChannels[j].getCost() < 0) { //no valid values for cost and previous present
						neighbouringChannels[j].setPathCostAndPreviousIfNotYetComputedInThisIteration(((ChannelWithCost) currentChannel), pFak, currentChannelWidth);
						pQ.add(neighbouringChannels[j]);
					}
					
				}
				
				
				
				
				if(sink instanceof LogicBlock) {
					int inputPin= containsChannelWithCost(inputChannelsLogicBlock[((ChannelWithCost) currentChannel).getTrackNum()], ((ChannelWithCost) currentChannel));
					if(inputPin >= 0) {
						
						SinkWithCost tmp= sinkWithCostMaps[inputPin].get(sink);
					
						tmp.setPathCostAndPreviousIfNotYetComputedInThisIteration(((ChannelWithCost) currentChannel), pFak, currentChannelWidth);
	//					System.err.println("cost of IPin: " + tmp.getCost());
						pQ.add(tmp);
					}
				}
				else {
					
					if(inputChannelsIOBlock[((ChannelWithCost) currentChannel).getTrackNum()].equals(currentChannel)) {
						
						SinkWithCost tmp= sinkWithCostMaps[4].get(sink);
					
						tmp.setPathCostAndPreviousIfNotYetComputedInThisIteration(((ChannelWithCost) currentChannel), pFak, currentChannelWidth);
	//					System.err.println("cost of IPin: " + tmp.getCost());
						pQ.add(tmp);
					}
				}

				
				
				
				currentChannel = pQ.poll(); 
				
				//remove
//				System.out.println("currentChannel: " + currentChannel.toString());
				
				polledResources.add(currentChannel);
			}

			
			
			
			//sink reached
			
			

			
			ResourceWithCost tmpChannel;
			
			//currentChannel is IPIN/Sink
			NodeOfResource branchingPoint;
			
			NodeOfResource currentBranch= new NodeOfResource(currentChannel); //create new branch
			currentChannel.incUsedCounter(); //set IPIN as used
			
			//TODO remove
//			System.err.println(currentChannelWidth + ": " + iterationCounter + ": used Pin: " + currentChannel.toString() + ", counter is: " + currentChannel.getUsedCounter(iterationCounter, globalIterationCounter));
			
//			System.err.println("IPin :" + currentChannel.toString() + ", cost: " + currentChannel.getCost() + ", currentCHannelWidth: " + currentChannelWidth + ", track: " + ((ChannelWithCost) currentChannel.getPrevious()).getTrackNum() + ", usedCounter: " + currentChannel.getUsedCounter(globalIterationCounter));
			
			
			//sink added to branch
			
			tmpChannel= currentChannel.getPrevious();
			branchingPoint= routingTreeRoot.findBranchingPoint(tmpChannel); //not null if tmpChannel is already part of routingTree (already used by different path of same net)
			currentBranch= new NodeOfResource(tmpChannel, currentBranch); //append tmpChannel to front of current branch
			//TODO remove
//			System.err.println(currentChannelWidth + ": " + iterationCounter + ": used channel: " + tmpChannel.toString() + ", counter is: " + tmpChannel.getUsedCounter(iterationCounter, globalIterationCounter));
			if(branchingPoint == null) { //branching point reached, tmpChannel holds branching point which is already used by this net, do not iterate usedCounter, while will be skipped
				tmpChannel.incUsedCounter();
			}
//			System.err.println("tmpChannel :" + tmpChannel.toString() + ", usedCounter: " + tmpChannel.getUsedCounter(globalIterationCounter));
			
			
			//first channel before sink added to branch
			
			tmpChannel= tmpChannel.getPrevious();
			
			while (branchingPoint == null){

				branchingPoint= routingTreeRoot.findBranchingPoint(tmpChannel); //not null if tmpChannel is already part of routingTree (already used by different path of same net)
				currentBranch= new NodeOfResource(tmpChannel, currentBranch); //append tmpChannel to front of current branch
				//TODO remove
//				System.err.println(currentChannelWidth + ": " + iterationCounter + ": used channel: " + tmpChannel.toString() + ", counter is: " + tmpChannel.getUsedCounter(iterationCounter, globalIterationCounter));
				if(branchingPoint == null) { //branching point reached, tmpChannel holds branching point which is already used by this net, do not iterate usedCounter, while will be exited
					tmpChannel.incUsedCounter();
				}
//				System.err.println("tmpChannel :" + tmpChannel.toString() + ", branchingPoint: " + branchingPoint);
				tmpChannel= tmpChannel.getPrevious();
				
			}
			
			
			//branch completed
			
			branchingPoint.addChild(currentBranch); //add branch to tree
			
			
			//branch added to tree

			for(ResourceWithCost r : polledResources) {
				r.resetCost();
			}
			polledResources.clear();
			for(ResourceWithCost r : pQ) {
				r.resetCost();
			}
			pQ.clear();
			
		}
		
		return routingTreeRoot;

	}

	/**
	 * saves neighbouring channels (up to 6) into neighbouringChannels with given current channel
	 * @param currentChannel
	 * @param neighbouringChannels
	 */
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


	/**
	 * returns an array with ChannelWithCost, where channels possible to be used for a given sink-block is stored. 
	 * This function also adds those channels to pQ
	 * @param sink
	 * @param pQ
	 * @return
	 */
	private static ChannelWithCost[] getInputChannelsIOBlock(NetlistBlock sink, PriorityQueue<ResourceWithCost> pQ) {
		
		int inputPin= sinkWithCostMaps[4].get(sink).getPin();
		ChannelWithCost[] outputChannels= new ChannelWithCost[currentChannelWidth];
		
		outputChannels= new ChannelWithCost[currentChannelWidth];
		if(inputPin > 1) { //left or bottom io
			if(inputPin == 3) { //left io, right pin
				for(int j= 0; j < currentChannelWidth; j++) {
					outputChannels[j]= channelIndex[sink.getX()][sink.getY()][0][j];
				}
			}
			else { //bottom io
				for(int j= 0; j < currentChannelWidth; j++) {
					outputChannels[j]= channelIndex[sink.getX()][sink.getY()][1][j];
				}
			}
		}
		else { //top or right io
			if(inputPin == 0) { //top io, bottom pin
				for(int j= 0; j < currentChannelWidth; j++) {
					outputChannels[j]= channelIndex[sink.getX()][sink.getY() - 1][1][j];
				}
			}
			else { //right io
				for(int j= 0; j < currentChannelWidth; j++) {
					outputChannels[j]= channelIndex[sink.getX() - 1][sink.getY()][0][j];
				}
			}
		}
		
		return outputChannels;
	}


	/**
	 * returns an array with ChannelWithCost, where channels possible to be used for a given sink-block is stored. 
	 * This function also adds those channels to pQ
	 * @param sink
	 * @param pQ
	 * @return
	 */
	private static ChannelWithCost[][] getInputChannelsLogicBlock(NetlistBlock sink, PriorityQueue<ResourceWithCost> pQ) {
		
		ChannelWithCost[][] outputChannels= new ChannelWithCost[currentChannelWidth][4];
		
		for(int j= 0; j < currentChannelWidth; j++) { //return all four surrounding channels
			outputChannels[j][2]= channelIndex[sink.getX()][sink.getY()][1][j]; //top
			outputChannels[j][3]= channelIndex[sink.getX()][sink.getY()][0][j]; //right
			outputChannels[j][0]= channelIndex[sink.getX()][sink.getY() - 1][1][j]; //bottom
			outputChannels[j][1]= channelIndex[sink.getX() - 1][sink.getY()][0][j]; //left
		}
		
		return outputChannels;
	}

	/**
	 * returns boolean: true if currentChannel is element of inputChannels
	 * @param inputChannels
	 * @param currentChannel
	 * @return
	 */
	private static int containsChannelWithCost(ChannelWithCost[] inputChannels, ChannelWithCost currentChannel) {
		for(int j= 0; j < inputChannels.length; j++) {
			if(currentChannel.equals(inputChannels[j])) return j;
		}
		return -1;
	}

	/**
	 * returns an array of ChannelWithCost with given source block
	 * @param source
	 * @return
	 */
	private static ChannelWithCost[] getOutputChannels(NetlistBlock source) {
		
		ChannelWithCost[] outputChannels;
		
		if(source instanceof IOBlock) { //IO Block
			
			int inputPin= sinkWithCostMaps[4].get(source).getPin();
			
			outputChannels= new ChannelWithCost[currentChannelWidth];
			if(inputPin > 1) { //left or bottom io
				if(inputPin == 3) { //left io, right pin
					for(int j= 0; j < currentChannelWidth; j++) {
						outputChannels[j]= channelIndex[source.getX()][source.getY()][0][j];
					}
				}
				else { //bottom io
					for(int j= 0; j < currentChannelWidth; j++) {
						outputChannels[j]= channelIndex[source.getX()][source.getY()][1][j];
					}
				}
			}
			else { //right or top io
				if(inputPin == 0) { //top io, bottom pin
					for(int j= 0; j < currentChannelWidth; j++) {
						outputChannels[j]= channelIndex[source.getX()][source.getY() - 1][1][j];
					}
				}
				else { //right io
					for(int j= 0; j < currentChannelWidth; j++) {
						outputChannels[j]= channelIndex[source.getX() - 1][source.getY()][0][j];
					}
				}
			}
			
		}
		else { //logic Block
			
			outputChannels= new ChannelWithCost[2 * currentChannelWidth];
			for(int j= 0; j < currentChannelWidth; j++) {
				outputChannels[j]= channelIndex[source.getX()][source.getY()][0][j];
				outputChannels[j + currentChannelWidth]= channelIndex[source.getX()][source.getY() - 1][1][j];
			}
			
		}
		
		return outputChannels;
	}

}
