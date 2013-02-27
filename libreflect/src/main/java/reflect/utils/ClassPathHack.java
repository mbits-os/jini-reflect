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
 
package reflect.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Allows to add a Jar file in run time.
 */
@SuppressWarnings("rawtypes")
public class ClassPathHack {
	private static final Class[] parameters = new Class[] {URL.class};

	/**
	 * @hide
	 */
	public ClassPathHack() {}

	/**
	 * Adds a Jar file by its path.
	 * 
	 * @param s the path of the Jar file
	 * @throws IOException
	 */
	public static boolean addFile(String s) throws IOException
    {
        File f = new File(s);
        return addFile(f);
    }

	/**
	 * Adds a Jar file by its path.
	 * 
	 * @param f the path of the Jar file
	 * @throws IOException
	 */
    public static boolean addFile(File f) throws IOException
    {
    	boolean ret = true;
		if (!f.exists() || !f.isFile()) {
			System.err.println(f + " does not exist");
			ret = false;
		}
        addURL(f.toURI().toURL());
        return ret;
    }

	/**
	 * Adds a Jar file by its URL.
	 * 
	 * @param u the url of the Jar file
	 * @throws IOException
	 */
	public static void addURL(URL u) throws IOException
    {
        try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            method.invoke(sysloader, new Object[] {u});
        } catch (Exception ex) {
            throw new IOException("Error, could not add URL to system classloader", ex);
        }

    }
}
