package reflect.api;

public class Artifact {
	private int m_since;
	private String m_signature;

	public Artifact() {
		this(1, "()V");
	}

	public Artifact(String signature) {
		this(1, signature);
	}

	public Artifact(int since, String signature) {
		m_since = since;
		m_signature = signature;
	}

	public String getSignature() { return m_signature; }
	public void setSignature(String sig) { m_signature = sig; }
	public final int availableSince() { return m_since; }
}
