/*
 * Copyright (C) 2013 midnightBITS
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
 
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import reflect.StringPatch;
import reflect.api.Class;
import reflect.api.Classes;
import reflect.api.Method;
import reflect.api.Param;
import reflect.api.Property;
import reflect.cpp.CppWriter;
import reflect.patches.Patches;
import reflect.plugin.ReflectPlugins;

/**
 * @hide
 */
public class Reflect {

	private File m_inc;
	private File m_src;
	private boolean m_utf8;
	private List<String> m_classes;
	private List<File> m_files = new LinkedList<File>();

	public Reflect(File inc, File src, boolean utf8) {
		m_inc = inc;
		m_src = src;
		m_utf8 = utf8;
		m_classes = null;
	}

	private void setKnownClasses(List<String> classes) {
		m_classes  = classes;
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
		CppWriter writer = new CppWriter(klazz, m_classes, m_utf8);

		writer.printHeader(m_inc, m_files);
		writer.printSource(m_src);
	}

	private static String relative(File dir, File path) {
		final String regexp = File.separator.replace("\\", "\\\\").replace(".", "\\.");
		String[] src = dir.toString().split(regexp);
		String[] dst = path.toString().split(regexp);
		int pos = 0;
		while (pos < src.length && pos < dst.length - 1 && src[pos].equals(dst[pos]))
			++pos;

		StringBuilder sb = new StringBuilder();
		for (int i = pos; i < src.length; ++i)
			sb.append("../");
		for (int i = pos; i < dst.length - 1; ++i)
			sb.append(dst[i] + "/");
		sb.append(dst[dst.length-1]);
		return sb.toString();
		
	}
	public void fileList(File output) {
		PrintStream out = null;
		try {
			final File dir = output.getCanonicalFile().getParentFile();

			out = new PrintStream(output, "UTF-8");
			for (File f: m_files)
				out.println(relative(dir, f));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			out.close();
		}
	}

