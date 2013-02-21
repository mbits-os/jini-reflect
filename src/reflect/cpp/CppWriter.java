package reflect.cpp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import reflect.android.api.Class;
import reflect.android.api.Class.MethodGroup;
import reflect.android.api.Method;
import reflect.android.api.Method.Type;
import reflect.android.api.Param;
import reflect.android.api.Property;

public class CppWriter {
	private Class m_class;
	private PrintStream m_out;

	public CppWriter(Class klazz) {
		m_class = klazz;
		m_out = System.out;
	}

	public void printSource(File src) {
		final File out = new File(src, m_class.getName().replace(".", "/") + ".cpp");

		try {
			final File abs = out.getCanonicalFile();
			final File dir = abs.getParentFile();
			dir.mkdirs();
			m_out = new PrintStream(out, "UTF-8");
			doPrintSource();
		} catch (FileNotFoundException e) {
		} catch (UnsupportedEncodingException e) {
		} catch (IOException e) {
		} finally {
			if (m_out != System.out)
				m_out.close();
			m_out = System.out;
		}
	}

	private void doPrintSource() {
	}

	public void printHeader(File inc) {
		final File out = new File(inc, m_class.getName().replace(".", "/") + ".hpp");

		try {
			final File abs = out.getCanonicalFile();
			final File dir = abs.getParentFile();
			dir.mkdirs();
			m_out = new PrintStream(out, "UTF-8");
			doPrintHeader();
		} catch (FileNotFoundException e) {
		} catch (UnsupportedEncodingException e) {
		} catch (IOException e) {
		} finally {
			if (m_out != System.out)
				m_out.close();
			m_out = System.out;
		}
	}

	private String relativize(String className, String include)
	{
		String[] src = className.split("\\.");
		String[] dst = include.split("/");
		int pos = 0;
		while (pos < src.length - 1 && pos < dst.length - 1 && src[pos].equals(dst[pos]))
			++pos;
		StringBuilder sb = new StringBuilder();
		for (int i = pos; i < src.length - 1; ++i)
			sb.append("../");
		for (int i = pos; i < dst.length - 1; ++i)
			sb.append(dst[i] + "/");
		sb.append(dst[dst.length-1]);
		return sb.toString();
	}
	private void doPrintHeader() {
		final String name = m_class.getName();
		int pos = name.lastIndexOf(".");
		final int filePos = pos < 0 ? 0 : pos + 1;
		if (pos < 0) pos = name.length();

		String pkg = name.substring(0, pos);
		String filename = name.substring(filePos) + ".hpp";
		final String guard_macro = "__JINI_" + name.toUpperCase().replace(".", "_") + "_HPP__";
		Vector<Class> classes = new Vector<Class>();

		println("/*");
		println(" * " + filename);
		println(" * ");
		println(" * JINI binding for the class " + name);
		println(" * This file was auto-generated. Do not change or all your edit will be overwritten.");
		println(" */");
		println();
		println("#ifndef " + guard_macro);
		println("#define " + guard_macro);
		println();
		println("#include \"jini.hpp\"");
		List<String> includes = getIncludes();
		for (String incl: includes) {
			println("#include \"" + relativize(name, incl) + "\"");
		}
		println();
		namespaceStart(pkg);
		println();
		printObjectDef(m_class, classes, "\t");
		println();
		namespaceEnd(pkg);
		println();

		Collections.reverse(classes);
		
		namespaceStart("jni");
		for (Class clazz: classes) {
			final String _name = clazz.getName().replace(".", "::").replace("$", "::");
			println("\ttemplate <> struct TypeInfo< " + _name + " >: TypeInfo_Object< " + _name + " > {};");
		}
		namespaceEnd("jni");
		println();
		namespaceStart(pkg);
		println();
		for (Class clazz: classes) {
			printObjectClassDef(clazz);
			println();
			printObjectMethodDef(clazz);
			println();
		}
		namespaceEnd(pkg);
		println();
		if (pkg.equals("java.lang"))
		{
			println("using java::lang::" + name.substring(filePos) + ";");
			println();
		}
		println("#endif // " + guard_macro);
	}

