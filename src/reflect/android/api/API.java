package reflect.android.api;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class API {
	private Map<String, Class> m_classes = new HashMap<String, Class>();

	public boolean read() throws RuntimeException {
		final String androidSDK = System.getenv("ANDROID_SDK");
		if (androidSDK == null)
		{
			throw new RuntimeException("Environment variable ANDROID_SDK is missing.");
		}
		return read(new File(androidSDK));
	}

	public boolean read(File android_sdk) {
		final File api = new File(android_sdk, "platform-tools" + File.separator + "api" + File.separator + "api-versions.xml");

		Document doc = null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(api);
			doc.getDocumentElement().normalize();
		} catch(Exception e) {
			return false;
		}

		NodeList nodes = doc.getElementsByTagName("class");
		int len = nodes.getLength();
		for (int i = 0; i < len; ++i)
		{
			final Element node = (Element)nodes.item(i);
			if (!read(node))
				return false;
		}
		return true;
	}

	private boolean read(Element clazz) {
		final String name = clazz.getAttribute("name");
		final String since = clazz.getAttribute("since");
		final int iSince;
		if (since == null) iSince = 1;
		else iSince = Integer.valueOf(since);
		
		Class _class = new Class(iSince, name);

		NodeList nodes = clazz.getElementsByTagName("method");
		int len = nodes.getLength();
		for (int i = 0; i < len; ++i)
		{
			final Element node = (Element)nodes.item(i);
			if (!read(_class, node))
				return false;
		}

		add(_class);
		return true;
	}

	private boolean read(Class _class, Element method) {
		final String name = method.getAttribute("name");
		final String since = method.getAttribute("since");
		
		if (name == null) return false;
		final String[] names = name.split("(", 2);
		if (names == null || names.length != 2)
			return false;

		final int iSince;
		if (since == null) iSince = 1;
		else iSince = Integer.valueOf(since);
		
		_class.add(new Method(iSince, names[0], "(" + names[1]));
		return true;
	}

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
