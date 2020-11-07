package blue.endless.junior.esovm;

public class VMStackException extends VMException {
	private static final long serialVersionUID = 4899627396637075098L;
	
	public VMStackException() {}
	
	public VMStackException(String message) {
		super(message);
	}
	
	public VMStackException(String message, Throwable cause) {
		super(message, cause);
	}
}
