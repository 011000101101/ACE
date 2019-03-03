package designAnalyzer.structures;

import designAnalyzer.ParameterManager;
import designAnalyzer.structures.pathElements.blocks.IOBlock;
import designAnalyzer.structures.pathElements.blocks.LogicBlock;
import designAnalyzer.structures.pathElements.blocks.NetlistBlock;
import designAnalyzer.timingAnalyzer.TimingAnalyzer;

/**
 * 
 * @author Dennis Grotz, Rumei Ma, Vincenz Mechler
 *
 */
public class SimplePath {

	TimingAnalyzer timingAnalyzer;
	ParameterManager parameterManager;
	
	private int delayExact; //tA always 0, as simplePath originated at an external input or the output of a flipflop
	private int tR;
	private int slack;
	private double criticality;
	
	/**
	 * current timing cost of this path (essentially a cache)<br>
	 * -1 if invalid
	 */
	private double cost= -1;
	
	/**
	 * last value of cost to be able to reset the buffer if swap is not performed<br>
	 * -1 if invalid
	 */
	private double oldCost= -1;
	
	/**
	 * flag to avoid duplicates when updating the timing cost (a simplePath might be part of both swapped blocks...)
	 */
	private boolean updated= false;

	private NetlistBlock source;
	private NetlistBlock sink;
	
	/**
	 * blocks between source and sink, null if path doesnt contain combinatorial blocks
	 */
	private NetlistBlock[] intermediate= null;
	
	/**
	 * net the sink is connected to
	 */
	private Net sinkingNet;
	
	/**
	 * shows if one of the connected blocks has been swapped after the last execution of timingCostSwap()
	 */
	private boolean unchanged;
	
	public SimplePath(NetlistBlock newSource, NetlistBlock newSink, Net newSinkingNet) {
		source= newSource;
		sink= newSink;
		sinkingNet= newSinkingNet;
		parameterManager= ParameterManager.getInstance();
		timingAnalyzer= TimingAnalyzer.getInstance();
	}
	
	public SimplePath(NetlistBlock newSource, NetlistBlock newSink, NetlistBlock[] newIntermediate, Net newSinkingNet) {
		intermediate= newIntermediate;
		source= newSource;
		sink= newSink;
		sinkingNet= newSinkingNet;
		parameterManager= ParameterManager.getInstance();
		timingAnalyzer= TimingAnalyzer.getInstance();
	}

	/**
	 * update tR, if the new value is greater than the old one
	 * @param newTR new tR value
	 */
	public void updateTR(int newTR) {
		if(newTR > tR) {
			tR= newTR;
		}
	}
	
	/**
	 * computes the slack of this path, executed after all simplePaths have executed their computeDelay() (and therefore tR and delay are known)
	 * @param dMax 
	 */
	public void computeSlack(int dMax) {
		slack= tR - delayExact;
		criticality= 1 - ((double) slack / (double) dMax);
		tR= -1; //reset tR for next cycle
		//structureManager.updateSlackMap(source, sink, slack);
	}
	
	/**
	 * estimates the delay of this simplePath by computing the minimum delay possible from source to sink through all intermediate combinatorial blocks
	 * @return 
	 */
	public int computeDelay() {
		
		if(intermediate == null) { //only one net
			delayExact= timingAnalyzer.estimateSinglePathNoEndpoints(source.getX(), source.getY(), sink.getX(), sink.getY());
		}
		else { //many nets
			delayExact= timingAnalyzer.estimateSinglePathNoEndpoints(source.getX(), source.getY(), intermediate[0].getX(), intermediate[0].getY()) + parameterManager.T_COMB;
			for(int i= 1; i < intermediate.length; i++) {
				delayExact+= timingAnalyzer.estimateSinglePathNoEndpoints(intermediate[i-1].getX(), intermediate[i-1].getY(), intermediate[i].getX(), intermediate[i].getY()) + parameterManager.T_COMB;
			}
			delayExact+= timingAnalyzer.estimateSinglePathNoEndpoints(intermediate[intermediate.length - 1].getX(), intermediate[intermediate.length - 1].getY(), sink.getX(), sink.getY());
		}
		
		//add in and out delay
		if(source instanceof IOBlock) {
			delayExact+= parameterManager.T_IPAD;
		}
		else {
			delayExact+= parameterManager.T_FFOUT;
		}
		if(sink instanceof IOBlock) {
			delayExact+= parameterManager.T_OPAD;
		}
		else {
			delayExact+= parameterManager.T_FFIN;
		}
		
		//propagate computed delay as possible tR
		sinkingNet.updateTRAllSinks(delayExact);
		
		return delayExact;
	}
	
