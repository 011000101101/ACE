package designAnalyzer;

/**
 * 
 * @author Dennis Grotz, Rumei Ma, Vincenz Mechler
 *
 */
public class ParameterManager {

	//global constants for efficient access from everywhere
	/*
	public static final int T_CONNECT_LOGIC_BLOCK_INTERNAL = 0;

	public static final int T_CONNECT_CHANNEL_IOBLOCK = 0;

	public static final int T_CONNECT_CHANNEL_CHANNEL = 0;

	public static final int T_CONNECT_CHANNEL_LOGIC_BLOCK = 0;
	*/
	
	
	/**
	 * delay out of input block (source)
	 */
	public final int T_IPAD;
	
	/**
	 * delay into output block (sink)
	 */
	public final int T_OPAD;
	
	/**
	 * delay of one switching element
	 */
	public final int T_SWITCH;
	
	/**
	 * internal delay of a combinatorial (unclocked) logic block
	 */
	public final int T_COMB;
	
	/**
	 * delay into clocked logic block (sink)
	 */
	public final int T_FFIN;
	
	/**
	 * delay out of clocked logic block (source)
	 */
	public final int T_FFOUT;


	
	/**
	 * Singleton instance of ParameterManager
	 */
	private static ParameterManager instance= null;
	
	
	/**
	 * number of configurable logic blocks in X direction
	 */
	public final int X_GRID_SIZE;
	
	/**
	 * number of configurable logic blocks in Y direction
	 */
	public final int Y_GRID_SIZE;
	
	
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
	
	/**
	 * construct a ParameterManager with given file paths
	 * @param netlistFilePath
	 * @param architectureFilePath
	 * @param placementFilePath
	 * @param array with all parameters (grid size, channel width and delays)
	 */
	public static void initialize(String netlistFilePath, String architectureFilePath, String placementFilePath, int[] parameter) {
		if(instance == null) {
			instance= new ParameterManager(netlistFilePath, architectureFilePath, placementFilePath, parameter);
		}
		
	}
	
	/**
	 * standard getter 
	 * @return a parameterManager
	 */
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
