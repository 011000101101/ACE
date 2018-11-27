package designAnalyzer.structures.pathElements.channels;

public class ChannelY extends AbstractChannel {


	@Override
	public boolean isHorizontal() {
		return false;
	}
	
	protected boolean matchesIsChanX(boolean isChanX) {
		return false;
	}
}
