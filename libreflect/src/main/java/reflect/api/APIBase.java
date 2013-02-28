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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Base class for the API plugins.
 */
public abstract class APIBase {
	private Map<String, Class> m_classes = new HashMap<String, Class>();

	/**
	 * Constructs a new instance of <code>APIBase</code>.
	 */
	protected APIBase() {}

	/**
	 * Allows the API implementations add a class during the {@link #read()}.
	 * 
	 * @param clazz the class to add
	 */
	protected void add(Class clazz) {
		m_classes.put(clazz.getName(), clazz);
	}

	/**
	 * Gets all classes in the API.
	 * 
	 * @return a vector of class names
	 */
	public Vector<String> getClasses()
	{
		Vector<String> classes = new Vector<String>();
		for (Map.Entry<String, Class> e: m_classes.entrySet())
		{
			if (filterOut(e.getValue()))
				continue;
			classes.add(e.getKey());
		}
		return classes;
	}

	/**
	 * Finds all the classes inside the given package.
	 * 
	 * @param packageName the name of the package
	 * @return the list of the class names
	 */
	public String[] getPackage(String packageName) {
		Vector<String> classes = new Vector<String>();
		for (Map.Entry<String, Class> e: m_classes.entrySet())
		{
			Class c = e.getValue();
			if (filterOut(c))
				continue;

			if (!c.getSimpleName().equals(c.getOuterName()))
				continue;

			if (!packageName.equals(c.getPackage()))
				continue;

			classes.add(c.getName());
		}
		if (classes.size() == 0)
			return null;

		String [] items = new String[classes.size()];
		return classes.toArray(items);
	}

	/**
	 * Looks up the object representing the class.
	 * 
	 * @param clazz name of the class in <code>Lpackage.subpkg.Class;</code> form.
	 * @return null if not found or derived class filtered this class out; class object otherwise.
	 */
	public Class get(String clazz) {
		if (!m_classes.containsKey(clazz))
			return null;
		Class klazz = m_classes.get(clazz);
		if (filterOut(klazz))
			return null;
		return klazz;
	}

	/**
	 * Reads the API configuration. Any class discovered should be added through {@link #add(reflect.api.Class) add(...)}
	 * 
	 * @return <code>true</code> if the read succeeded.
	 * @throws IOException
	 */
	public abstract boolean read() throws IOException;

	/**
	 * Allows the API to choose whether the class should be included or not.
	 * The default implementation disallows all classes.
	 * 
	 * @param c the class to test
	 * @return <code>true</code> is the implementing class wants to filter the class out. 
	 */
	protected boolean filterOut(Class c) { return true; }

	/**
	 * Applies information, that might be missing after the {@link #read()}. 
	 * 
	 * @param className the name of the class to be hinted.
	 */
	public abstract void getHints(String className);
}
