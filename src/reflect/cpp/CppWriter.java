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
import reflect.android.api.Method;
import reflect.android.api.Param;
import reflect.android.api.Property;
import reflect.patches.Patch;
import reflect.patches.Patches;

public class CppWriter extends TypeUtils {
	private Class m_class;
	private PrintStream m_out;
	private Patch m_patch;

	public CppWriter(Class klazz, List<String> classes) {
		m_class = klazz;
		m_out = System.out;
		s_classes = classes;
		m_patch = Patches.getPatch(m_class.getName());
	}

	public void printSource(File src) {
		/*
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
		*/
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

	/*private String relativize(String className, String include)
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
	}*/

	private void doPrintHeader() {
		final String name = m_class.getName();

		String pkg = m_class.getPackage();
		String filename = m_class.getSimpleName() + ".hpp";
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
			//println("#include \"" + relativize(name, incl) + "\"");
			println("#include \"" + incl + "\"");
		}
		m_patch.onIncludes(m_out);
		println();
		namespaceStart(pkg);
		println();
		m_patch.onNamespaceStart(m_out);
		printObjectDef(m_class, classes, "\t");
		println();
		namespaceEnd(pkg);
		println();

		Collections.reverse(classes);
		
		namespaceStart("jni");
		for (Class clazz: classes) {
			final String _name = j2c(clazz.getName());
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
		m_patch.onNamespaceEnd(m_out);
		namespaceEnd(pkg);
		println();
		if (pkg.equals("java.lang"))
		{
			println("using java::lang::" + m_class.getSimpleName() + ";");
			println();
		}
		println("#endif // " + guard_macro);
	}

	private void printObjectDef(Class clazz, Vector<Class> classes, String indent) {
		classes.add(clazz);

		final String name = clazz.getName();
		final String indent2 = indent + "\t";

		String simpleName = clazz.getSimpleName();

		print(indent); println("class " + simpleName);
		print(indent); println("\t: private jni::Object<" + simpleName + ">");
		final String superClass = clazz.getSuper();
		if (superClass != null && isKnownClass(superClass)) {
			print(indent); println("\t, public " + getClass(superClass, name));
		}
		for (String iface: clazz.getInterfaces()) {
			if (!isKnownClass(iface)) continue;
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
		if (superClass != null && isKnownClass(superClass)) {
			print(indent2); println("\t, " + getClass(superClass, name) + "(_this)");
		}
		for (String iface: clazz.getInterfaces()) {
			if (!isKnownClass(iface)) continue;
			print(indent2); println("\t, " + getClass(iface, name) + "(_this)");
		}
		print(indent2); println("{}");
		m_patch.onConstructors(m_out, indent2, clazz);
		println();
		PropWriter.print(m_out, indent2, clazz, new PropWriter() {
			@Override
			void onProperty(PrintStream out, String indent, Class clazz, boolean isStatic, String type, String name) {
				out.print(indent);
				out.print("inline ");
				if (isStatic) out.print("static ");
				out.print(getType(type, clazz.getName()));
				out.print(" ");
				out.print(name);
				out.println("();");
			}
		});
		MethodWriter.print(m_out, indent2, clazz, new MethodWriter() {
			@Override
			void onMethod() {
				put("static", type != Method.Type.METHOD ? "static " : "");

				templateLine("inline $static$classRetType $name($namesAndTypes);");
			}
		});
		m_patch.onObjectMembers(m_out, indent2, clazz);
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
			
			PropWriter.print(m_out, "\t\t", clazz, new PropWriter() {
				@Override
				void onProperty(PrintStream out, String indent, Class clazz, boolean isStatic, String type, String name) {
					out.print(indent);
					out.print(m_sch.produce());
					out.print("m_");
					out.print(name);
					out.print("(\"");
					out.print(name);
					out.println("\")");
				}
			});

			MethodWriter.print(m_out, "\t\t", clazz, new MethodWriter() {
				@Override
				void onMethod() {
					if (type == Method.Type.CONSTRUCTOR)
						return;

					put("helper", m_sch.produce());
					templateLine("$helper$var(\"$name\")");
				}
			});
			// bindings
			println("\t\t{}");
		}
	}
	private void printObjectClassDef(Class clazz) {
		final String name = clazz.getName();
		final String pkgName = j2c(clazz.getOuterName());
		final String simpleName = clazz.getSimpleName();

		println("\tclass " + pkgName + "::Class: public jni::Class< " + pkgName + "::Class >");
		println("\t{");
		println("\t\tfriend class " + simpleName + ";");
		PropWriter.print(m_out, "\t\t", clazz, new PropWriter() {
			@Override
			void onProperty(PrintStream out, String indent, Class clazz, boolean isStatic, String type, String name) {
				out.print(indent);
				out.print("jni::");
				if (isStatic) out.print("Static");
				out.print("Property< ");
				out.print(getType(type, clazz.getName()));
				out.print(" > m_");
				out.print(name);
				out.println(";");
			}
		});
		MethodWriter.print(m_out, "\t\t", clazz, new MethodWriter() {
			@Override
			void onMethod() {
				final String Type;
				if (type == Method.Type.CONSTRUCTOR)
					Type = "Constructor";
				else
					if (type == Method.Type.STATIC_METHOD)
						Type = "StaticMethod";
					else
						Type = "Method";
				put("Type", Type);

				templateLine("jni::$Type< ${types: $classRetType} > $var;");
			}
		});
		println();
		PropWriter.print(m_out, "\t\t", clazz, new PropWriter() {
			@Override
			void onProperty(PrintStream out, String indent, Class clazz, boolean isStatic, String type, String name) {
				out.print(indent);
				out.print("inline ");
				out.print(getType(type, clazz.getName()));
				out.print(" ");
				out.print(name);
				out.print("(");
				if (!isStatic) out.print("jobject thiz");
				out.println(");");
			}
		});
		MethodWriter.print(m_out, "\t\t", clazz, new MethodWriter() {
			@Override
			void onMethod() {
				put("jobject", type == Method.Type.METHOD ? "jobject thiz" : "");

				templateLine("inline $classRetType $name(${namesAndTypes: $jobject});");
			}
		});
		println("\tpublic:");
		new ConstructBindings().printBindings(clazz);
		println();
		println("\t\tDECLARE_JAVA_CLASS(\"" + name.replace(".", "/") + "\")");
		println("\t};");
	}

	private class TypePrinter {
		Class m_clazz;
		String m_dummy;
		TypePrinter(Class clazz) {
			m_clazz = clazz;
			m_dummy = clazz.getPackage() + ".?";
		}
		public void printObjectProps() {
			PropWriter.print(m_out, "\t", m_clazz, new PropWriter() {
				@Override
				void onProperty(PrintStream out, String indent, Class clazz, boolean isStatic, String type, String name) {
					out.print(indent);
					out.print("inline ");
					out.print(getType(type, m_dummy));
					out.print(" ");
					out.print(j2c(m_clazz.getOuterName()));
					out.print("::");
					out.print(name);
					out.print("() { return getClass().");
					out.print(name);
					out.print("(");
					if (!isStatic) {
						out.print("m_this");
					}
					out.println("); }");
				}
			});
		}
		public void printObjectMethods() {
			MethodWriter.print(m_out, "\t", m_clazz, new MethodWriter() {
				@Override
				void onMethod() {
					put("return", isVoid ? "" : "return ");
					put("thiz", type == Method.Type.METHOD ? "m_this" : "");
					put("outerName", j2c(m_clazz.getOuterName()));

					templateLine("inline $nsRetType $outerName::$name($namesAndTypes) { ${return}getClass().$name(${names: $thiz}); }");
				}
			});
		}
		public void printClassProps() {
			PropWriter.print(m_out, "\t", m_clazz, new PropWriter() {
				@Override
				void onProperty(PrintStream out, String indent, Class clazz, boolean isStatic, String type, String name) {
					out.print(indent);
					out.print("inline ");
					out.print(getType(type, m_dummy));
					out.print(" ");
					out.print(j2c(m_clazz.getOuterName()));
					out.print("::Class::");
					out.print(name);
					out.print("(");
					if (!isStatic) out.print("jobject thiz");
					out.print(") { return m_");
					out.print(name);
					out.print("(m_class");
					if (!isStatic) {
						out.print(", thiz");
					}
					out.println("); }");
				}
			});
		}
		public void printClassMethods() {
			MethodWriter.print(m_out, "\t", m_clazz, new MethodWriter() {
				@Override
				void onMethod() {
					put("return", isVoid ? "" : "return ");
					put("jobject", type == Method.Type.METHOD ? "jobject thiz" : "");
					put("thiz", type == Method.Type.METHOD ? "thiz" : "m_class");
					put("outerName", j2c(m_clazz.getOuterName()));

					templateLine("inline $nsRetType $outerName::Class::$name(${namesAndTypes: $jobject}) { $return$var(${names: $thiz}); }");
				}
			});
		}
	}
	private void printObjectMethodDef(Class clazz) {
		//final String name = clazz.getName();
		final String pkgName = j2c(clazz.getOuterName());
		//final String simpleName = clazz.getSimpleName();

		TypePrinter tp = new TypePrinter(clazz);
		println("\tDEFINE_GETCLASS(" + pkgName + ");");
		println();
		tp.printObjectProps();
		tp.printObjectMethods();
		println();
		tp.printClassProps();
		tp.printClassMethods();
	}

	private void namespaceStart(String ns) {
		println("namespace " + ns.replace(".", " { namespace ") + " {");
	}

	private void namespaceEnd(String ns) {
		final int depth = ns.split("\\.").length;
		println(repeat("}", depth) + " // " + j2c(ns));
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
		if (!isKnownClass(className))
			return;

		className = className.replace(".", "/") + ".hpp";

		for (String s: classes)
			if (s.equals(className)) return;

		classes.add(className);
	}
}