	private static interface OnProperty {
		void onProperty(String className, String indent, boolean isStatic, String type, String name);
	}
	
	private void printProps(String indent, Class clazz, OnProperty cb) {
		for (Property prop: clazz.getProperties())
			cb.onProperty(clazz.getName(), indent, prop.isStatic(), prop.getSignature(), prop.getName());
	}

	private static interface OnMethod {
		void onMethod(String className, String simpleName, String indent, Method.Type type, String retType, String name, Param[] pars, int version);
	}
	
	private void printMethods(String indent, Class clazz, String simpleName, OnMethod cb) {
		for (MethodGroup group: clazz.getGroups())
		{
			if (group.m_methods.size() == 1) {
				Method meth = group.m_methods.get(0);
				cb.onMethod(
						clazz.getName(), simpleName, indent, meth.getType(),
						meth.getReturnType(), meth.getName(), meth.getParameterTypes(),
						0);
				continue;
			}
			int ver = 0;
			for (Method meth: group.m_methods) {
				cb.onMethod(
						clazz.getName(), simpleName, indent, meth.getType(),
						meth.getReturnType(), meth.getName(), meth.getParameterTypes(),
						++ver);
			}
		}
	}

	private static interface OnParameter {
		void onParameter(String className, String sep, String type, String name);
	}
	
	private void printParameters(String sep, String className, Param[] pars, OnParameter cb) {
		boolean first = true;
		for (Param par: pars)
		{
			final String _sep = first ? "" : sep;
			first = false;
			cb.onParameter(className, _sep, par.getSignature(), par.getName());
		}
	}

	private void printObjectDef(Class clazz, Vector<Class> classes, String indent) {
		classes.add(clazz);

		final String name = clazz.getName();
		final String indent2 = indent + "\t";

		int pos = name.lastIndexOf(".");
		pos = pos < 0 ? 0 : pos + 1;
		int dollar = name.lastIndexOf("$");
		if (dollar > pos) pos = dollar + 1;
		String simpleName = name.substring(pos);

		print(indent); println("class " + simpleName);
		print(indent); println("\t: private jni::Object<" + simpleName + ">");
		final String superClass = clazz.getSuper();
		if (superClass != null) {
			print(indent); println("\t, public " + getClass(superClass, name));
		}
		for (String iface: clazz.getInterfaces()) {
			print(indent); println("\t, public " + getClass(iface, name));
		}
		print(indent); println("{");
		print(indent); println("public:");
		for (Class sub: clazz.getClasses()) {
			printObjectDef(sub, classes, indent2);
			println();
		}
		print(indent2); println("DECLARE_GETCLASS();");
		println();
		print(indent2); println(simpleName + "(jobject _this = NULL)");
		print(indent2); println("\t: jni::Object<" + simpleName + ">(_this)");
		if (superClass != null) {
			print(indent2); println("\t, " + getClass(superClass, name) + "(_this)");
		}
		for (String iface: clazz.getInterfaces()) {
			print(indent2); println("\t, " + getClass(iface, name) + "(_this)");
		}
		print(indent2); println("{}");
		println();
		printProps(indent2, clazz, new OnProperty() {
			public void onProperty(String className, String indent, boolean isStatic, String type, String name) {
				print(indent);
				//print("inline ");
				if (isStatic) print("static ");
				print(getType(type, className));
				print(" ");
				print(name);
				println("();");
			}
		});
		printMethods(indent2, clazz, simpleName, new OnMethod() {
			public void onMethod(String className, String simpleName, String indent, Method.Type type, String retType, String name, Param[] pars, int version) {
				print(indent);
				//print("inline ");
				if (type != Method.Type.METHOD) print("static ");
				if (type == Method.Type.CONSTRUCTOR)
				{
					print(simpleName);
					print(" jini_newObject");
				}
				else
				{
					print(getType(retType, className));
					print(" ");
					print(name);
				}
				print("(");
				printParameters(", ", className, pars, new OnParameter() {
					public void onParameter(String className, String sep, String type, String name) {
						print(sep);
						print(getType(type, className));
						print(" ");
						print(name);
					}
				});
				println(");");
			}
		});
		print(indent); println("};");
	}

