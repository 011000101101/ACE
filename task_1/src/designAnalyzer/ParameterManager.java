package designAnalyzer;

public class ParameterManager {

	//global constants for efficient access from everywhere
	
	public static final int T_CONNECT_LOGIC_BLOCK_INTERNAL = 0;

	public static final int T_CONNECT_CHANNEL_IOBLOCK = 0;

	public static final int T_CONNECT_CHANNEL_CHANNEL = 0;

	public static final int T_CONNECT_CHANNEL_LOGIC_BLOCK = 0;
	
	
	
	public final int T_IPAD;
	
	public final int T_OPAD;
	
	public final int T_SWITCH;
	
	public final int T_COMB;
	
	public final int T_FFIN;
	
	public final int T_FFOUT;


	
	/**
	 * Singleton instance of ParameterManager
	 */
	private static ParameterManager instance= null;
	
	
	/**
	 * number of configurable logic blocks in X direction
	 */
	private final int X_GRID_SIZE;
	
	/**
	 * number of configurable logic blocks in Y direction
	 */
	private final int Y_GRID_SIZE;
	
	
	/**
	 * path of netlist file
	 */
	public final String NETLIST_PATH;
	
	/**
	 * path of architecture file
	 */
	public final String ARCHITECTURE_PATH;
	
	/**
	 * path of placement file
	 */
	public final String PLACEMENT_PATH;
	
	
	/**
	 * number of parallel wires in each communication channel
	 */
	public final int CHANNEL_WIDTH;
	
	private ParameterManager(String netlistFilePath, String architectureFilePath, String placementFilePath, int[] parameter) {
		NETLIST_PATH = netlistFilePath;
		ARCHITECTURE_PATH = architectureFilePath;
		PLACEMENT_PATH = placementFilePath;
		X_GRID_SIZE = parameter[0];
		Y_GRID_SIZE = parameter[1];
		CHANNEL_WIDTH = parameter[2];
		T_IPAD = parameter[3];
		T_OPAD = parameter[4];
		T_SWITCH = parameter[5];
		T_COMB = parameter[6];
		T_FFIN = parameter[7];
		T_FFOUT = parameter[8];
	}
	

	public static void initialize(String netlistFilePath, String architectureFilePath, String placementFilePath, int[] parameter) {
		if(instance == null) {
			instance= new ParameterManager(netlistFilePath, architectureFilePath, placementFilePath, parameter);
		}
		
	}
	
	
	public static ParameterManager getInstance() {
		
		return instance;
		
	}
/*
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
	
	public String getNetlistPath() {
		return NETLIST_PATH;
	}
	
	public void setNetlistName(String newVal) {
		netlistPath = newVal;
	}*/

}
