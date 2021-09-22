import java.io.Serializable;

public class ConnectionResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean success;
	private String leftId;
	private String rightId;
	public ConnectionResponse(boolean success, String leftId, String rightId) {

		this.success = success;
		this.leftId = leftId;
		this.rightId = rightId;
	}
	public boolean isSuccess() {
		return success;
	}
	public String getLeftId() {
		return leftId;
	}
	public String getRightId() {
		return rightId;
	}

	


}
