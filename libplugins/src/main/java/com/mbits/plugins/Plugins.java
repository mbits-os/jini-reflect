package com.mbits.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;

public class Plugins {
	private static final Attributes.Name PLUGIN_CLASS = new Attributes.Name("Plugin-Class");

	private static String pluginClass(File file) throws IOException {
		final InputStream in;
		JarInputStream jar = null;

		try {
			in = new FileInputStream(file);
			jar = new JarInputStream(in);
			final Attributes main = jar.getManifest().getMainAttributes();
			if (!main.containsKey(PLUGIN_CLASS))
				throw new PluginJarException(PLUGIN_CLASS + " entry int the menifest is missing");
			return (String)main.get(PLUGIN_CLASS);
		} finally {
			try { 
				if (jar != null) jar.close();
			} catch (IOException e) {}
		}
	}

	protected static class Impl<T extends Plugin> {
		protected List<T> m_plugins = new LinkedList<T>();
	
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
