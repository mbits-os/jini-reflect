/*
 * Copyright (C) 2013 midnightBITS
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
 
package reflect.api;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import reflect.java.SourceCodeParamsHint;

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
	private String m_package;
	private String m_simpleName;
	private String m_outerName;
	private String m_super;
	private String[] m_interfaces;
	private Vector<String> m_internals = new Vector<String>();
	private boolean m_hinted = false, m_vmfixed = false;

	public Class() {}

	public Class(int since, String signature, String[] interfaces) {
		this(since, signature, null, interfaces);
	}

	public Class(int since, String signature, String superClass, String[] interfaces) {
		super(since, signature);
		m_super = superClass;
		m_interfaces = interfaces;
		final String name = getName();
		m_package = null;
		int pos = name.lastIndexOf('.');
		if (pos == -1) {
			m_package = null;
			m_outerName = name;
		} else {
			m_package = name.substring(0, pos);
			m_outerName = name.substring(pos + 1);
		}
		// if indexOf return -1, then substring will get 0; in this case it should return 'this'
		m_simpleName = m_outerName.substring(m_outerName.lastIndexOf('$') + 1);
	}

	public String getName() { return getSignature(); }
	public String getPackage() { return m_package; }
	public String getSimpleName() { return m_simpleName; }
	public String getOuterName() { return m_outerName; }
	public String getSuper() { return m_super; }
	public String[] getInterfaces() { return m_interfaces; }
	public String[] getInternals() {
		String[] subs = new String[m_internals.size()];
		return m_internals.toArray(subs);
	}
	public Class[] getClasses() {
		Class[] subs = new Class[m_internals.size()];
		for (int i = 0; i < subs.length; ++i)
			subs[i] = Classes.forName(m_internals.get(i));
		return subs;
	}
	public Property[] getProperties() {
		Property[] props = new Property[m_props.size()];
		return m_props.values().toArray(props);
	}
	public MethodGroup[] getGroups() {
		MethodGroup[] groups = new MethodGroup[m_groups.size()];
		return m_groups.values().toArray(groups);
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

	public void add(Method meth) {
		final String methName = meth.getName();
		if (!m_groups.containsKey(methName))
			m_groups.put(methName, new MethodGroup(methName));
		m_groups.get(methName).add(meth);
	}

	public void add(Property prop) {
		m_props.put(prop.getName(), prop);
	}

	public void addInternalClass(String sub) {
		m_internals.add(sub);
	}

	private boolean fixDeclarationsFromVM() {
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
			java.lang.Class<?> c = fld.getType();
			String name = c.getName();
			if (name.charAt(0) != '[')
			{
				String n = SourceCodeParamsHint.builtin(name);
				if (n == null) n = "L" + name + ";";
				name = n;
			}
			prop.setSignature(name);
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

	boolean update() {
		if (!m_vmfixed)
		{
			m_vmfixed = true;
			if (!fixDeclarationsFromVM())
				return false;
		}

		if (!m_hinted)
			Classes.getHints(getName());

		return true;
	}

	public void setHinted(boolean hinted) { m_hinted = hinted; }

	public String toString() { return getName() + " " + m_groups.toString() + " " + m_props.toString(); }

}
