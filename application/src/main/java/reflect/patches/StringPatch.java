package reflect.patches;

import java.io.PrintStream;

import reflect.api.Class;

/**
 * Adds <code>const char*</code> and <code>std::string</code> conversion
 * constructors.
 */
public class StringPatch extends Patch {

	/**
	 * @hide
	 */
	public StringPatch() {}

	/**
	 * Adds <code>&lt;string&gt;</code> to the list of includes.
	 * @see Patch#onIncludes(PrintStream)
	 */
	@Override public void onIncludes(PrintStream out) {
		out.println("#include <string>");
	}

	/**
	 * Adds the conversion constructors for <code>const char*</code> and
	 * <code>std::string</code>. Uses JNIEnv::NewStringUTF, which creates
	 * <code>jstring</code>s from UTF8 C-style null-terminated strings. 
	 * @see Patch#onConstructors(PrintStream, String, Class)
	 */
	@Override public void onConstructors(PrintStream out, String indent, Class clazz) {
		if (!clazz.getName().equals("java.lang.String"))
			return;
		writeConstructor(out, indent, "const char*", "");
		writeConstructor(out, indent, "const std::string&", ".c_str()");
	}

	private void writeConstructor(PrintStream out, String indent, String type, String accessor) {
		out.print(indent); out.println("String(" + type + " utf8)");
		out.print(indent); out.println("\t: jini::Object<String>(Env()->NewStringUTF(utf8" + accessor + "))");
		out.print(indent); out.println("\t, Object(jni::Object<String>::m_this)");
		out.print(indent); out.println("\t, java::io::Serializable(jni::Object<String>::m_this)");
		out.print(indent); out.println("\t, CharSequence(jni::Object<String>::m_this)");
		out.print(indent); out.println("\t, Comparable(jni::Object<String>::m_this)");
		out.print(indent); out.println("{}");
	}

}
