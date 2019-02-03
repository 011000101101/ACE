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
import router.structures.tree.NodeOfResource;
import router.structures.blockPinCost.BlockPinCost;
import router.structures.blockPinCost.IOBlockPinCost;
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
	private static RoutingWriter routingWriter; //TODO
	
	/**
	 * iteration limit for globalRouter
	 */
	private static int limit;
	
	/**
	 * collection holding all nets to be routed
	 */
	private static Collection<Net> nets;
	
	private static ParameterManager parameterManager;
	private static StructureManager structureManager;
	
	
	private static int[][][] channelUsedCount;
	private static double[][][] channelCostIndex;
	private static boolean[][][] channelCostNotYetComputedFlagIndex;
	
	private static ChannelWithCost[][][] channelIndex;
	
	private static Map<NetlistBlock, BlockPinCost> blockPinCosts;

	/**
	 * counter for iterations of the signal router during one pass of the global router
	 */
	private static int iterationCounter;

	public static void main(String[] args) {
		
		
		// TODO input parsing and basic datastructure initialization
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
			
			routingWriter= new RoutingWriter();
			
		
			structureManager= StructureManager.getInstance();
			parameterManager= ParameterManager.getInstance();			
			
			channelUsedCount= new int[parameterManager.X_GRID_SIZE + 1][parameterManager.Y_GRID_SIZE + 1][2];
			channelCostIndex= new double[parameterManager.X_GRID_SIZE + 1][parameterManager.Y_GRID_SIZE + 1][2];
			channelCostNotYetComputedFlagIndex= new boolean[parameterManager.X_GRID_SIZE + 1][parameterManager.Y_GRID_SIZE + 1][2];
			
			channelIndex= new ChannelWithCost[parameterManager.X_GRID_SIZE + 1][parameterManager.Y_GRID_SIZE + 1][2];
			
			blockPinCosts= new HashMap<NetlistBlock, BlockPinCost>(structureManager.getBlockMap().values().size()); //TODO check hashmap initialization
			
			
			//TODO binary search for minimal channel width, set global variable channelWidth to new value and execute globalRouter, if returns false -> vergrößere channelWidth, else verkleinere, bis wert eindeutig festgelegt (upper and lower bound lokal speichern und in jeder iteration aufeinander zu bewegen...)
			//parameterManager.setChannelWidth(...); //set new channel width before execution of global router
			globalRouter();

			
			routingWriter.write(routingFilePath);

			
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
		iterationCounter= 0 ; 
		while(sharedressources() && iterationCounter < limit) {
			for( Net n : nets) { 
				signalRouter(n,iterationCounter) ; 
			}
			iterationCounter++ ; 
			//foreach r in RRG.Edges do TODO implement this
				//r.updateHistory() ;
				//r.updateWith(pv) ;
		}
		if (iterationCounter > limit) {
			return false ;
		}
		else {
			return true; //results of routing stored in global variable
		}
	}

	/**
	 * checks if the current routing complies to limits on shared resources or violates at least one of them
	 * @return true if a limit has been violated, false if the routing is valid
	 */
	private static boolean sharedressources() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * signal routing algorithm routing a single Net
	 * @param currentNet the Net to be routed
	 */
	private static void signalRouter(Net currentNet, int globalIterationCounter) {
		//Tree<Point> signalrouter(Net n) begin 
		NodeOfResource routingTreeRoot ; 
		//RtgRsrc i, j, w, v := nil ; (just coordinates, no seperate objects)
		PriorityQueue<ChannelWithCost> pQ= new PriorityQueue<ChannelWithCost>(
			parameterManager.X_GRID_SIZE * 2 + parameterManager.Y_GRID_SIZE * 2, 
			new Comparator<ChannelWithCost>() {

				@Override
				public int compare(ChannelWithCost point1, ChannelWithCost point2) {
					if(point1.getCost() < point2.getCost()) return -1;
					if(point2.getCost() < point1.getCost()) return 1;
					return 0;
				}
			
			}
		); 
		//HashMap<RtgRsrc,int> PathCost ; (reuse global cost array(s))
		NetlistBlock source= currentNet.getSource(); 
		//RT.add(i, ()) ; 
		//resetResourceCosts(); //TODO self-invalidating cost storage: e.g. save iteration counter when writing and check if counter == current when reading, else invalid
		
		initializeSourceCosts(source, pQ);
		
		ChannelWithCost currentChannel;
		ChannelWithCost[] neighbouringChannels;
		
		double pFak = 0.5 * Math.pow(2 ,globalIterationCounter);
		
		for( NetlistBlock sink : currentNet.getSinks() ) {
			/* route Verbindung zur Senke sink */
			BlockPinCost sinkCosts= blockPinCosts.get(sink);
			
			pQ.clear() ; 

			ChannelWithCost[] inputChannels= getInputChannels(sink);
			initializeSinkCosts(sink, inputChannels);
			
			routingTreeRoot.addAllChannelsToPriorityQueue(pQ);
			currentChannel = pQ.poll(); 
			currentChannel.setUsed(iterationCounter);
			while(!containsChannelWithCost(inputChannels, currentChannel)) {
				
				neighbouringChannels= getNeighbouringChannels(currentChannel);
				
				for(int j= 0; j < neighbouringChannels.length; j++) {
					
					if(neighbouringChannels[j].notYetComputed(iterationCounter)) { 
						
						neighbouringChannels[j].setPathCostAndPrevious(currentChannel, iterationCounter);
						pQ.add(neighbouringChannels[j]);
					
					}
					
				}
				
				currentChannel = pQ.poll(); 
				currentChannel.setUsed(iterationCounter);
				
			}

			//TODO reach sink (add to tree etc)
			
			ChannelWithCost tmpChannel;
			
			NodeOfResource currentBranch;
			NodeOfResource branchingPoint= routingTreeRoot.findBranchingPoint(currentChannel);
			
			while (branchingPoint == null){
				tmpChannel= currentChannel.getPrevious();
				currentBranch= new NodeOfResource(currentChannel, currentBranch);
				
				currentChannel.updateCost() ; 
				
				currentChannel= tmpChannel;
				branchingPoint= routingTreeRoot.findBranchingPoint(currentChannel); //tmp = current at this point
			}
			branchingPoint.addChild(currentBranch);
		}
		
		return (RT) ; //TODO return only relevant information

	}

	private static ChannelWithCost[] getInputChannels(NetlistBlock sink) {
		BlockPinCost tmp= blockPinCosts.get(sink);
		if(tmp instanceof IOBlockPinCost) {
			if(((IOBlockPinCost) tmp).getLeftOrRight()) { //left ot top io
				if(((IOBlockPinCost) tmp).getLeftOrTop()) { //left io
					return new ChannelWithCost[]{channelIndex[sink.getX()][sink.getY()][0]};
				}
				else { //top io
					return new ChannelWithCost[]{channelIndex[sink.getX()][sink.getY() - 1][1]};
				}
			}
			else { //
				if(((IOBlockPinCost) tmp).getLeftOrTop()) {
					return new ChannelWithCost[]{channelIndex[sink.getX()][sink.getY()][0]};
				}
				else {
					return new ChannelWithCost[]{channelIndex[sink.getX()][sink.getY()][1]};
				}
			}
		}
		return null;
	}

	private static boolean containsChannelWithCost(ChannelWithCost[] inputChannels, ChannelWithCost currentChannel) {
		for(int j= 0; j < inputChannels.length; j++) {
			if(currentChannel.equals(inputChannels[j])) return true;
		}
		return false;
	}



	private static void initializeSinkCosts(NetlistBlock sink, ChannelWithCost[] inputChannels) {
		BlockPinCost sinkPins= blockPinCosts.get(sink);
		for(int j= 0; j < inputChannels.length; j++) {
			inputChannels[j].setLastChannel(sinkPins, iterationCounter);
		}
	}

	private static void initializeSourceCosts(NetlistBlock source, PriorityQueue<ChannelWithCost> pQ) {
		ChannelWithCost[] outputChannels= getOutputChannels(source);
		for(int j= 0; j < outputChannels.length; j++) {
			outputChannels[j].setPathCostAndPrevious(null, iterationCounter); //initialize cost as first channel of path
			pQ.add(outputChannels[j]); //add to priority queue
		}
	}

	/**
	 * updates the resource use counter to include currentChannel
	 * @param currentChannel the newly selected Channel
	 */
	private static void setChannelUsed(ChannelWithCost currentChannel) {
		channelUsedCount[currentChannel.getX()][currentChannel.getY()][currentChannel.getHorizontal() ? 1 : 0]= channelUsedCount[currentChannel.getX()][currentChannel.getY()][currentChannel.getHorizontal() ? 1 : 0] + 1;
	}

}
