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

	private static Collection<ChannelWithCost> usedChannels;

	private static Collection<BlockPinCost> usedSinkPins;

	private static int currentChannelWidth;

	private static Collection<NodeOfResource> currentRouting;

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
		iterationCounter= -1 ; 
		while(sharedressources() && iterationCounter < limit) {
			iterationCounter++ ;
			currentRouting.clear();
			usedChannels.clear();
			usedSinkPins.clear();
			for( Net n : nets) { 
				currentRouting.add(signalRouter(n,iterationCounter)); 
			} 
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
		for(ChannelWithCost c : usedChannels) {
			if(c.getUsedCounter() > currentChannelWidth) return true;
		}
		for(BlockPinCost p : usedSinkPins) {
			if(p.limitExceeded(iterationCounter)) return true;
		}
		return false; //no usage limit exceeded
	}

	/**
	 * signal routing algorithm routing a single Net
	 * @param currentNet the Net to be routed
	 * @return 
	 */
	private static NodeOfResource signalRouter(Net currentNet, int globalIterationCounter) {
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
		//resetResourceCosts(); //DONE self-invalidating cost storage: e.g. save iteration counter when writing and check if counter == current when reading, else invalid
		
		initializeSourceCosts(source, pQ);
		
		ChannelWithCost currentChannel;
		ChannelWithCost[] neighbouringChannels= new ChannelWithCost[6];
		
		double pFak = 0.5 * Math.pow(2 ,globalIterationCounter);
		
		for( NetlistBlock sink : currentNet.getSinks() ) {
			/* route Verbindung zur Senke sink */
			BlockPinCost sinkCosts= blockPinCosts.get(sink);
			
			pQ.clear() ; 

			ChannelWithCost[] inputChannels= getInputChannels(sink);
			BlockPinCost sinkPins= blockPinCosts.get(sink);
			initializeSinkCosts(sink, inputChannels, sinkPins);
			
			routingTreeRoot.addAllChannelsToPriorityQueue(pQ);
			currentChannel = pQ.poll(); 
			currentChannel.setUsed(iterationCounter);
			while(!containsChannelWithCost(inputChannels, currentChannel)) {
				
				getNeighbouringChannels(currentChannel, neighbouringChannels);
				
				for(int j= 0; j < neighbouringChannels.length; j++) {
					
					if(neighbouringChannels[j] == null) break;
					
//					if(neighbouringChannels[j].notYetComputed(iterationCounter)) { 
//						
//						neighbouringChannels[j].setPathCostAndPrevious(currentChannel, iterationCounter);
//						pQ.add(neighbouringChannels[j]);
//					
//					}
					
					//saves one method call...
					neighbouringChannels[j].setPathCostAndPreviousIfNotYetComputedInThisIteration(currentChannel, iterationCounter);
					pQ.add(neighbouringChannels[j]);
					
				}
				
				currentChannel = pQ.poll(); 
				
			}

			resetSinkCosts(inputChannels);

			
			ChannelWithCost tmpChannel;
			
			NodeOfResource currentBranch= new NodeOfResource(new SinkWithCost(sink, 0, currentChannel));
			NodeOfResource branchingPoint= routingTreeRoot.findBranchingPoint(currentChannel);
			
			while (branchingPoint == null){
				tmpChannel= currentChannel.getPrevious();
				currentBranch= new NodeOfResource(currentChannel, currentBranch);

				currentChannel.setUsed(iterationCounter);
				usedChannels.add(currentChannel);
				
				currentChannel= tmpChannel;
				branchingPoint= routingTreeRoot.findBranchingPoint(currentChannel); //tmp = current at this point
			}
			branchingPoint.addChild(currentBranch);
			
			usedSinkPins.add(sinkPins);
		}
		
		return routingTreeRoot;

	}



	private static void getNeighbouringChannels(ChannelWithCost currentChannel, ChannelWithCost[] neighbouringChannels) {
		int channelsAdded= 0;
		if(currentChannel.getHorizontal()) { //is a horizontal channel
			//no horizontal channels at x = 0
			//if(currentChannel.getX() > 0) { //add left neighbor
				neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX() - 1][currentChannel.getY()][1];
				channelsAdded++;
			//}
			if(currentChannel.getX() < parameterManager.X_GRID_SIZE + 1) { //add right neighbor
				neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX() + 1][currentChannel.getY()][1];
				channelsAdded++;
			}
			if(currentChannel.getY() > 0) { //add bottom neighbors
				neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX()][currentChannel.getY()][0];
				channelsAdded++;
				//no horizontal channels at x = 0
				//if(currentChannel.getX() > 0) {
					neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX() - 1][currentChannel.getY()][0];
					channelsAdded++;
				//}
			}
			if(currentChannel.getY() < parameterManager.Y_GRID_SIZE + 1) { //add top neighbors
				neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX()][currentChannel.getY() + 1][0];
				channelsAdded++;
				//no horizontal channels at x = 0
				//if(currentChannel.getX() > 0) {
					neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX() - 1][currentChannel.getY() + 1][0];
					channelsAdded++;
				//}
			}
		}
		else { //is a vertical channel
			//no vertical channels at y = 0
			//if(currentChannel.getY() > 0) {  //add bottom neighbor
				neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX()][currentChannel.getY() - 1][0];
				channelsAdded++;
			//}
			if(currentChannel.getY() < parameterManager.Y_GRID_SIZE + 1) {  //add top neighbor
				neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX()][currentChannel.getY() + 1][0];
				channelsAdded++;
			}
			if(currentChannel.getX() > 0) {  //add left neighbors
				neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX()][currentChannel.getY()][1];
				channelsAdded++;
				//no vertical channels at y = 0
				//if(currentChannel.getX() > 0) {
					neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX()][currentChannel.getY() - 1][1];
					channelsAdded++;
				//}
			}
			if(currentChannel.getX() < parameterManager.X_GRID_SIZE + 1) {  //add right neighbors
				neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX() + 1][currentChannel.getY()][1];
				channelsAdded++;
				//no vertical channels at y = 0
				//if(currentChannel.getX() > 0) {
					neighbouringChannels[channelsAdded]= channelIndex[currentChannel.getX() + 1][currentChannel.getY() - 1][1];
					channelsAdded++;
				//}
			}
			
		}
	}



	private static ChannelWithCost[] getInputChannels(NetlistBlock sink) {
		BlockPinCost tmp= blockPinCosts.get(sink);
		if(tmp instanceof IOBlockPinCost) { //IO Block
			if(((IOBlockPinCost) tmp).getLeftOrRight()) { //left ot top io
				if(((IOBlockPinCost) tmp).getLeftOrTop()) { //left io
					return new ChannelWithCost[]{channelIndex[sink.getX()][sink.getY()][0]};
				}
				else { //top io
					return new ChannelWithCost[]{channelIndex[sink.getX()][sink.getY() - 1][1]};
				}
			}
			else { //right or bottom io
				if(((IOBlockPinCost) tmp).getLeftOrTop()) { //right io
					return new ChannelWithCost[]{channelIndex[sink.getX() - 1][sink.getY()][0]};
				}
				else { //bottom io
					return new ChannelWithCost[]{channelIndex[sink.getX()][sink.getY()][1]};
				}
			}
		}
		else { //logic Block
			return new ChannelWithCost[]{ //return all four surrounding channels
					channelIndex[sink.getX()][sink.getY()][1],
					channelIndex[sink.getX()][sink.getY()][0],
					channelIndex[sink.getX()][sink.getY() - 1][1],
					channelIndex[sink.getX() - 1][sink.getY()][0]
			};
		}
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

	private static void initializeSourceCosts(NetlistBlock source, PriorityQueue<ChannelWithCost> pQ) {
		ChannelWithCost[] outputChannels= getOutputChannels(source);
		for(int j= 0; j < outputChannels.length; j++) {
			outputChannels[j].setPathCostAndPreviousIfNotYetComputedInThisIteration(null, iterationCounter); //initialize cost as first channel of path
			pQ.add(outputChannels[j]); //add to priority queue
		}
	}

	private static void resetSinkCosts(ChannelWithCost[] inputChannels) {
		for(int j= 0; j < inputChannels.length; j++) {
			inputChannels[j].resetLastChannel();
		}
	}

	private static ChannelWithCost[] getOutputChannels(NetlistBlock source) {
		BlockPinCost tmp= blockPinCosts.get(source);
		if(tmp instanceof IOBlockPinCost) { //IO Block
			if(((IOBlockPinCost) tmp).getLeftOrRight()) { //left ot top io
				if(((IOBlockPinCost) tmp).getLeftOrTop()) { //left io
					return new ChannelWithCost[]{channelIndex[source.getX()][source.getY()][0]};
				}
				else { //top io
					return new ChannelWithCost[]{channelIndex[source.getX()][source.getY() - 1][1]};
				}
			}
			else { //right or bottom io
				if(((IOBlockPinCost) tmp).getLeftOrTop()) { //right io
					return new ChannelWithCost[]{channelIndex[source.getX() - 1][source.getY()][0]};
				}
				else { //bottom io
					return new ChannelWithCost[]{channelIndex[source.getX()][source.getY()][1]};
				}
			}
		}
		else { //logic Block
			return new ChannelWithCost[]{ //return right and bottom channels
					channelIndex[source.getX()][source.getY()][0],
					channelIndex[source.getX()][source.getY() - 1][1]
			};
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
