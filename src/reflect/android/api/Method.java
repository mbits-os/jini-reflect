package reflect.android.api;

public class Method extends Artifact {

	private String m_name;

	public Method() {
		this(1, "<init>", "()V");
	}

	public Method(String name, String signature) {
		this(1, name, signature);
	}

	public Method(int since, String name, String signature) {
		super(since, signature);
		m_name = name;
	}

	public String getName() { return m_name; }
}
