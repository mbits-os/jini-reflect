package reflect.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import reflect.android.api.Class;

public abstract class APIBase {
	private Map<String, Class> m_classes = new HashMap<String, Class>();

	protected void add(Class clazz) {
		m_classes.put(clazz.getName(), clazz);
	}

	public Class find(String clazz) {
		if (!m_classes.containsKey(clazz))
			return null;
		return m_classes.get(clazz);
	}

	public Class find(String clazz, int targetAPI) {
		final Class result = find(clazz);
		if (result == null) return null;
		if (result.availableSince() > targetAPI)
			return null;
		return result;
	}

	public Vector<String> getClasses()
	{
		Vector<String> classes = new Vector<String>();
		for (Map.Entry<String, Class> e: m_classes.entrySet())
		{
			if (filterOut(e.getValue()))
				continue;
			classes.add(e.getKey());
		}
		return classes;
	}

	public String[] getPackage(String packageName) {
		Vector<String> classes = new Vector<String>();
		for (Map.Entry<String, Class> e: m_classes.entrySet())
		{
			Class c = e.getValue();
			if (filterOut(c))
				continue;

			if (!c.getSimpleName().equals(c.getOuterName()))
				continue;

			if (!packageName.equals(c.getPackage()))
				continue;

			classes.add(c.getName());
		}
		if (classes.size() == 0)
			return null;

		String [] items = new String[classes.size()];
		return classes.toArray(items);
	}

	public Class get(String clazz) {
		if (!m_classes.containsKey(clazz))
			return null;
		Class klazz = m_classes.get(clazz);
		if (filterOut(klazz))
			return null;
		return klazz;
	}

	public abstract boolean read() throws IOException;
	protected boolean filterOut(Class c) { return true; }
	public abstract void getHints(String className);
}
