package reflect.android.api;

import java.util.HashMap;
import java.util.Map;

public class API {
	private Map<String, Class> m_classes = new HashMap<String, Class>();
	void add(Class clazz) {
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
}
