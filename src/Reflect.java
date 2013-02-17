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

import reflect.ClassHint;
import reflect.CodeExceptions;
import reflect.ParamsHint;
import reflect.android.AndroidParamsHint;

@SuppressWarnings("rawtypes")
public class Reflect {

	private static class ClassInfo
	{
		String m_className;
		Class m_class;
		String m_super;

		ClassInfo(String clazz)
		{
			m_className = clazz;
			m_class = null;
			m_super = null;
		}
		boolean Extends(String clazz)
		{
			return m_super != null && m_super.equals(clazz);
		}
		void Start() throws ClassNotFoundException {
			m_class = Class.forName(m_className);
			final Class s = m_class.getSuperclass();
			if (s != null) m_super = s.getName();
		}

		static void detachNextClass(Vector<Class> dst, Map<String, ClassInfo> src)
		{
			String nextName = null;
			Class next = null;
			for (Map.Entry<String, ClassInfo> e: src.entrySet())
			{
				if (e.getValue().m_super == null)
				{
					nextName = e.getKey();
					next = e.getValue().m_class;
					break;
				}
			}
			if (nextName != null) src.remove(nextName);
			if (next == null) return;
			dst.add(next);
			for (Map.Entry<String, ClassInfo> e: src.entrySet())
			{
				if (e.getValue().Extends(next.getName()))
					e.getValue().m_super = null;
			}
		}

		static void sort(Vector<Class> dst, Map<String, ClassInfo> src)
		{
			while(src.size() > 0)
			{
				int size = src.size();
				detachNextClass(dst, src);
				if (size == src.size()) // we have loops
				{
					for (Map.Entry<String, ClassInfo> e: src.entrySet())
					{
						if (e.getValue().m_class != null)
							dst.add(e.getValue().m_class);
					}
					break;
				}
			}
		}
	}

	private ParamsHint.HintCreator m_hinter;

	public Reflect(ParamsHint.HintCreator hinter) {
		m_hinter = hinter;
	}

	private ClassHint[] getHints(String klazz)
	{
		return m_hinter.createHint().getHints(klazz);
	}

	private static final String spaces = "                                                            ";
	private void printClass(Class klazz, int curr, int max) throws IOException
	{
		Class supah = klazz.getSuperclass();
		System.out.print("class " + klazz.getName());
		if (supah != null)
		{
			if (klazz.getName().length() < spaces.length())
				System.out.print(spaces.substring(klazz.getName().length()));
			System.out.print(" extends " + supah.getName());
		}
		System.out.println(" (" + curr + "/" + max + ")");

		ClassHint[] hints = getHints(klazz.getName());
	}

	private static void usage(String err)
	{
		System.out.println("usage: Reflect --android # <class> [<class>...]\n       Reflect --android # --test");
		System.out.println();
		if (err != null) System.err.println(err);
		else System.out.println("i.e.: Reflect --android 17 android.graphics.Canvas");
	}

	public static void main(String[] args)
	{

		CodeExceptions.readExceptions();

		Map<String, ClassInfo> classes = new HashMap<String, ClassInfo>();
		String sdk = null;

		boolean nextIsSdk = false;
		boolean performTests = false;
		for (String arg: args)
		{
			if (arg.startsWith("--"))
			{
				if (nextIsSdk)
				{
					usage("Error: --android followed by " + arg);
					return;
				}
				final String var = arg.substring(2);
				if (var.equalsIgnoreCase("android"))
					nextIsSdk = true;
				else if (var.equalsIgnoreCase("test"))
					performTests = true;
				else
				{
					usage("Unknown param: " + arg);
					return;
				}
				continue;
			}
			if (nextIsSdk)
			{
				sdk = arg;
				nextIsSdk = false;
			}
			else
			{
				classes.put(arg, new ClassInfo(arg));
			}
		}

		if (nextIsSdk || sdk == null || (classes.size() == 0 && !performTests))
		{
			usage(null);
			return;
		}

		try {
			final AndroidParamsHint.HintCreator hintCreator = new AndroidParamsHint.HintCreator(sdk);
			final Reflect reflect = new Reflect(hintCreator);

			if (!hintCreator.read())
				return;

			Map<String, ClassInfo> additional = new HashMap<String, ClassInfo>();
			if (performTests)
			{
				classes.clear();
				String[] api_classes = hintCreator.classes();
				for (String clazz: api_classes)
				{
					ClassInfo nfo = new ClassInfo(clazz);
					nfo.Start();
					additional.put(clazz, nfo);
				}
			}
			else
			{
				for (Map.Entry<String, ClassInfo> e: classes.entrySet())
				{
					e.getValue().Start();
					if (e.getValue().m_super != null)
					{
						String _superName = e.getValue().m_super;
						Class _super = Class.forName(_superName);
						while (_super != null)
						{
							ClassInfo nfo = new ClassInfo(_superName);
							nfo.Start();
							additional.put(_superName, nfo);
							_superName = nfo.m_super;
							_super = _superName == null ? null : Class.forName(_superName);
						}
					}
				}
			}
			for (Map.Entry<String, ClassInfo> e: additional.entrySet())
			{
				classes.put(e.getKey(), e.getValue());
			}
			Vector<Class> _classes = new Vector<Class>();
			ClassInfo.sort(_classes, classes);
			int curr = 0;
			for (Class c: _classes)
			{
				reflect.printClass(c, ++curr, _classes.size());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
