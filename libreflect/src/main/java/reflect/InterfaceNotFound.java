package reflect;

public class InterfaceNotFound extends ReflectiveOperationException {

	private static final long serialVersionUID = -317885433077735424L;

	public InterfaceNotFound() {
	}

	public InterfaceNotFound(String message) {
		super(message);
	}

	public InterfaceNotFound(Throwable cause) {
		super(cause);
	}

	public InterfaceNotFound(String message, Throwable cause) {
		super(message, cause);
	}

}
