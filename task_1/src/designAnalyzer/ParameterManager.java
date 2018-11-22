package designAnalyzer;

public class ParameterManager {

	/**
	 * Singleton instance of ParameterManager
	 */
	private static ParameterManager instance= null;
	
	
	/**
	 * number of configurable logic blocks in X direction
	 */
	private int xGridSize;
	
	/**
	 * number of configurable logic blocks in Y direction
	 */
	private int yGridSize;
	
	/**
	 * number of parallel wires in each communication channel
	 */
	private int channelWidth;
	
	private ParameterManager() {
		
	}
	
	public static ParameterManager getInstance() {
		
		if(instance == null) {
			instance= new ParameterManager();
		}
		return instance;
		
	}

	public void setXGridSize(int xSize) {
		xGridSize= xSize;
	}

	public void setYGridSize(int ySize) {
		yGridSize= ySize;
	}

	public void setChannelWidth(int newChannelWidth) {
		channelWidth= newChannelWidth;
	}

	public int getXGridSize() {
		return xGridSize;
	}

	public int getYGridSize() {
		return yGridSize;
	}
	
}
