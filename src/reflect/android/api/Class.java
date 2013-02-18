package reflect.android.api;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import reflect.ParamsHint;

public class Class extends Artifact {

	public static class MethodGroup {
		public List<Method> m_methods;
		private String m_name;
		MethodGroup(String name) {
			m_methods = new LinkedList<Method>();
			m_name = name;
		}

		public String getName() { return m_name; }

		public void add(Method meth) {
			m_methods.add(meth);
		}

		public String toString() { return m_methods.toString(); }
	}

	private Map<String, MethodGroup> m_groups = new HashMap<String, MethodGroup>();
	private Map<String, Property> m_props = new HashMap<String, Property>();
	private String m_super;
	private String[] m_interfaces;
	private Vector<String> m_internals = new Vector<String>();
	private boolean m_hinted;

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
	public String[] getInternals() {
		String[] subs = new String[m_internals.size()];
		return m_internals.toArray(subs);
	}
	public Property[] getProperties() {
		Property[] props = new Property[m_props.size()];
		return m_props.values().toArray(props);
	}
	public Method[] getMethods() {
		int len = 0;

		for (Map.Entry<String, MethodGroup> e: m_groups.entrySet())
			len += e.getValue().m_methods.size();
		
		Method[] meths = new Method[len];

		int index = 0;
		for (Map.Entry<String, MethodGroup> e: m_groups.entrySet())
			for (Method m: e.getValue().m_methods)
				meths[index++] = m;

		return meths;
	}

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

	public MethodGroup get(String methName) {
		if (!m_groups.containsKey(methName))
			return null;
		return m_groups.get(methName);
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

	void addInternalClass(String sub) {
		m_internals.add(sub);
	}

	public boolean fixDeclarationsFromVM() {
		java.lang.Class<?> _this = null;
		try {
			_this = java.lang.Class.forName(getName());
		} catch (ClassNotFoundException e) {
			return false;
		}
		for (java.lang.reflect.Field fld: _this.getFields())
		{
			if (!m_props.containsKey(fld.getName()))
				continue;

			final Property prop = m_props.get(fld.getName());
			prop.setSignature(fld.getType().getName());
			prop.setIsStatic(java.lang.reflect.Modifier.isStatic(fld.getModifiers()));
		}
		for (java.lang.reflect.Method meth: _this.getMethods())
		{
			if (!java.lang.reflect.Modifier.isStatic(meth.getModifiers()))
				continue;
			
			if (!m_groups.containsKey(meth.getName()))
				continue;

			final StringBuilder sb = new StringBuilder();
			sb.append("(");
			for (java.lang.Class<?> par: meth.getParameterTypes())
				sb.append(par.getName());
			sb.append(")");
			sb.append(meth.getReturnType().getName());
			final String sig = sb.toString();

			final MethodGroup group = m_groups.get(meth.getName());
			for (Method m: group.m_methods)
			{
				if (m.getType() == Method.Type.METHOD && m.getSignature().equals(sig))
				{
					m.setType(Method.Type.STATIC_METHOD);
					break;
				}
			}
		}
		return true;
	}

	public void getHints(ParamsHint hint) {
		if (!m_hinted)
			hint.getHints(getName());
	}
	
	public void setHinted(boolean hinted) { m_hinted = hinted; }

	public String toString() { return getName() + " " + m_groups.toString() + " " + m_props.toString(); }
}