	static class SemiColonHelper {
		boolean first = true;
		String produce() {
			if (!first)
				return ", ";
			first = false;
			return ": ";
		}
	}
	
	private class ConstructBindings {
		SemiColonHelper m_sch = new SemiColonHelper();
		void printBindings(Class clazz) {
			println("\t\tClass()");
			
			printProps("\t\t", clazz, new OnProperty() {
				public void onProperty(String className, String indent, boolean isStatic, String type, String name) {
					print(indent);
					print(m_sch.produce());
					print("m_");
					print(name);
					print("(\"");
					print(name);
					println("\")");
				}
			});

			final String name = clazz.getName();
			int pos = name.lastIndexOf(".");
			pos = pos < 0 ? 0 : pos + 1;
			int dollar = name.lastIndexOf("$");
			if (dollar > pos) pos = dollar + 1;
			final String simpleName = name.substring(pos);

			printMethods("\t\t", clazz, simpleName, new OnMethod() {
				public void onMethod(String className, String simpleName, String indent, Type type, String retType, String name, Param[] pars, int version) {
					if (type == Method.Type.CONSTRUCTOR)
						return;

					print(indent);
					print(m_sch.produce());
					print("m_");
					print(name);
					if (version != 0)
						print(String.valueOf(version));
					print("(\"");
					print(name);
					println("\")");
				}
			});
			// bindings
			println("\t\t{}");
		}
	}
	private void printObjectClassDef(Class clazz) {
		final String name = clazz.getName();

		int pos = name.lastIndexOf(".");
		pos = pos < 0 ? 0 : pos + 1;
		final String pkgName = name.substring(pos).replace(".", "::").replace("$", "::");

		int dollar = name.lastIndexOf("$");
		if (dollar > pos) pos = dollar + 1;
		String simpleName = name.substring(pos);

		println("\tclass " + pkgName + "::Class: public jni::Class< " + pkgName + "::Class >");
		println("\t{");
		println("\t\tfriend class " + simpleName + ";");
		printProps("\t\t", clazz, new OnProperty() {
			public void onProperty(String className, String indent, boolean isStatic, String type, String name) {
				print(indent);
				print("jni::");
				if (isStatic) print("Static");
				print("Property< ");
				print(getType(type, className));
				print(" > m_");
				print(name);
				println(";");
			}
		});
		printMethods("\t\t", clazz, simpleName, new OnMethod() {
			public void onMethod(String className, String simpleName, String indent, Method.Type type, String retType, String name, Param[] pars, int version) {
				print(indent);
				print("jni::");
				if (type == Method.Type.CONSTRUCTOR)
				{
					print("Constructor<");
				}
				else
				{
					if (type == Method.Type.STATIC_METHOD)
						print("Static");

					print("Method< ");
					print(getType(retType, className));
					if (pars.length > 0) print(",");
				}
				printParameters(",", className, pars, new OnParameter() {
					public void onParameter(String className, String sep, String type, String name) {
						print(sep);
						print(" " + getType(type, className));
					}
				});
				print(" > m_");
				if (type == Method.Type.CONSTRUCTOR)
					print("ctor");
				else
					print(name);
				if (version != 0)
					print(String.valueOf(version));
				println(";");
			}
		});
		// bindings
		println("\tpublic:");
		new ConstructBindings().printBindings(clazz);
		println();
		println("\t\tDECLARE_JAVA_CLASS(\"" + name.replace(".", "/") + "\")");
		println("\t};");
	}

	private void printObjectMethodDef(Class clazz) {
	}

