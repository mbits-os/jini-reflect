package reflect.cpp;

import reflect.android.api.Class;

public class CppWriter {
	private Class m_class;

	public CppWriter(Class klazz) {
		m_class = klazz;
	}

	public void printHeader() {
		String name = m_class.getName();
		int pos = name.lastIndexOf(".");
		if (pos < 0) pos = name.length();
		String pkg = name.substring(0, pos);
		final String guard_macro = "__JINI_" + name.toUpperCase().replace(".", "_") + "_HPP__";
		println("#ifndef " + guard_macro);
		println("#define " + guard_macro);
		println();
		//includes
		println("namespace " + pkg.replace(".", " { namespace ") + " {");
		final int depth = pkg.split("\\.").length;
		for (int i = 0; i < depth; ++i)
			print("}");
		println(" // " + pkg.replace(".", "::"));
		println();
		println("#endif // " + guard_macro);
	}

	private void println() {
		System.out.println();
	}
	private void println(String string) {
		System.out.println(string);
	}
	private void print(String string) {
		System.out.print(string);
	}
}
