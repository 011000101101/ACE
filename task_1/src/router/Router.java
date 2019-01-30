package router;

import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.awt.Point;

import designAnalyzer.ParameterManager;
import designAnalyzer.structures.Net;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import router.structures.tree.NodeOfChannel;
import router.structures.ChannelWithCost;

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
	
	
	private static int[][][] channelUsedCount;
	private static int[][][] channelCostIndex;
	private static boolean[][][] channelCostNotYetComputedFlagIndex;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		parameterManager= ParameterManager.getInstance();
		channelUsedCount= new int[parameterManager.X_GRID_SIZE + 1][parameterManager.Y_GRID_SIZE + 1][2];
		channelCostIndex= new int[parameterManager.X_GRID_SIZE + 1][parameterManager.Y_GRID_SIZE + 1][2];
		channelCostNotYetComputedFlagIndex= new boolean[parameterManager.X_GRID_SIZE + 1][parameterManager.Y_GRID_SIZE + 1][2];
		
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
		NodeOfChannel routingTreeRoot ; 
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
		resetResourceCosts();
		initializeSourceCosts(source);
		
		ChannelWithCost currentChannel;
		int[] neighbouringChannelsX= new int[6];
		int[] neighbouringChannelsY= new int[6];
		boolean[] neighbouringChannelsHorizontal= new boolean[6];
		
		for( NetlistBlock sink : currentNet.getSinks() ) {
			/* route Verbindung zur Senke sink */ 
			pQ.clear() ; 
			
			initializeSinkCosts(sink);
			
			routingTreeRoot.addAllToPriorityQueue(pQ);
			currentChannel = pQ.poll(); 
			while(!sinkReached(sink, currentChannel.getX(), currentChannel.getY(), currentChannel.getHorizontal())) {
				getNeighbouringChannels(currentChannel, neighbouringChannelsX, neighbouringChannelsY, neighbouringChannelsHorizontal);
				for(int j= 0; j < 6; j++) {
					if(neighbouringChannelsX[j] == -1) break;
					if(channelCostNotYetComputedFlagIndex[neighbouringChannelsX[j]][neighbouringChannelsY[j]][neighbouringChannelsHorizontal[j] ? 1 : 0]) { 
						channelCostIndex[neighbouringChannelsX[j]][neighbouringChannelsY[j]][neighbouringChannelsHorizontal[j] ? 1 : 0]= channelCostIndex[currentChannel.getX()][currentChannel.getY()][currentChannel.getHorizontal() ? 1 : 0] + computeCost(neighbouringChannelsX[j], neighbouringChannelsY[j], neighbouringChannelsHorizontal[j]); 
						pQ.add(new ChannelWithCost(neighbouringChannelsX[j], neighbouringChannelsY[j], neighbouringChannelsHorizontal[j], channelCostIndex[neighbouringChannelsX[j]][neighbouringChannelsY[j]][neighbouringChannelsHorizontal[j] ? 1 : 0]));
					}
				}
				currentChannel = pQ.poll(); 
			}

			int tmpX;
			int tmpY;
			boolean tmpHorizontal;
			int tmpTmpX;
			int tmpTmpY;
			boolean tmpTmpHorizontal;
			double tmpTmpCost;
			
			NodeOfChannel currentBranch;
			NodeOfChannel branchingPoint= routingTreeRoot.findBranchingPoint(currentChannel.getX(), currentChannel.getY(), currentChannel.getHorizontal());
			
			while (routingTreeRoot.findBranchingPoint(tmpX, tmpY, tmpHorizontal) != null){
				//TODO
				/*if(currentChannel.getHorizontal()) {
					tmpX= currentChannel.getX();
					tmpY= currentChannel.getY();
					tmpHorizontal= false;
					tmpTmpCost
					
				}
				else {
					
				}*/
				//tmpX etc now hold the cheapest Neighbour
				currentBranch= new NodeOfChannel(currentChannel.getX(), currentChannel.getY(), currentChannel.getHorizontal(), currentBranch);
				updateCost(currentChannel) ; 
				currentChannel.setX(tmpX);
				currentChannel.setY(tmpY);
				currentChannel.setHorizontal(tmpHorizontal);
				branchingPoint= routingTreeRoot.findBranchingPoint(tmpX, tmpY, tmpHorizontal); //tmp = current at this point
			}
			branchingPoint.addChild(currentBranch);
		}
		return (RT) ; //TODO return only relevant information

	}

}
