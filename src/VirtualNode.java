


public class VirtualNode {

	private String hostId;
	private String nId;
	private String leftId;
	private String rightId;

	public VirtualNode(String id, String l, String r) {
		this.hostId=null;
		this.nId = id;
		this.leftId = l;
		this.rightId=r;

	}
	public void print() {
		System.out.println("V-NodeId:"+nId+",occupiedBy:"+hostId+",leftId:"+leftId+"rightId:"+rightId);

	}
	public String getHostId() {
		return hostId;
	}
	public void setHostId(String hostId) {
		this.hostId=hostId;
	}
	public String getNid() {
		return nId;
	}
	public String getLeftId() {
		return leftId;
	}

	public String getRightId() {
		return rightId;
	}



	
	
}
