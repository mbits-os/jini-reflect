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

public abstract class APIBase {
	private Map<String, Class> m_classes = new HashMap<String, Class>();

	protected void add(Class clazz) {
		m_classes.put(clazz.getName(), clazz);
	}

	public Class find(String clazz) {
		if (!m_classes.containsKey(clazz))
			return null;
		return m_classes.get(clazz);
	}

	public Class find(String clazz, int targetAPI) {
		final Class result = find(clazz);
		if (result == null) return null;
		if (result.availableSince() > targetAPI)
			return null;
		return result;
	}

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

	public Class get(String clazz) {
		if (!m_classes.containsKey(clazz))
			return null;
		Class klazz = m_classes.get(clazz);
		if (filterOut(klazz))
			return null;
		return klazz;
	}

	public abstract boolean read() throws IOException;
	protected boolean filterOut(Class c) { return true; }
	public abstract void getHints(String className);
}
