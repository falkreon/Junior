package blue.endless.junior.esovm;

public class VMException extends Exception {
	private static final long serialVersionUID = 3575334892567684864L;
	
	public VMException() {
	}
	
	public VMException(String message) {
		super(message);
	}
	
	public VMException(String message, Throwable cause) {
		super(message, cause);
	}
}
