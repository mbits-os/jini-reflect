package reflect.android.api;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import reflect.android.AndroidParamsHint;
import reflect.api.APIBase;
import reflect.utils.ClassPathHack;

public class API extends APIBase {
	private static final String API_VERSIONS = "platform-tools" + File.separator + "api" + File.separator + "api-versions.xml";
	private File m_sdk_root;
	private File m_sdk;
	private int m_targetAPI;


	public API() {
		final String androidSDK = System.getenv("ANDROID_HOME");
		if (androidSDK == null)
		{
			throw new RuntimeException("Environment variable ANDROID_HOME is missing.");
		}
		m_sdk_root = new File(androidSDK);
		m_targetAPI = -1;
		m_sdk    = null;
	}

	@Override public boolean read() throws RuntimeException {
		if (m_targetAPI == -1)
			throw new RuntimeException("Android API Level is not set");
		return read(m_sdk_root);
	}

	@Override protected boolean filterOut(Class c)
	{
		return c.availableSince() >= m_targetAPI;
	}

	@Override public void getHints(String className) {
		new AndroidParamsHint(m_sdk, this, m_targetAPI).getHints(className);
	}
	
	public boolean setTargetApi(int targetAPI) {
		m_targetAPI = targetAPI;
		m_sdk = new File(m_sdk_root, "sources" + File.separator + "android-" + targetAPI);
		try {
			ClassPathHack.addFile(new File(m_sdk_root, "platforms" + File.separator + "android-" + targetAPI + File.separator + "android.jar"));
			ClassPathHack.addFile(new File(m_sdk_root, "add-ons" + File.separator + "addon-google_apis-google-" + targetAPI + File.separator + "libs" + File.separator + "maps.jar"));
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public boolean read(File android_sdk) {
		final File api = new File(android_sdk, API_VERSIONS);
		Map<String, String> subs = new HashMap<String, String>();

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
			if (!read(node, subs))
				return false;
		}
		
		for (Map.Entry<String, String> e: subs.entrySet())
		{
			Class cont = find(e.getValue());
			if (cont == null) continue;
			cont.addInternalClass(e.getKey());
		}
		return true;
	}

	private boolean read(Element clazz, Map<String, String> subs) {
		Vector<String> interfaces = new Vector<String>();
		final String name = clazz.getAttribute("name");
		final String since = clazz.getAttribute("since");
		final int iSince;
		if (since == null) iSince = 1;
		else iSince = Integer.valueOf(since);

		NodeList nodes;
		int len;

		String superClass = null;

		nodes = clazz.getElementsByTagName("extends");
		len = nodes.getLength();
		if (len > 0)
		{
			final Element node = (Element)nodes.item(0);
			superClass = node.getAttribute("name");
			if (superClass.isEmpty())
				superClass = null;
		}

		nodes = clazz.getElementsByTagName("implements");
		len = nodes.getLength();
		for (int i = 0; i < len; ++i)
		{
			final Element node = (Element)nodes.item(i);
			final String iface = node.getAttribute("name");
			interfaces.add(iface.replace("/", "."));
		}
		String[] ifaces = new String[interfaces.size()];
		ifaces = interfaces.toArray(ifaces);

		Class _class = superClass == null ?
				new Class(iSince, name.replace("/", "."), ifaces) :
				new Class(iSince, name.replace("/", "."), superClass.replace("/", "."), ifaces);

		int pos = name.lastIndexOf('$');
		if (pos != -1)
			subs.put(_class.getName(), name.substring(0, pos).replace("/", "."));

		nodes = clazz.getElementsByTagName("method");
		len = nodes.getLength();
		for (int i = 0; i < len; ++i)
		{
			final Element node = (Element)nodes.item(i);
			if (!readMethod(_class, node))
				return false;
		}

		nodes = clazz.getElementsByTagName("field");
		len = nodes.getLength();
		for (int i = 0; i < len; ++i)
		{
			final Element node = (Element)nodes.item(i);
			if (!readProp(_class, node))
				return false;
		}
		
		add(_class);
		return true;
	}

	private boolean readMethod(Class _class, Element method) {
		final String name = method.getAttribute("name");
		final String since = method.getAttribute("since");
		
		if (name == null) return false;
		final String[] names = name.split("\\(", 2);
		if (names == null || names.length != 2)
			return false;

		final int iSince;
		if (since == null || since.isEmpty()) iSince = 1;
		else iSince = Integer.valueOf(since);
		
		_class.add(new Method(iSince, names[0], "(" + names[1].replace("/", ".")));
		return true;
	}

	private boolean readProp(Class _class, Element property) {
		final String name = property.getAttribute("name");
		final String since = property.getAttribute("since");
		
		if (name == null) return false;

		final int iSince;
		if (since == null || since.isEmpty()) iSince = 1;
		else iSince = Integer.valueOf(since);
		
		_class.add(new Property(iSince, name));
		return true;
	}
}
