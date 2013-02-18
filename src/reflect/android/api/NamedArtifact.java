package reflect.android.api;

public class NamedArtifact extends Artifact {

	private String m_name;

	public NamedArtifact(String signature, String name) {
		this(1, signature, name);
	}

	public NamedArtifact(int since, String signature, String name) {
		super(since, signature);
		m_name = name;
	}

	public void setName(String nameHint) { m_name = nameHint; }
	public String getName() { return m_name; }

	public String toString() { return getSignature() + " " + getName(); }
}
