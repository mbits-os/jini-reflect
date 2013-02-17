package reflect.android.api;

public class Property extends NamedArtifact {

	public Property(String signature, String name) {
		this(1, signature, name);
	}

	public Property(int since, String signature, String name) {
		super(since, signature, name);
	}

}
