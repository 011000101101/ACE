package router.structures;

public class ChannelWithCost {

	private int x;
	private int y;
	
	private Boolean horizontal;
	
	double cost;
	
	public ChannelWithCost(int newX, int newY, boolean newHorizontal, double newCost) {
		x= newX;
		y= newY;
		horizontal= newHorizontal;
		cost= newCost;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public Boolean getHorizontal() {
		return horizontal;
	}
	
	public double getCost() {
		return cost;
	}
	
	public void setX(int newX) {
		x= newX;
	}
	
	public void setY(int newY) {
		y= newY;
	}
	
	public void setHorizontal(boolean newHorizontal) {
		horizontal= newHorizontal;
	}
}
