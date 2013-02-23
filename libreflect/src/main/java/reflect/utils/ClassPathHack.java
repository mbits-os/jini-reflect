package reflect.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

@SuppressWarnings("rawtypes")
public class ClassPathHack {
	private static final Class[] parameters = new Class[] {URL.class};

    public static boolean addFile(String s) throws IOException
    {
        File f = new File(s);
        return addFile(f);
    }

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
