package reflect.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;

public class Plugins {
	private static final Attributes.Name PLUGIN_CLASS = new Attributes.Name("Plugin-Class");
	public static void loadPlugins(File dir) {
		if (!dir.exists() || !dir.isDirectory())
			return;

		File[] files = dir.listFiles();
		for (File file: files) {
			if (!file.isFile() || !file.toString().endsWith(".jar"))
				continue;

			final InputStream in;
			JarInputStream jar = null;

			try {
				in = new FileInputStream(file);
				jar = new JarInputStream(in);
				final Attributes main = jar.getManifest().getMainAttributes();
				if (!main.containsKey(PLUGIN_CLASS)) continue;
				final String pluginClass = (String)main.get(PLUGIN_CLASS);

				System.out.println("Plugin class: " + pluginClass);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try { 
					if (jar != null) jar.close();
				} catch (IOException e) {}
			}
		}
	}
}
