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
 
package com.mbits.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;

/**
 * Base class for the Plugin Engine. Classes implementing the engine should
 * provide their own plugin interface based on {@link Plugin} and extend
 * the {@link Plugins.Impl} with implementation of their own operations.
 * 
 * <p>Plugins of such engines should implement that interface and must put
 * <code>Plugin-Class</code> entry into the manifest file, e.g.:
 * 
 * <pre>Plugin-Class: {@link reflect.android.plugin.AndroidPlugin reflect.android.plugin.AndroidPlugin}</pre>
 * 
 * <p>Plugin object is created only, if the Jar file is placed in the directory
 * expected by the Plugins Engine, manifest entry is present and the class
 * pointed by this entry implements the interface of this particular Plugins Engine.
 * 
 * <p>If Jar placed in the plugins directory is not a plugin, it is treated as an exceptional
 * situation.
 */
public class Plugins {

	/**
	 * @hide
	 */
	public Plugins() {}

	private static final Attributes.Name PLUGIN_CLASS = new Attributes.Name("Plugin-Class");

	private static String pluginClass(File file) throws IOException {
		final InputStream in;
		JarInputStream jar = null;

		try {
			in = new FileInputStream(file);
			jar = new JarInputStream(in);
			final Attributes main = jar.getManifest().getMainAttributes();
			if (!main.containsKey(PLUGIN_CLASS))
				throw new PluginJarException(PLUGIN_CLASS + " entry in the manifest is missing");

			final String className = (String)main.get(PLUGIN_CLASS);
			if (className == null || className.isEmpty())
				throw new PluginJarException(PLUGIN_CLASS + " entry in the manifest is empty");
			return className;
		} finally {
			try { 
				if (jar != null) jar.close();
			} catch (IOException e) {}
		}
	}

	/**
	 * The Impl loads the plugins and keeps them for later reference. 
	 * 
	 * @param <T> The expected interface of the plugin. 
	 */
	protected static class Impl<T extends Plugin> {
		/**
		 * Holds the list of loaded plugins.
		 */
		protected List<T> m_plugins = new LinkedList<T>();
	
		/**
		 * @hide
		 */
		protected Impl() {}

		/**
		 * Loads single plugin. Performs actual checking for the <code>Plugin-Class</code>,
		 * locating the plugin class and examining its interfaces on behalf of
		 * {@link #loadPlugins(File, Class) loadPlugins}.
		 * 
		 * <p>The code will throw {@link PluginJarException} if the entry is missing,
		 * <tt>ClassNotFoundException</tt> if the class cannot be located and
		 * {@link InterfaceNotFound} if the class does not have the plugin interface
		 * as it's direct ancestor.
		 * 
		 * @param file The .jar file with the plugin
		 * @param clazz The class object of the interface
		 * @throws Exception The problem reported by the class matching algorithm
		 */
		protected final void loadPlugin(File file, Class<T> clazz) throws Exception {
			final String className = pluginClass(file);
			Class<?> c = Class.forName(className, true, new PluginClassLoader(file));
			Class<?>[] ifaces = c.getInterfaces();
			boolean found = false;
			for (Class<?> iface: ifaces) {
				if (iface.equals(clazz)) {
					found = true;
					break;
				}
			}
			if (!found)
				throw new InterfaceNotFound(clazz.getName() + " in " + className + " missing");

			final T plugin = clazz.cast(c.newInstance());
			if (plugin != null);
				m_plugins.add(plugin);
		}

		/**
		 * Wrapper for {@link #loadPlugin(File, Class) loadPlugin}. Catches most
		 * of the exceptions <tt>loadPlugin</tt> might have thrown. This way problem
		 * with one faulty plugin will not prevent other plugins to load. Implementers
		 * of the Plugin Engine might decide to handle this in other way, they should,
		 * however, call the <tt>loadPlugin</tt> with the arguments provided, or no
		 * plugins will be loaded.
		 * 
		 * @param file The .jar file with the plugin
		 * @param clazz The class object of the interface
		 * @throws Exception In overriden methods: the problem reported by the
		 *                   class matching algorithm.
		 */
		protected void loadPluginWrapper(File file, Class<T> clazz) throws Exception {
				try {
					loadPlugin(file, clazz);
				} catch (IOException e) {
					System.err.println("While reading " + file + ":");
					e.printStackTrace();
				} catch (ReflectiveOperationException e) {
					System.err.println("While reading " + file + ":");
					e.printStackTrace();
				}
			
		}

		/**
		 * Loads the plugin into {@link #m_plugins}. Enumerates the Jar files,
		 * first looking for the manifest entry called <code>Plugin-Class</code>,
		 * then tries to load the specified class, but only if the class is
		 * implementing the <code>clazz</code> interface. The work is performed by
		 * {@link #loadPlugin(File, Class) loadPlugin} called through
		 * {@link #loadPluginWrapper(File, Class) loadPluginWrapper}.
		 * 
		 * <p><b>Warning!</b> Current implementation does not set any
		 * security manager.
		 * 
		 * @param dir The directory for the plugins
		 * @param clazz The class object of the interface
		 * @throws Exception Could throw an exception with overriden {@link #loadPluginWrapper(File, Class) loadPluginWrapper}.
		 */
		public void loadPlugins(File dir, Class<T> clazz) throws Exception {
			if (!dir.exists() || !dir.isDirectory())
				return;

			File[] files = dir.listFiles();
			for (File file: files) {
				if (!file.isFile() || !file.toString().endsWith(".jar"))
					continue;
				loadPluginWrapper(file, clazz);
			}
		}
	}
}
