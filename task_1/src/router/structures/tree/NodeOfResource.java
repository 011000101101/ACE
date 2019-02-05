package router.structures.tree;

import java.util.PriorityQueue;

import router.structures.resourceWithCost.ChannelWithCost;
import router.structures.resourceWithCost.ResourceWithCost;

public class NodeOfResource {

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
	
	public void addChild(NodeOfResource additionalChild) {
		if(child == null) child= additionalChild;
		else child.addSibling(additionalChild);
	}

	private void addSibling(NodeOfResource additionalSibling) {
		if(sibling == null) sibling= additionalSibling;
		else sibling.addSibling(additionalSibling);
	}

	public void addAllChannelsToPriorityQueue(PriorityQueue<ResourceWithCost> pQ) {
		data.addChannelToPriorityQueue(pQ);
		sibling.addAllChannelsToPriorityQueue(pQ);
		child.addAllChannelsToPriorityQueue(pQ);
	}
	
}
