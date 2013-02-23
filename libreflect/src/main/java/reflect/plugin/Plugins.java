package reflect.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;

import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import reflect.InterfaceNotFound;
import reflect.PluginJarException;

public class Plugins {
	private static final Attributes.Name PLUGIN_CLASS = new Attributes.Name("Plugin-Class");
	private static List<Plugin> s_plugins = new LinkedList<Plugin>();

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

	public static void loadPlugins(File dir) {
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
					if (iface.equals(Plugin.class)) {
						found = true;
						break;
					}
				}
				if (!found)
					throw new InterfaceNotFound(Plugin.class.getName() + " in " + className + " missing");

				s_plugins.add((Plugin) c.newInstance());

			} catch (IOException e) {
				System.err.println("While reading " + file + ":");
				e.printStackTrace();
			} catch (ReflectiveOperationException e) {
				System.err.println("While reading " + file + ":");
				e.printStackTrace();
			}
		}
	}

	public static void onAddArguments(ArgumentParser parser) {
		for (Plugin plug: s_plugins) {
			if (!plug.wantsArguments()) continue;
			ArgumentGroup group = parser.addArgumentGroup(plug.getName() + " arguments");
			plug.onAddArguments(group);
		}
	}

	public static void onReadArguments(Namespace ns) throws Exception {
		for (Plugin plug: s_plugins) {
			if (!plug.wantsArguments()) continue;
			plug.onReadArguments(ns);
		}
	}
}