	/**
	 * returns the slack of this simplePath
	 * @return the slack of this simplePath
	 */
	public int getSlack() {
		return slack;
	}

	/**
	 * computes the timing cost for this path
	 * @param ce criticality exponent
	 * @return timing cost of this path
	 */
	public double timingCost(double ce) {
		cost= ((double) lookUpDelay()) * Math.pow(criticality, ce);
		oldCost= cost;
		return cost;
	}

	/**
	 * looks up the new delay value for this path in the LUTs
	 * @return
	 */
	private int lookUpDelay() {
		int delayTemp= 0;
		if(intermediate == null) { //only one net
			delayTemp= timingAnalyzer.lookUpSinglePathNoEndpoints(source, sink, source.getX(), source.getY(), sink.getX(), sink.getY());
		}
		else { //many nets
			delayTemp= timingAnalyzer.lookUpSinglePathNoEndpoints(source, intermediate[0], source.getX(), source.getY(), intermediate[0].getX(), intermediate[0].getY()) + parameterManager.T_COMB;
			for(int i= 1; i < intermediate.length; i++) {
				delayTemp+= timingAnalyzer.lookUpSinglePathNoEndpoints(intermediate[i-1], intermediate[i], intermediate[i-1].getX(), intermediate[i-1].getY(), intermediate[i].getX(), intermediate[i].getY()) + parameterManager.T_COMB;
			}
			delayTemp+= timingAnalyzer.lookUpSinglePathNoEndpoints(intermediate[intermediate.length - 1], sink, intermediate[intermediate.length - 1].getX(), intermediate[intermediate.length - 1].getY(), sink.getX(), sink.getY());
		}
		
		//add in and out delay
		if(source instanceof IOBlock) {
			delayTemp+= parameterManager.T_IPAD;
		}
		else {
			delayTemp+= parameterManager.T_FFOUT;
		}
		if(sink instanceof IOBlock) {
			delayTemp+= parameterManager.T_OPAD;
		}
		else {
			delayTemp+= parameterManager.T_FFIN;
		}
		
		return delayTemp;
	}
	

	/**
	 * returns the new timing cost after swap
	 * @param ce
	 * @param logicBlockSwap
	 * @return
	 */
	public double timingCostSwap(double ce, int[] logicBlockSwap) {
		oldCost= cost;
//		System.out.println("timingCostSwap: oldCost= " + oldCost);
		cost= ((double) lookUpDelaySwap(logicBlockSwap)) * Math.pow(criticality, ce);
//		System.out.println("timingCostSwap: criticality= " + criticality);
//		System.out.println("timingCostSwap: ce= " + ce);
//		System.out.println("timingCostSwap: lookUpDelaySwap(logicBlockSwap)= " + lookUpDelaySwap(logicBlockSwap));
//		System.out.println("timingCostSwap: cost= " + cost);
		return cost;
	}

	/**
	 * looks up the new delay value for this path in the LUTs after a swap
	 * @param logicBlockSwap
	 * @return
	 */
	private double lookUpDelaySwap(int[] logicBlockSwap) {
		int delayTemp= 0;
		if(intermediate == null) { //only one net
			delayTemp= lookUpChanged(source, sink, logicBlockSwap);
		}
		else { //many nets
			delayTemp= lookUpChanged(source, intermediate[0], logicBlockSwap) + parameterManager.T_COMB;
			for(int i= 1; i < intermediate.length; i++) {
				delayTemp+= lookUpChanged(intermediate[i-1], intermediate[i], logicBlockSwap) + parameterManager.T_COMB;
			}
			delayTemp+= lookUpChanged(intermediate[intermediate.length - 1], sink, logicBlockSwap);
		}
		
		//add in and out delay
		if(source instanceof IOBlock) {
			delayTemp+= parameterManager.T_IPAD;
		}
		else {
			delayTemp+= parameterManager.T_FFOUT;
		}
		if(sink instanceof IOBlock) {
			delayTemp+= parameterManager.T_OPAD;
		}
		else {
			delayTemp+= parameterManager.T_FFIN;
		}
		
		unchanged= true;
		
		return delayTemp;
	}

