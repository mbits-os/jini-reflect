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
				throw new PluginJarException(PLUGIN_CLASS + " entry int the manifest is missing");
			return (String)main.get(PLUGIN_CLASS);
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
		 * Loads the plugin into {@link #m_plugins}. Enumerates the Jar files,
		 * first looking for the manifest entry called <code>Plugin-Class</code>,
		 * then tries to load the specified class, but only if the class is
		 * implementing the <code>clazz</code> interface.
		 * 
		 * <p><b>Warning!</b> Current implementation does not set any
		 * security manager.
		 * 
		 * <p>The code will throw {@link PluginJarException} if the entry is missing,
		 * <tt>ClassNotFoundException</tt> if the class cannot be located and
		 * {@link InterfaceNotFound} if the class does not have the plugin interface
		 * as it's direct ancestor.
		 * 
		 * @param dir The directory for the plugins
		 * @param clazz The class object of the interface
		 */
		public void loadPlugins(File dir, Class<T> clazz) {
			if (!dir.exists() || !dir.isDirectory())
				return;

			File[] files = dir.listFiles();
			for (File file: files) {
				if (!file.isFile() || !file.toString().endsWith(".jar"))
					continue;

				try {
					final String className = pluginClass(file);
					if (className == null) continue;
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

				} catch (IOException e) {
					System.err.println("While reading " + file + ":");
					e.printStackTrace();
				} catch (ReflectiveOperationException e) {
					System.err.println("While reading " + file + ":");
					e.printStackTrace();
				}
			}
		}
	}
}
