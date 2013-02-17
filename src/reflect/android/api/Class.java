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
	private Map<String, Property> m_props = new HashMap<String, Property>();
	private String m_super;
	private String[] m_interfaces;

	public Class() {}

	public Class(int since, String signature) {
		this(since, signature, null, new String[0]);
		m_interfaces = new String[0];
	}

	public Class(int since, String signature, String superClass) {
		this(since, signature, superClass, new String[0]);
		m_super = superClass;
		m_interfaces = new String[0];
	}

	public Class(int since, String signature, String superClass, String[] interfaces) {
		super(since, signature);
		m_super = superClass;
		m_interfaces = interfaces;
	}

	public String getName() { return getSignature(); }
	public String getSuper() { return m_super; }
	public String[] getInterfaces() { return m_interfaces; }

	boolean has(Method meth) {
		final String methName = meth.getName();
		if (!m_groups.containsKey(methName))
			return false;
		final MethodGroup group = m_groups.get(methName);
		for (Method m: group.m_methods)
		{
			if (m.equals(meth))
				return true;
		}
		return false;
	}

	boolean has(Property prop) {
		final String propName = prop.getName();
		if (!m_props.containsKey(propName))
			return false;
		final Property _prop = m_props.get(propName);
		return _prop.equals(prop);
	}

	void add(Method meth) {
		final String methName = meth.getName();
		if (!m_groups.containsKey(methName))
			m_groups.put(methName, new MethodGroup(methName));
		m_groups.get(methName).add(meth);
	}

	void add(Property prop) {
		m_props.put(prop.getName(), prop);
	}
}