	/**
	 * looks up the new delay value for this path in the LUTs, only called when the value has changed
	 * @param from
	 * @param to
	 * @param logicBlockSwap
	 * @return
	 */
	private int lookUpChanged(NetlistBlock from, NetlistBlock to, int[] logicBlockSwap) {

		if(unchanged) return timingAnalyzer.lookUpSinglePathNoEndpoints(from, to, from.getX(), from.getY(), to.getX(), to.getY());
		if(from.getX() == logicBlockSwap[0] && from.getY() == logicBlockSwap[1] && (from instanceof LogicBlock || from.getSubblk_1() == (logicBlockSwap[2] == 1))) {
			if(to.getX() == logicBlockSwap[3] && to.getY() == logicBlockSwap[4] && (to instanceof LogicBlock || to.getSubblk_1() == (logicBlockSwap[5] == 1))) {
				return timingAnalyzer.lookUpSinglePathNoEndpoints(from, to, logicBlockSwap[3], logicBlockSwap[4], logicBlockSwap[0], logicBlockSwap[1]);
			}
			else {
				return timingAnalyzer.lookUpSinglePathNoEndpoints(from, to, logicBlockSwap[3], logicBlockSwap[4], to.getX(), to.getY());
			}
		}
		else if(from.getX() == logicBlockSwap[3] && from.getY() == logicBlockSwap[4] && (from instanceof LogicBlock || from.getSubblk_1() == (logicBlockSwap[5] == 1))) {
			if(to.getX() == logicBlockSwap[0] && to.getY() == logicBlockSwap[1] && (to instanceof LogicBlock || to.getSubblk_1() == (logicBlockSwap[2] == 1))) {
				return timingAnalyzer.lookUpSinglePathNoEndpoints(from, to, logicBlockSwap[0], logicBlockSwap[1], logicBlockSwap[3], logicBlockSwap[4]);
			}
			else {
				return timingAnalyzer.lookUpSinglePathNoEndpoints(from, to, logicBlockSwap[0], logicBlockSwap[1], to.getX(), to.getY());
			}
		}
		else {
			if(to.getX() == logicBlockSwap[3] && to.getY() == logicBlockSwap[4] && (to instanceof LogicBlock || to.getSubblk_1() == (logicBlockSwap[5] == 1))) {
				return timingAnalyzer.lookUpSinglePathNoEndpoints(from, to, from.getX(), from.getY(), logicBlockSwap[0], logicBlockSwap[1]);
			}
			else if(to.getX() == logicBlockSwap[0] && to.getY() == logicBlockSwap[1] && (to instanceof LogicBlock || to.getSubblk_1() == (logicBlockSwap[2] == 1))) {
				return timingAnalyzer.lookUpSinglePathNoEndpoints(from, to, from.getX(), from.getY(), logicBlockSwap[3], logicBlockSwap[4]);
			}
			else {
				return timingAnalyzer.lookUpSinglePathNoEndpoints(from, to, from.getX(), from.getY(), to.getX(), to.getY());
			}
		}
	}

	/**
	 * marks this path as changed by a recent swap
	 */
	public void setChanged() {

		unchanged= false;
		
	}
	
	/**
	 * standard getter
	 * @return
	 */
	public boolean getUpdated() {
		return updated;
	}
	
	/**
	 * standard setter
	 * @param newUpdated
	 */
	public void setUpdated(boolean newUpdated) {
		updated= newUpdated;
	}
	
	/**
	 * adds this to the list of paths in every connected block to be able to recieve the changed flag update
	 */
	public void registerAtBlocks() {
		source.addPath(this);
		sink.addPath(this);
		if(intermediate != null) {
			for(int i = 0; i < intermediate.length; i++) {
				intermediate[i].addPath(this);
			}
		}
	}

	/**
	 * retrieve cached timing cost, or compute, store and return the cost if the cache was invalid/empty
	 * @param ce criticality exponent for possible computation of cost
	 * @return timing cost
	 */
	public double getCachedCost(double ce) {
		if(cost != -1) return cost;
		else {
			cost= timingCost(ce);
			return cost;
		}
	}
	
	/**
	 * reset cache if swap was aborted
	 */
	public void resetCostCache() {
		cost= oldCost;
	}
	
}
