package reflect.patches;

import java.io.PrintStream;

import reflect.android.api.Class;

public class StringPatch extends Patch {

	@Override
	public void constructorDeclarations(PrintStream out, String indent, Class clazz) {
		if (!clazz.getName().equals("java.lang.String"))
			return;
		out.print(indent); out.println("String(const char* utf8)");
		out.print(indent); out.println(": jini::Object<String>(Env()->NewStringUTF(utf8))");
		out.print(indent); out.println(", Object(jni::Object<String>::m_this)");
		out.print(indent); out.println(", java::io::Serializable(jni::Object<String>::m_this)");
		out.print(indent); out.println(", CharSequence(jni::Object<String>::m_this)");
		out.print(indent); out.println(", Comparable(jni::Object<String>::m_this)");
		out.print(indent); out.println("{}");
	}

}
