package designAnalyzer;

public class ParameterManager {

	//global constants for efficient access from everywhere
	
	public static final int T_CONNECT_LOGIC_BLOCK_INTERNAL = 0;

	public static final int T_CONNECT_CHANNEL_IOBLOCK = 0;

	public static final int T_CONNECT_CHANNEL_CHANNEL = 0;

	public static final int T_CONNECT_CHANNEL_LOGIC_BLOCK = 0;


	
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
