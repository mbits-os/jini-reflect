package reflect;

import java.util.HashMap;
import java.util.Map;

public class ClassHint {
	private String m_className;
	private Map<String, MethodHint> m_methods;

	ClassHint(String className) {
		m_className = className;
		m_methods = new HashMap<String, MethodHint>();
	}

	String className() { return m_className; }
	void add(MethodHint hint)
	{
		if (hint == null)
			return;
		String sig = hint.signature();
		m_methods.put(sig, hint);
	}
}