	private static File getAppDir() {
		String path = Reflect.class.getResource("/" + Reflect.class.getName() + ".class").toString();
		while (path.startsWith("jar:")) {
			int pos = path.lastIndexOf("!");
			if (pos == -1) pos = path.length();
			path = path.substring(4, pos);
		}
		if (path.startsWith("file:/")) {
			path = path.substring(6);
		}
		try {
			return new File(path).getCanonicalFile().getParentFile();
		} catch (IOException e) {
			e.printStackTrace();
		};
		return null;
	}
	public static void main(String[] args)
	{
		final File appDir = getAppDir();
		if (appDir == null)
			return;

		ReflectPlugins.loadPlugins(new File(appDir, "plugins"));

		//CodeExceptions.readExceptions(appDir);
		Patches.put("java.lang.String", new StringPatch());

		ArgumentParser parser = ArgumentParsers.newArgumentParser("Reflect")
				.defaultHelp(true)
				.description("Create JINI bindings for given class(es). CLASS can be in form java.lang.Class to generate binding for one class only or java.lang.* to generate it for all java.lang classes (but not java.class.reflect classes). When a subclass is provided ($ is present), it will be changed to the outer-most class.")
				.epilog("Either --all or at least one class is needed.");

		ReflectPlugins.onAddArguments(parser);

		parser.addArgument("-8", "--utf8")
				.action(Arguments.storeTrue())
				.setDefault(false)
				.help("replace java.lang.String with const char*");
		parser.addArgument("--dest")
				.metavar("DIR")
				.type(File.class)
				.setDefault(new File("./code"))
				.dest("dest")
				.help("the output directory");
		parser.addArgument("--inc")
				.metavar("DIR")
				.type(File.class)
				.dest("inc")
				.help("the output dir for .hpp files (default: $dest" + File.separator + "inc)");
		parser.addArgument("--src")
				.metavar("DIR")
				.type(File.class)
				.dest("src")
				.help("the output dir for .cpp files (default: $dest" + File.separator + "src)");
		parser.addArgument("--preserve-refs")
				.action(Arguments.storeTrue())
				.setDefault(false)
				.dest("refs")
				.help("preserve methods and properties, whose types are not builtin, in java.lang package nor on the list of classes");
		parser.addArgument("--parents")
				.action(Arguments.storeTrue())
				.setDefault(false)
				.help("generate classes for the superclass and interfaces classes");
		parser.addArgument("--all-deps")
				.action(Arguments.storeTrue())
				.setDefault(false)
				.help("Generate classes for the superclass, interfaces, field and parameter classes; implies --parent and --preserve-refs");
		parser.addArgument("--all")
				.action(Arguments.storeTrue())
				.setDefault(false)
				.help("Generate bindings for all the classes in the API; implies --all-deps, --parent and --preserve-refs");
		parser.addArgument("--file-list")
				.metavar("FILE")
				.type(File.class)
				.help("output list of generated files for further processing");
		parser.addArgument("classes")
				.metavar("CLASS")
				.type(String.class)
				.nargs("*")
				.help("class and/or package to generate bindings for");

		Namespace ns = null;
		try {
			ns = parser.parseArgs(args);
			if (!ns.getBoolean("all") && ns.getList("classes").size() == 0)
				throw new ArgumentParserException("Either --all or at least one class is needed.", parser);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		}
		File inc = (File)ns.get("inc");
		File src = (File)ns.get("src");
		File dest = (File)ns.get("dest");
		File files = (File)ns.get("file_list");
		boolean utf8 = ns.getBoolean("utf8");
		boolean all = ns.getBoolean("all");
		boolean deps = ns.getBoolean("all_deps");
		boolean parents = ns.getBoolean("parents");
		boolean refs = ns.getBoolean("refs");
		final List<String> list = ns.getList("classes");
		List<String> classes = new LinkedList<String>();

		if (inc == null) inc = new File(dest, "inc");
		if (src == null) src = new File(dest, "src");
		if (all) deps = true; // deps is a subset of deps
		if (deps) parents = true; // parents is a subset of deps
		if (deps) refs = true; //if deps (or all) is present, we do not want to look for limits, as there should be none

		try {		
			System.out.print("API Level : "); System.out.println(ns.getInt("targetAPI"));
			System.out.print("Headers   : "); System.out.println(inc.getCanonicalPath());
			System.out.print("Sources   : "); System.out.println(src.getCanonicalPath());
			System.out.print("Strings   : "); System.out.println(utf8 ? "const char* utf8" : "java.lang.String");
			System.out.print("Mode      : "); System.out.println(all ? "Entire API" : deps ? "All dependencies" : parents ? "Parents" : "Classes");
			System.out.print("Unk. refs : "); System.out.println(refs ? "preserved" : "methods removed");
			System.out.print("Classes   : "); if (all) System.out.println("all"); else System.out.println(list);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			ReflectPlugins.onReadArguments(ns);

			if (!Classes.readApis())
				return;

			final Reflect reflect = new Reflect(inc, src, utf8);

			for (String item: list) {
				if (item.endsWith(".*")) {
					String [] pkg = Classes.packageClasses(item.substring(0, item.length()-2));
					for (String clazz: pkg)
						classes.add(clazz);
					continue;
				}
				classes.add(item);
			}

			if (all)
			{
				classes.clear();
				String[] api_classes = Classes.classNames();
				for (String clazz: api_classes)
					if (clazz.indexOf('$') == -1)
						classes.add(clazz);
			}
			else if (parents) // || deps, see above
			{
				int i = 0;
				while (i < classes.size())
					addClass(classes, Classes.forName(classes.get(i++)), deps);
			}

			int curr = 0;
			if (!refs)
				reflect.setKnownClasses(classes);

			for (String s: classes)
			{
				reflect.printClass(s, ++curr, classes.size());
			}

			if (files != null)
				reflect.fileList(files);
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
