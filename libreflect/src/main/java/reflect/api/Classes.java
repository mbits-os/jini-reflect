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
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import reflect.api.APIBase;

/**
 * Entry point for looking-up the classes. The <code>Classes</code> achieves
 * that through collecting APIs and querying various bits of information
 * from them.
 */
public class Classes {
	static class Impl {
		private List<APIBase> m_apis = new LinkedList<APIBase>();

		Class forName(String className) {
			for (APIBase api: m_apis) {
				Class clazz = api.get(className);
				if (clazz == null) continue;
				clazz.update();
				return clazz;
			}
			return null;
		}

		boolean read() throws IOException {
			for (APIBase api: m_apis)
				if (!api.read())
					return false;
			return true;
		}

		public void addApi(APIBase api) {
			m_apis.add(api);
		}

		public String[] getClasses() {
			Vector<String> classes = new Vector<String>();
			boolean first = true;
			for (APIBase api: m_apis) {
				final Vector<String> api_classes = api.getClasses();
				if (first) {
					classes.addAll(api_classes);
					first = false;
				} else {
					for (String clazz: api_classes) {
						if (classes.contains(clazz))
							continue;
						classes.add(clazz);
					}
				}
			}
			String [] items = new String[classes.size()];
			return classes.toArray(items);
		}

		public String[] getPackage(String packageName) {
			for (APIBase api: m_apis) {
				final String[] pkg = api.getPackage(packageName);
				if (pkg != null)
					return pkg;
			}
			return new String[0];
		}

		public void getHints(String className) {
			for (APIBase api: m_apis)
				api.getHints(className);
		}
	}

	private static Impl impl = null;

	private static void ensureImpl() {
		if (impl == null)
			impl = new Impl();
	}

	private Classes() {}

	/**
	 * Looks up the class by its name. If found in one of the APIs, this method
	 * updates the class object and then returns it. The update assumes the field
	 * types are unknown and the <code>status</code> attribute is not applied.
	 * It first tries the VM's {@link java.lang.Class} and then moves on the the APIs. 
	 * 
	 * @param className the name of the class to look-up.
	 * @return object, if one exists in one of the APIs or <code>null</code> otherwise.
	 * @see reflect.api.APIBase#getHints(java.lang.String) APIBase.getHints(String)
	 */
	public static Class forName(String className) {
		ensureImpl();
		if (impl == null)
			return null;
		return impl.forName(className);
	}

	/**
	 * Registers an API.
	 * 
	 * @param api the API to register
	 */
	public static void addApi(APIBase api) {
		ensureImpl();
		if (impl == null)
			return;
		impl.addApi(api);
	}

	/**
	 * Reads the API configuration for every API.
	 * 
	 * @return <code>true</code> if all APIs return <code>true</code> from their {@link reflect.api.APIBase#read() read()}.
	 * @throws IOException
	 */
	public static boolean readApis() {
		try {
			ensureImpl();
			if (impl == null)
				return false;
			return impl.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Applies information, that might be missing after the {@link #readApis()}. 
	 * 
	 * @param className the name of the class to be hinted.
	 */
	static void getHints(String className) {
		if (impl != null)
			impl.getHints(className);
	}
	
	/**
	 * Gets all classes in all APIs.
	 * 
	 * @return a vector of class names
	 */
	static public String[] classNames() {
		if (impl == null) return new String[0];
		return impl.getClasses();
	}

	/**
	 * Finds all the classes inside the given package.
	 * Queries all APIs until one returns a non-<code>null</code> value.
	 * 
	 * @param packageName the name of the package
	 * @return the list of the class names
	 */
	static public String[] packageClasses(String packageName) {
		if (impl == null) return new String[0];
		return impl.getPackage(packageName);
	}
}
