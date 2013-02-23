package reflect;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public class Plugins {
	public static void loadPlugins(File dir) {
		System.out.println("Trying " + dir);
		if (!dir.exists() || !dir.isDirectory())
			return;

		File[] files = dir.listFiles();
		for (File file: files) {
			if (!file.isFile() || !file.toString().endsWith(".jar"))
				continue;

			JarInputStream jar = null;
			try {
				InputStream in = new FileInputStream(file);
				jar = new JarInputStream(in);
				Manifest mt = jar.getManifest();
				Attributes main = mt.getMainAttributes();
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
