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
		//if(data.neighbours(branchingPoint)) return this;
		if(data.equals(branchingPoint)) return this;
		NodeOfResource tmp= null;
		if(child != null) tmp= child.findBranchingPoint(branchingPoint);
		if(tmp == null && sibling != null) tmp= sibling.findBranchingPoint(branchingPoint);
		return tmp;
	}

	/*
	public void addAllToPriorityQueue(PriorityQueue<ResourceWithCost> pQ) {
		pQ.add(data);
		sibling.addAllToPriorityQueue(pQ);
		child.addAllToPriorityQueue(pQ);
	}
	*/
	
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

//	public void addAllChannelsToPriorityQueue(PriorityQueue<ResourceWithCost> pQ) {
//		
//		
//		data.addChannelToPriorityQueue(pQ);
//		data.setAlreadyAdded(true, innerIterationCounter, iterationCounter);
//		if(sibling != null) sibling.addAllChannelsToPriorityQueue(pQ);
//		if(child != null) child.addAllChannelsToPriorityQueue(pQ);
//	}
	
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
}