	private String getType(String signature, String className) {
		int arrays = 0;
		while (signature.charAt(arrays) == '[') ++arrays;
		if (arrays > 1) return "jobjectArray";

		switch(signature.charAt(arrays))
		{
		case 'Z': return arrayed(arrays, "bool");
		case 'B': return arrayed(arrays, "jbyte");
		case 'C': return arrayed(arrays, "jchar");
		case 'S': return arrayed(arrays, "jshort");
		case 'I': return arrayed(arrays, "jint");
		case 'J': return arrayed(arrays, "jlong");
		case 'F': return arrayed(arrays, "jfloat");
		case 'D': return arrayed(arrays, "jdouble");
		case 'V': return "void";
		case 'L':
			return arrayed(arrays,
					getClass(signature.substring(arrays + 1, signature.length()-1), className)
					);
		}
		return signature;
	}

	private String getClass(String signature, String className) {
		return innerGetClass(signature, className).replace(".", "::").replace("$", "::");
	}
	private String innerGetClass(String signature, String className) {
		int pkgPos = className.lastIndexOf('.');

		// a.b.c.D$E @ a.b.c.D$E --> E
		// a.b.c.D @ a.b.c.D --> D
		if (signature.equals(className))
		{
			int pos = signature.lastIndexOf('$');
			if ( pos >= 0) return signature.substring(pos + 1);
			pos = signature.lastIndexOf('.');
			if ( pos >= 0) return signature.substring(pos + 1);
			return signature;
		}

		// a.b.c.D$E$F @ a.b.c.D --> E$F
		if (signature.startsWith(className + "$"))
			return signature.substring(className.length() + 1);

		// a.b.c.D$E$F @ a.b.c.X --> D$E$F
		final String pkg = className.substring(0, pkgPos + 1);
		if (signature.startsWith(pkg))
			return signature.substring(pkgPos + 1);

		// a.b.c.D$E$F @ a.b.y.X --> c.D$E$F
		// TODO
		
		// java.lang.D$E @ a.b.c.X --> D$E
		if (signature.startsWith("java.lang."))
			return signature.substring(10);

		return signature;
	}

	private void namespaceStart(String ns) {
		println("namespace " + ns.replace(".", " { namespace ") + " {");
	}

	private void namespaceEnd(String ns) {
		final int depth = ns.split("\\.").length;
		println(repeat("}", depth) + " // " + ns.replace(".", "::"));
	}

	private static String arrayed(int arrays, String inner) {
		return repeat("jni::Array< ", arrays) + inner + repeat(" >", arrays);
	}

	private static String repeat(String s, int n) {
		if (n == 0) return "";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; ++i)
			sb.append(s);
		return sb.toString();
	}
	private void println() {
		m_out.println();
	}
	private void println(String string) {
		m_out.println(string);
	}
	private void print(String string) {
		m_out.print(string);
	}

	private List<String> getIncludes()
	{
		List<String> out = new LinkedList<String>();

		getIncludes(out, m_class);

		Collections.sort(out);

		return out;
	}

	private void getIncludes(List<String> classes, Class c) {
		final String sup = c.getSuper();
		final String[] ifaces = c.getInterfaces();
		if (sup != null) addClass(classes, sup);
		for (String iface: ifaces)
			addClass(classes, iface);

		for (Property prop: c.getProperties())
			addSignature(classes, prop.getSignature());
		for (Method method: c.getMethods()) {
			addSignature(classes, method.getReturnType());
			for (Param param: method.getParameterTypes())
				addSignature(classes, param.getSignature());
		}

		for (Class sub: c.getClasses()) {
			if (sub == null) continue;
			getIncludes(classes, sub);
		}
	}

	private void addSignature(List<String> classes, String type)
	{
		int array = 0;
		while (array < type.length() && type.charAt(array) == '[')
			++array;

		if (array < type.length() && type.charAt(array) == 'L')
			addClass(classes, type.substring(array + 1, type.length() - 1));
	}

	private void addClass(List<String> classes, String className)
	{
		int pos = className.indexOf('$');
		if (pos != -1)
			className = className.substring(0, pos);

		if (m_class.getName().equals(className))
			return;

		className = className.replace(".", "/") + ".hpp";

		for (String s: classes)
			if (s.equals(className)) return;

		classes.add(className);
	}
}
