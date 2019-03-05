package router.structures.tree;

import java.util.PriorityQueue;

import router.structures.resourceWithCost.ChannelWithCost;
import router.structures.resourceWithCost.ResourceWithCost;

public class NodeOfResource {

	
	//tree structure with maximum one child and maximum one sibling. Additional siblings added to first sibling and so on
	
//	/**
//	 * data contained in this node: x coordinate of this channel
//	 */
//	private int x;
//	/**
//	 * data contained in this node: y coordinate of this channel
//	 */
//	private int y;
//	/**
//	 * data contained in this node: horizontal flag of this channel
//	 */
//	private Boolean horizontal;
	
	/**
	* data contained in this node
	*/
	private ResourceWithCost data;
	
	/**
	 * child node in tree structure
	 */
	private NodeOfResource child; //avoid list of childs by permitting possible siblings to the single child
	/**
	 * sibling node in tree structure (same parent node)
	 */
	private NodeOfResource sibling;
	
	public NodeOfResource(ResourceWithCost newData) {
		data= newData;
		child= null;
	}
	
	public NodeOfResource(ResourceWithCost newData, NodeOfResource newChild) {
		data= newData;
		child= newChild;
	}
	
	/**
	 * returns the NodeOfPoint that contains a point equal to branchingPoint
	 * @param branchingPoint the point of which the NodeOfPoint holding it is to be found
	 * @return the NodeOfPoint that contains a point equal to branchingPoint, or null if none such NodeOfPoint exists in the subtree
	 */
	public NodeOfResource findBranchingPoint(ResourceWithCost branchingPoint) {
		if(data.equals(branchingPoint)) return this;
		NodeOfResource tmp= null;
		if(child != null) tmp= child.findBranchingPoint(branchingPoint);
		if(tmp == null && sibling != null) tmp= sibling.findBranchingPoint(branchingPoint);
		return tmp;
	}

	public void addAllToPriorityQueue(PriorityQueue<ResourceWithCost> pQ, int sinkX, int sinkY) {
		if(data instanceof ChannelWithCost) {
			data.setCostToZero(sinkX, sinkY);
			pQ.add(data);
		}
		if(sibling != null) sibling.addAllToPriorityQueueButSkipThisNode(pQ, sinkX, sinkY); //sibling is always a duplicate of parent which has already been processed
		if(child != null) child.addAllToPriorityQueue(pQ, sinkX, sinkY); //first child is never a duplicate
	}
	
	private void addAllToPriorityQueueButSkipThisNode(PriorityQueue<ResourceWithCost> pQ, int sinkX, int sinkY) {
		if(sibling != null) sibling.addAllToPriorityQueueButSkipThisNode(pQ, sinkX, sinkY); //sibling is always a duplicate of parent which has already been processed
		if(child != null) child.addAllToPriorityQueue(pQ, sinkX, sinkY); //first child is never a duplicate
	}

	public NodeOfResource getChild() {
		return child;
	}
	
	/**
	 * appends parameter as child when possible, else appends parameter as sibling to existing child
	 * @param additionalChild
	 */
	public void addChild(NodeOfResource additionalChild) {
		if(child == null) child= additionalChild;
		else child.addSibling(additionalChild);
	}
  
	/**
	 * appends parameter as sibling when possible, else append parameter as sibling to existing sibling
	 * @param additionalSibling
	 */
	private void addSibling(NodeOfResource additionalSibling) {
		if(sibling == null) sibling= additionalSibling;
		else sibling.addSibling(additionalSibling);
	}

	
	public NodeOfResource getSibling() {
		return sibling;
	}
	
	/**
	 * retrieves ResorceWithCost
	 * @return
	 */
	public ResourceWithCost getData() {
		return data;
	}
	
	/**
	 * function for cloning
	 */
	public NodeOfResource clone() {
		NodeOfResource tmp= new NodeOfResource(data);
		if(sibling != null) tmp.addSibling(sibling.clone());
		if(child != null) tmp.addChild(child.clone());
		return tmp;
	}

	/**
	 * effectively unroute the net
	 * recursively reduce the used counter of each ResourceWithCost contained in this tree exactly once (assumes correct structure for tree (branching point duplicates are repeated as sibling of first child of original))
	 */
	public void decUsedCounters() {
		data.decUsedCounter();
		if(child != null) child.decUsedCounters(); //first child is never a duplicate
		if(sibling != null) sibling.decUsedCountersButSkipThisNode(); //sibling is always a duplicate of parent which has already been processed
	}

	/**
	 * like decUsedCounters, but don't dec the counter of data, 
	 * this method is only called on siblings
	 */
	private void decUsedCountersButSkipThisNode() {
		//don't dec used counter of data
		if(child != null) child.decUsedCounters(); //first child is never a duplicate
		if(sibling != null) sibling.decUsedCountersButSkipThisNode(); //sibling is always a duplicate of parent which has already been processed
	}

	/**
	 * recursively update hv of each ResourceWithCost contained in this tree exactly once (assumes correct structure for tree (branching point duplicates are repeated as sibling of first child of original))
	 */
	public void updateHistoryCongestions() {
		data.updateHistoryCongestion();
		if(child != null) child.updateHistoryCongestions(); //first child is never a duplicate
		if(sibling != null) sibling.updateHistoryCongestionsButSkipThisNode(); //sibling is always a duplicate of parent which has already been processed
	}

	/**
	 * like updateHistoryCongestions, but don't update hv of data, 
	 * this method is only called on siblings
	 */
	private void updateHistoryCongestionsButSkipThisNode() {
		//don't dec used counter of data
		if(child != null) child.updateHistoryCongestions(); //first child is never a duplicate
		if(sibling != null) sibling.updateHistoryCongestionsButSkipThisNode(); //sibling is always a duplicate of parent which has already been processed
	}

	/**
	 * recursively checks for violations of resource limits
	 * multiple execution on same instance of ResourceWithCost does no harm, and won't cause performance hit
	 * may print information about violating resource for debugging
	 * @return
	 */
	public boolean checkForResourceLimitViolations() {
		boolean violated= false;
		violated= data.getUsedCounter() > 1;
		

		
		if(child != null) if(child.checkForResourceLimitViolations()) violated= true;
		if(sibling != null) if(sibling.checkForResourceLimitViolations()) violated= true;
		return violated;
	}
}
