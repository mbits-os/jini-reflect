package reflect.android.api;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Class extends Artifact {

	static class MethodGroup {
		private List<Method> m_methods;
		private String m_name;
		MethodGroup(String name) {
			m_methods = new LinkedList<Method>();
			m_name = name;
		}

		public String getName() { return m_name; }

		public void add(Method meth) {
			m_methods.add(meth);
		}
	}

	private Map<String, MethodGroup> m_groups = new HashMap<String, MethodGroup>();

	public Class() {}

	public Class(String signature) {
		super(signature);
	}

	public Class(int since, String signature) {
		super(since, signature);
	}

	public String getName() { return getSignature(); }

	void add(Method meth) {
		final String methName = meth.getName();
		if (!m_groups.containsKey(methName))
			m_groups.put(methName, new MethodGroup(methName));
		m_groups.get(methName).add(meth);
	}
}
