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

	void setName(String nameHint) { m_name = nameHint; }
	String getName() { return m_name; }

}
