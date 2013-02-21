import java.io.File;
import java.io.IOException;
import java.util.List;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import reflect.CodeExceptions;
import reflect.android.api.Class;
import reflect.android.api.Classes;
import reflect.android.api.Method;
import reflect.android.api.Param;
import reflect.android.api.Property;
import reflect.cpp.CppWriter;

public class Reflect {

	private File m_inc;
	private File m_src;

	public Reflect(File inc, File src) {
		m_inc = inc;
		m_src = src;
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
		writer.printHeader(m_inc);
		writer.printSource(m_src);
	}

	public static void main(String[] args)
	{

		CodeExceptions.readExceptions();

		ArgumentParser parser = ArgumentParsers.newArgumentParser("Reflect")
				.defaultHelp(true)
				.description("Create JINI bindings for given class(es). CLASS can be in form java.lang.Class to generate binding for one class only or java.lang.* to generate it for all java.lang classes (but not java.class.reflect classes). When a subclass is provided ($ is present), it will be changed to the outer-most class.")
				.epilog("Either --all or at least one class is needed.");
		parser.addArgument("-a", "--android")
				.metavar("api")
				.type(Integer.class)
				.dest("targetAPI")
				.required(true)
				.help("Android API Level (e.g. -a 17 for Android 4.2)");
		parser.addArgument("--dest")
				.metavar("dir")
				.type(File.class)
				.setDefault(new File("./code"))
				.dest("dest")
				.help("The output directory");
		parser.addArgument("--inc")
				.metavar("dir")
				.type(String.class)
				.dest("inc")
				.help("The output dir for .hpp files (default: $dest/inc)");
		parser.addArgument("--src")
				.metavar("dir")
				.type(String.class)
				.dest("src")
				.help("The output dir for .cpp files (default: $dest/src)");
		parser.addArgument("--parents")
				.action(Arguments.storeTrue())
				.setDefault(false)
				.help("Generate classes for the extends and implements classes");
		parser.addArgument("--all-deps")
				.action(Arguments.storeTrue())
				.setDefault(false)
				.help("Generate classes for the extends, implements, field and parameter classes");
		parser.addArgument("--all")
				.action(Arguments.storeTrue())
				.setDefault(false)
				.help("Generates bindings for all the classes in the API");
		parser.addArgument("files")
				.metavar("CLASS")
				.type(String.class)
				.nargs("*")
				.help("Class to generate binding for");

		Namespace ns = null;
		try {
			ns = parser.parseArgs(args);
			if (!ns.getBoolean("all") && ns.getList("files").size() == 0)
				throw new ArgumentParserException("Either --all or at least one class is needed.", parser);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		}
		File inc = (File)ns.get("inc");
		File src = (File)ns.get("src");
		File dest = (File)ns.get("dest");
		if (inc == null) inc = new File(dest, "inc");
		if (src == null) src = new File(dest, "src");

		try {		
			System.out.print("API Level: "); System.out.println(ns.getInt("targetAPI"));
			System.out.print("Headers:   "); System.out.println(inc.getCanonicalPath());
			System.out.print("Sources:   "); System.out.println(src.getCanonicalPath());
			System.out.print("Mode:      "); System.out.println(
					ns.getBoolean("all") ? "Entire API" :
						ns.getBoolean("all_deps") ? "All dependencies" : 
							ns.getBoolean("parents") ? "Parents" : "Only classes");
			System.out.print("Classes:   "); System.out.println(ns.getList("files"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		List<String> classes = ns.<String>getList("files");
		int sdk = ns.getInt("targetAPI");

		try {
			if (!Classes.setTargetApi(sdk))
			{
				System.err.println("Could not initiate android-" + sdk + " environment");
				return;
			}

			final Reflect reflect = new Reflect(inc, src);

			if (ns.getBoolean("all"))
			{
				classes.clear();
				String[] api_classes = Classes.classNames();
				for (String clazz: api_classes)
					if (clazz.indexOf('$') == -1)
						classes.add(clazz);
			}
			else if (ns.getBoolean("parents") || ns.getBoolean("all_deps"))
			{
				boolean all = ns.getBoolean("all_deps");
				int i = 0;
				while (i < classes.size())
					addClass(classes, Classes.forName(classes.get(i++)), all);
			}

			int curr = 0;
			for (String s: classes)
			{
				reflect.printClass(s, ++curr, classes.size());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void addSignature(List<String> classes, String type)
	{
		int array = 0;
		while (array < type.length() && type.charAt(array) == '[')
			++array;

		if (array < type.length() && type.charAt(array) == 'L')
			addClass(classes, type.substring(array + 1, type.length() - 1));
	}

	static void addClass(List<String> classes, String className)
	{
		int pos = className.indexOf('$');
		if (pos != -1)
			className = className.substring(0, pos);

		for (String s: classes)
			if (s.equals(className)) return;

		classes.add(className);
	}

	static void addClass(List<String> classes, Class c, boolean all) {
		if (c == null) return;
		final String sup = c.getSuper();
		final String[] ifaces = c.getInterfaces();
		if (sup != null) addClass(classes, sup);
		for (String iface: ifaces)
			addClass(classes, iface);

		if (all) {
			for (Property prop: c.getProperties())
				addSignature(classes, prop.getSignature());
			for (Method method: c.getMethods()) {
				addSignature(classes, method.getReturnType());
				for (Param param: method.getParameterTypes())
					addSignature(classes, param.getSignature());
			}
		}

		for (Class sub: c.getClasses())
			addClass(classes, sub, all);
	}
}
