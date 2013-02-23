package reflect;

public class TokenException extends RuntimeException {

	private static final long serialVersionUID = 5324865741537259701L;

	public TokenException(String message, String file, long line) {
		super(getMessage(message, file, line));
	}

	private static String getMessage(String message, String file, long line) {
		if (file == null) return message;

		StringBuilder sb = new StringBuilder();
		if (message != null)
		{
			sb.append(message);
			sb.append(" ");
		}
		sb.append("(");
		sb.append(file);
		sb.append(":");
		sb.append(String.valueOf(line));
		sb.append(")");

		return sb.toString();
	}
}
