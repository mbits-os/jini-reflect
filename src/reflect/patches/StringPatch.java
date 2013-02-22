package reflect.patches;

import java.io.PrintStream;

import reflect.android.api.Class;

public class StringPatch extends Patch {

	@Override
	public void onConstructors(PrintStream out, String indent, Class clazz) {
		if (!clazz.getName().equals("java.lang.String"))
			return;
		out.print(indent); out.println("String(const char* utf8)");
		out.print(indent); out.println("\t: jini::Object<String>(Env()->NewStringUTF(utf8))");
		out.print(indent); out.println("\t, Object(jni::Object<String>::m_this)");
		out.print(indent); out.println("\t, java::io::Serializable(jni::Object<String>::m_this)");
		out.print(indent); out.println("\t, CharSequence(jni::Object<String>::m_this)");
		out.print(indent); out.println("\t, Comparable(jni::Object<String>::m_this)");
		out.print(indent); out.println("{}");
	}

}
