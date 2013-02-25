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
 
/**
 * Provides patch for the java.lang.String code binding.
 */
package reflect;

import java.io.PrintStream;

import reflect.api.Class;
import reflect.patches.Patch;

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
