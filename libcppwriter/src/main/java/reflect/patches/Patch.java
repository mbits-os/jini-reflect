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
 
package reflect.patches;

import java.io.PrintStream;

import reflect.api.Class;

/**
 * The Patch represents the code changes introduced during
 * the code generation. Each of the callbacks is called during
 * specific code generation phase:
 * 
 * <ul>
 * <li>{@link #onIncludes(PrintStream) onIncludes} is called
 * after all includes, but before writer entered the namespace
 * for the first time.</li>
 * <li>{@link #onNamespaceStart(PrintStream) onNamespaceStart}
 * is called after the writer enters the namespace for the
 * first time.</li>
 * <li>{@link #onConstructors(PrintStream, String, Class) onConstructors}
 * is called between constructors and other members for each class.</li>
 * <li>{@link #onObjectMembers(PrintStream, String, Class) onObjectMembers}
 * is called after all generated members are written out.</li>
 * <li>{@link #onNamespaceEnd(PrintStream) onNamespaceEnd} is called
 * just as the writer is about to leave the namespace for the
 * second time.</li>
 * </ul>
 */
public class Patch {

	/**
	 * @hide
	 */
	public Patch() {}

	/**
	 * Called after printing the JINI includes and before
	 * entering the namespace. Can be used to print additional
	 * include directives.
	 * 
	 * @param out The output to write to
	 */
	public void onIncludes(PrintStream out) {}

	/**
	 * Called after printing the basic constructor.
	 * Used to provide alternative constructors, e.g. <code>java::lang::String(const char* utf8)</code>
	 * in addition to the <code>java::lang::String(jobject _this)</code>.
	 * 
	 * @param out The output to write to
	 * @param indent The indentation used for lines inside the class definition
	 * @param clazz The class this callback is called for. Be aware, that if a class
	 *              has internal classes, this patch will be called for them as well
	 */

	public void onConstructors(PrintStream out, String indent, Class clazz) {}

	/**
	 * Called after all member functions for the Java bindings. Allows to
	 * enhance the API with additional operations.
	 * 
	 * @param out The output to write to
	 * @param indent The indentation used for lines inside the class definition
	 * @param clazz The class this callback is called for. Be aware, that if a class
	 *              has internal classes, this patch will be called for them as well
	 */
	public void onObjectMembers(PrintStream out, String indent, Class clazz) {}

	/**
	 * Called at the first entry into the namespace. Allows to add additional
	 * structures and functions needed by the code introduced with
	 * {@link #onConstructors(PrintStream, String, Class) onConstructors} or
	 * {@link #onObjectMembers(PrintStream, String, Class) onObjectMembers}.
	 * 
	 * @param out The output to write to
	 */
	public void onNamespaceStart(PrintStream out) {}

	/**
	 * Called at the last exit from the namespace. Allows to add code and
	 * structures relying on generated classes and their memebrs.
	 * 
	 * @param out The output to write to
	 */
	public void onNamespaceEnd(PrintStream out) {}
}
