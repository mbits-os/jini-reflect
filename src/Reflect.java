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
import reflect.android.api.Class;

@SuppressWarnings("rawtypes")
public class Reflect {

	private ParamsHint.HintCreator m_hinter;

	public Reflect(ParamsHint.HintCreator hinter) {
		m_hinter = hinter;
	}

	private ClassHint[] getHints(String klazz)
	{
		return m_hinter.createHint().getHints(klazz);
	}

	private static final String spaces = "                                                            ";
	private void printClass(java.lang.Class klazz, int curr, int max) throws IOException
	{
		java.lang.Class supah = klazz.getSuperclass();
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

		Map<String, Integer> classes = new HashMap<String, Integer>();
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
				classes.put(arg, 0);
			}
		}

		if (nextIsSdk || sdk == null || (classes.size() == 0 && !performTests))
		{
			usage(null);
			return;
		}

		try {
			final AndroidParamsHint.HintCreator androidAPI = new AndroidParamsHint.HintCreator(sdk);
			final Reflect reflect = new Reflect(androidAPI);

			if (!androidAPI.read())
				return;

			if (performTests)
			{
				classes.clear();
				String[] api_classes = androidAPI.classes();
				for (String clazz: api_classes)
				{
					Class klazz = androidAPI.get(clazz);
					if (klazz.fixDeclarationsFromVM())
						System.out.println(klazz.getName());
					else
						System.err.println(klazz.getName());
				}
			}
			else
			{
				for (Map.Entry<String, Integer> e: classes.entrySet())
				{
					Class klazz = androidAPI.get(e.getKey());
					if (klazz != null)
					{
						if (klazz.fixDeclarationsFromVM())
							System.out.println(klazz.getName());
						else
							System.err.println(klazz.getName());
					}
				}
			}
			/*for (Map.Entry<String, ClassInfo> e: additional.entrySet())
			{
				classes.put(e.getKey(), e.getValue());
			}
			Vector<java.lang.Class> _classes = new Vector<java.lang.Class>();
			ClassInfo.sort(_classes, classes);
			int curr = 0;
			for (java.lang.Class c: _classes)
			{
				reflect.printClass(c, ++curr, _classes.size());
			}*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
