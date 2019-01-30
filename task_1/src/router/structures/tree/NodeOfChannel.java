package router.structures.tree;

import java.util.PriorityQueue;

import router.structures.ChannelWithCost;

public class NodeOfChannel {

	/**
	 * data contained in this node: x coordinate of this channel
	 */
	private int x;
	/**
	 * data contained in this node: y coordinate of this channel
	 */
	private int y;
	/**
	 * data contained in this node: horizontal flag of this channel
	 */
	private Boolean horizontal;
	
	/**
	 * child node in tree structure
	 */
	private NodeOfChannel child; //avoid list of childs by permitting possible siblings to the single child
	/**
	 * sibling node in tree structure (same parent node)
	 */
	private NodeOfChannel sibling;
	
	public NodeOfChannel(int newX, int newY, boolean newHorizontal) {
		x= newX;
		y= newY;
		horizontal= newHorizontal;
	}
	
	public NodeOfChannel(int newX, int newY, boolean newHorizontal, NodeOfChannel newChild) {
		x= newX;
		y= newY;
		horizontal= newHorizontal;
		child= newChild;
	}
	
	/**
	 * returns the NodeOfPoint that contains a point equal to branchingPoint
	 * @param branchingPoint the point of which the NodeOfPoint holding it is to be found
	 * @return the NodeOfPoint that contains a point equal to branchingPoint, or null if none such NodeOfPoint exists in the subtree
	 */
	public NodeOfChannel findBranchingPoint(int branchingPointX, int branchingPointY, boolean branchingPointHorizontal) {
		if(x == branchingPointX && y == branchingPointY && horizontal == branchingPointHorizontal) return this;
		NodeOfChannel tmp= null;
		if(child != null) tmp= child.findBranchingPoint(branchingPointX, branchingPointY, branchingPointHorizontal);
		if(tmp == null && sibling != null) tmp= sibling.findBranchingPoint(branchingPointX, branchingPointY, branchingPointHorizontal);
		return tmp;
	}

	public void addAllToPriorityQueue(PriorityQueue<ChannelWithCost> pQ) {
		pQ.add(new ChannelWithCost(x, y, horizontal, 0));
		sibling.addAllToPriorityQueue(pQ);
		child.addAllToPriorityQueue(pQ);
	}
	
	public NodeOfChannel getChild() {
		return child;
	}
	
	public void addChild(NodeOfChannel additionalChild) {
		if(child == null) child= additionalChild;
		else child.addSibling(additionalChild);
	}

	private void addSibling(NodeOfChannel additionalSibling) {
		if(sibling == null) sibling= additionalSibling;
		else sibling.addSibling(additionalSibling);
	}
	
}
