import java.io.Serializable;

public class Message implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String destination;
	private String text;
	public Message(String destination, String text) {
		this.destination = destination;
		this.text = text;
	}
	public String getDestination() {
		return destination;
	}

	public String getText() {
		return text;
	}

	

}
