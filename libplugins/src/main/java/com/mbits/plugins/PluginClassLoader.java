package com.mbits.plugins;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class PluginClassLoader extends ClassLoader {
	
	private File m_zip;
	PluginClassLoader(File zip) {
		m_zip = zip;
	}

	private static void closeQuietly(InputStream input) {
		try {
			if (input != null) input.close();
		} catch (IOException ioe) {
			// ignore
		}
	}

	private static void closeQuietly(OutputStream input) {
		try {
			if (input != null) input.close();
		} catch (IOException ioe) {
			// ignore
		}
	}

	private static int copy(InputStream input, OutputStream output) throws IOException {
	    long count = copyLarge(input, output);
	    if (count > Integer.MAX_VALUE) {
	        return -1;
	    }
	    return (int) count;
	}

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

	private static long copyLarge(InputStream input, OutputStream output)
	        throws IOException {
	    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
	    long count = 0;
	    int n = 0;
	    while (-1 != (n = input.read(buffer))) {
	        output.write(buffer, 0, n);
	        count += n;
	    }
	    return count;
	}

	private Class<?> load(String classname) throws Exception {
		Class<?> c;
		c = findLoadedClass(classname);
		if (c != null) return c;

		try { c = findSystemClass(classname); }
		catch (Exception ex) {}
		if (c != null) return c;

		ZipFile zip = new ZipFile(m_zip);
		InputStream in = null;
		ByteArrayOutputStream out = null;
		
		try {
			String filename = classname.replace('.', File.separatorChar)+".class";
			ZipEntry entry = zip.getEntry(filename);
			if (entry == null) entry = zip.getEntry(classname.replace('.', '\\')+".class");
			if (entry == null) entry = zip.getEntry(classname.replace('.', '/')+".class");
			in = zip.getInputStream(entry);
			out = new ByteArrayOutputStream();
			copy(in, out);

			byte[] data = out.toByteArray();
			c = defineClass(classname, data, 0, data.length);
			return c;
			
		} finally {
			closeQuietly(in);
			closeQuietly(out);
			zip.close();
		}
	}

	public Class<?> loadClass (String classname, boolean resolve) throws ClassNotFoundException {
		try {
			
			Class<?> c = load(classname);
			if (c != null && resolve)
				resolveClass(c);
			if (c != null)
				return c;
		}
	    catch (Exception ex) { throw new ClassNotFoundException(classname, ex); }
    	throw new ClassNotFoundException(classname);
    }

}
