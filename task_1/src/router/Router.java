package router;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.awt.Point;

import designAnalyzer.ParameterManager;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.StructureManager;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import router.structures.resourceWithCost.ChannelWithCost;
import router.structures.resourceWithCost.ResourceWithCost;
import router.structures.tree.NodeOfResource;
import router.structures.blockPinCost.BlockPinCost;

public class Router {

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
	
	private static Map<NetlistBlock, BlockPinCost> blockPinCosts;

	public static void main(String[] args) {
		
		
		// TODO input parsing and basic datastructure initialization
		
		
		parameterManager= ParameterManager.getInstance();
		structureManager= StructureManager.getInstance();
		channelUsedCount= new int[parameterManager.X_GRID_SIZE + 1][parameterManager.Y_GRID_SIZE + 1][2];
		channelCostIndex= new double[parameterManager.X_GRID_SIZE + 1][parameterManager.Y_GRID_SIZE + 1][2];
		channelCostNotYetComputedFlagIndex= new boolean[parameterManager.X_GRID_SIZE + 1][parameterManager.Y_GRID_SIZE + 1][2];
		blockPinCosts= new HashMap<NetlistBlock, BlockPinCost>(structureManager.getBlockMap().values().size()); //TODO check hashmap initialization
		
		
		//TODO binary search for minimal channel width, set global variable channelWidth to new value and execute globalRouter, if returns false -> vergrößere channelWidth, else verkleinere, bis wert eindeutig festgelegt (upper and lower bound lokal speichern und in jeder iteration aufeinander zu bewegen...)
		globalRouter();

	}
	
	/**
	 * global routing algorithm routing all nets repeatedly for up to [limit] number of times or until the placement has been routed validly
	 */
	private static boolean globalRouter() {
		int count= 0 ; 
		while(sharedressources() && count < limit) {
			for( Net n : nets) { 
				signalRouter(n) ; 
				count++ ; 
			}
		}
		if (count > limit) {
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
	private static void signalRouter(Net currentNet) {
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
		resetResourceCosts(); //TODO self-invalidating cost storage: e.g. save iteration counter when writing and check if counter == current when reading, else invalid
		
		initializeSourceCosts(source);
		
		ChannelWithCost currentChannel;
		int[] neighbouringChannelsX= new int[6];
		int[] neighbouringChannelsY= new int[6];
		boolean[] neighbouringChannelsHorizontal= new boolean[6];
		
		for( NetlistBlock sink : currentNet.getSinks() ) {
			/* route Verbindung zur Senke sink */
			BlockPinCost sinkCosts= blockPinCosts.get(sink);
			
			pQ.clear() ; 
			
			initializeSinkCosts(sink);
			
			routingTreeRoot.addAllChannelsToPriorityQueue(pQ);
			currentChannel = pQ.poll(); 
			setChannelUsed(currentChannel);
			while(!sinkReached(sink, sinkCosts, currentChannel.getX(), currentChannel.getY(), currentChannel.getHorizontal())) {
				
				getNeighbouringChannels(currentChannel, neighbouringChannelsX, neighbouringChannelsY, neighbouringChannelsHorizontal);
				
				for(int j= 0; j < 6; j++) {
					
					if(neighbouringChannelsX[j] == -1) break;
					
					if(channelCostNotYetComputedFlagIndex[neighbouringChannelsX[j]][neighbouringChannelsY[j]][neighbouringChannelsHorizontal[j] ? 1 : 0]) { 
						
						channelCostIndex[neighbouringChannelsX[j]][neighbouringChannelsY[j]][neighbouringChannelsHorizontal[j] ? 1 : 0]= channelCostIndex[currentChannel.getX()][currentChannel.getY()][currentChannel.getHorizontal() ? 1 : 0] + computeCost(neighbouringChannelsX[j], neighbouringChannelsY[j], neighbouringChannelsHorizontal[j]); 
						pQ.add(new ChannelWithCost(neighbouringChannelsX[j], neighbouringChannelsY[j], neighbouringChannelsHorizontal[j], channelCostIndex[neighbouringChannelsX[j]][neighbouringChannelsY[j]][neighbouringChannelsHorizontal[j] ? 1 : 0], currentChannel));
					
					}
					
				}
				
				currentChannel = pQ.poll(); 
				setChannelUsed(currentChannel);
				
			}

			//TODO reach sink (add to tree etc)
			
			ChannelWithCost tmpChannel;
			
			NodeOfResource currentBranch;
			NodeOfResource branchingPoint= routingTreeRoot.findBranchingPoint(currentChannel);
			
			while (branchingPoint == null){
				//TODO
				/*if(currentChannel.getHorizontal()) {
					tmpX= currentChannel.getX();
					tmpY= currentChannel.getY();
					tmpHorizontal= false;
					tmpTmpCost
					
				}
				else {
					
				}*/
				tmpChannel= currentChannel.getPrevious();
				//tmpX etc now hold the cheapest Neighbour
				currentBranch= new NodeOfResource(currentChannel, currentBranch);
				
				updateCost(currentChannel) ; 
				
				currentChannel= tmpChannel;
				branchingPoint= routingTreeRoot.findBranchingPoint(currentChannel); //tmp = current at this point
			}
			branchingPoint.addChild(currentBranch);
		}
		
		return (RT) ; //TODO return only relevant information

	}

	private static void initializeSinkCosts(NetlistBlock sink) {
		// TODO Auto-generated method stub
		
	}

	private static void initializeSourceCosts(NetlistBlock source) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * updates the resource use counter to include currentChannel
	 * @param currentChannel the newly selected Channel
	 */
	private static void setChannelUsed(ChannelWithCost currentChannel) {
		channelUsedCount[currentChannel.getX()][currentChannel.getY()][currentChannel.getHorizontal() ? 1 : 0]= channelUsedCount[currentChannel.getX()][currentChannel.getY()][currentChannel.getHorizontal() ? 1 : 0] + 1;
	}

}
