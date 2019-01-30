package router;

import java.util.Collection;

import designAnalyzer.structures.Net;

public class Router {

	/**
	 * iteration limit for globalRouter
	 */
	private static int limit;
	
	/**
	 * collection holding all nets to be routed
	 */
	private static Collection<Net> nets;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		globalRouter();

	}
	
	/**
	 * global routing algorithm routing all nets repeatedly for up to [limit] number of times or until the placement has been routed validly
	 */
	private static void globalRouter() {
		int count= 0 ; 
		while(sharedressources() && count < limit) {
			for( Net n : nets) { 
				signalRouter(n) ; 
				count++ ; 
			}
		}
		if (count > limit) {
			//return “unrouteable” ; //TODO 
		}
		else {
			//return results //TODO
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
		Tree<RtgRsrc> signalrouter(Net n) begin 
		Tree<RtgRsrc> RT ; 
		RtgRsrc i, j, w, v := nil ; 
		PriorityQueue<int,RtgRsrc> PQ ; 
		HashMap<RtgRsrc,int> PathCost ; 
		i := n.source() ; 
		RT.add(i, ()) ; 
		PathCost[*] :=∞; 
		PathCost[i] := 0 ; 
		foreach SinkTerminal j in n.sinks() do 
			/* route Verbindung zur Senke j */ 
			PQ.clear() ; 
			foreach v in RT.nodes() do 
				PQ.add(0, v) ; 
			repeat 
				v := PQ.removeLowestCostNode() ; 
				if v6= j then 
						foreach w in v.neighbors() do 
							if PathCost[w] > (PathCost[v] + w.cost()) then 
								PathCost[w] := PathCost[v] + w.cost() ; 
								PQ. add(PathCost[w], w) ;
			until v = j; 
			while !(v in RT.nodes()) do 
				w := v.ﬁndCheapestNeighbor(PathCost) ; 
				RT.add(v,(w,v)) ; 
				v.updateCost() ; 
				v := w ;

		return (RT) ;

	}

}
