import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import reflect.ClassHint;
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

	private void printClass(Class klazz) throws IOException
	{
		Class supah = klazz.getSuperclass();
		System.out.println("\nClass: " + klazz.getName());
		if (supah != null)
			System.out.println("    Extends: " + supah.getName() + "\n");

		ClassHint[] hints = getHints(klazz.getName());
	}

	private static void usage(String err)
	{
		System.out.println("usage: Reflect --android # <class> [<class>...]");
		System.out.println();
		if (err != null) System.err.println(err);
		else System.out.println("i.e.: Reflect --android 17 android.graphics.Canvas");
	}

	public static void main(String[] args)
	{
		Map<String, ClassInfo> classes = new HashMap<String, ClassInfo>();
		String sdk = null;

		boolean nextIsSdk = false;
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

		if (nextIsSdk || sdk == null || classes.size() == 0)
		{
			usage(null);
			return;
		}

		try {
			final Reflect reflect = new Reflect(new AndroidParamsHint.HintCreator(sdk));

			Map<String, ClassInfo> additional = new HashMap<String, ClassInfo>();
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
			for (Map.Entry<String, ClassInfo> e: additional.entrySet())
			{
				classes.put(e.getKey(), e.getValue());
			}
			Vector<Class> _classes = new Vector<Class>();
			ClassInfo.sort(_classes, classes);
			for (Class c: _classes)
				reflect.printClass(c);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
