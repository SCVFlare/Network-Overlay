import java.io.Serializable;

public class ConnectionRequest implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String hostId;
	private String nodeId;
	private boolean mode;
	public ConnectionRequest(String h, String n,boolean m) {
		this.hostId = h;
		this.nodeId = n;
		this.mode =m;
	}
	public String getHostId() {
		return hostId;
	}
	public String getNodeId() {
		return nodeId;
	}
	public boolean getMode() {
		return mode;
	}
	
}
