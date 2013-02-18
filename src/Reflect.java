import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import reflect.CodeExceptions;
import reflect.android.api.Class;
import reflect.android.api.Classes;
import reflect.cpp.CppWriter;

public class Reflect {

	public Reflect() {
	}

	private static final String spaces = "                                                            ";
	private void printClass(String clazz, int curr, int max) throws IOException
	{
		Class klazz = Classes.forName(clazz);
		if (klazz == null) return;

		String supah = klazz.getSuper();
		String[] interfaces = klazz.getInterfaces();
		System.out.print("class " + clazz);
		if (supah != null || interfaces.length > 0)
		{
			if (clazz.length() < spaces.length())
				System.out.print(spaces.substring(clazz.length()));
		}
		if (supah != null)
			System.out.print(" extends " + supah);
		if (interfaces.length > 0) {
			System.out.print(" implements ");
			boolean first = true;
			for (String iface: interfaces) {
				if (first) first = false;
				else System.out.print(", ");
				System.out.print(iface);
			}
		}
		System.out.println(" (" + curr + "/" + max + ")");
		CppWriter writer = new CppWriter(klazz);
		writer.printHeader();

		/*for (Property prop: klazz.getProperties()) {
			System.out.print("    ");
			if (prop.isStatic())
				System.out.print("static ");
			System.out.println(prop.getSignature() + " " + prop.getName());
		}
		for (Method meth: klazz.getMethods()) {
			System.out.print("    ");
			if (meth.getType() == Method.Type.STATIC_METHOD)
				System.out.print("static ");
			System.out.print(meth.getReturnType() + " " + meth.getName() + "(");
			boolean first = true;
			for (Param param: meth.getParameterTypes()) {
				if (first) first = false;
				else System.out.print(", ");
				System.out.print(param.getSignature() + " " + param.getName());
			}
			System.out.println(")");
		}

		for (String internal: klazz.getInternals())
			printClass(internal, curr, max);*/
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
			if (!Classes.setTargetApi(Integer.valueOf(sdk)))
			{
				System.err.println("Could not initiate android-" + sdk + " environment");
				return;
			}

			final Reflect reflect = new Reflect();

			if (performTests)
			{
				classes.clear();
				String[] api_classes = Classes.classNames();
				for (String clazz: api_classes)
					classes.put(clazz, 0);
			}

			int curr = 0;
			for (Map.Entry<String, Integer> e: classes.entrySet())
			{
				reflect.printClass(e.getKey(), ++curr, classes.size());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
