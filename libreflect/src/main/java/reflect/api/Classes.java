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

	public static Class forName(String className) {
		ensureImpl();
		if (impl == null)
			return null;
		return impl.forName(className);
	}

	public static void addApi(APIBase api) {
		ensureImpl();
		if (impl == null)
			return;
		impl.addApi(api);
	}
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

	static void getHints(String className) {
		if (impl != null)
			impl.getHints(className);
	}
	
	static public String[] classNames() {
		if (impl == null) return new String[0];
		return impl.getClasses();
	}

	static public String[] packageClasses(String packageName) {
		if (impl == null) return new String[0];
		return impl.getPackage(packageName);
	}
}
