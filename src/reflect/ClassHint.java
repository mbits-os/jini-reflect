package reflect;

public class ClassHint {
	private String m_className;

	ClassHint(String className) {
		m_className = className;
	}

	String className() { return m_className; }
}
