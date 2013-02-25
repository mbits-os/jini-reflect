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
 
package reflect.cpp;

import java.io.PrintStream;

import reflect.api.Class;
import reflect.api.Property;

public abstract class PropWriter extends TypeUtils {
	private PrintStream out;
	private Class clazz;
	private Template tmplt = new Template();
	private String indent;
	protected boolean isStatic;

	private void onProperty(PrintStream _out, String _indent, Class _clazz, String _ns_dummy, boolean _isStatic, String type, String name) {
		String var;
		String classRetType;
		String nsRetType;
		String rawRetType;

		out = _out;
		clazz = _clazz;
		indent = _indent;
		isStatic = _isStatic;

		tmplt.clear();
		tmplt.setHost(this);

		var = "m_" + name;
		rawRetType = type;

		classRetType = j2c(getType(rawRetType, clazz.getName()));
		nsRetType = j2c(getType(rawRetType, _ns_dummy));

		tmplt.put("name", name);
		tmplt.put("var", var);
		tmplt.put("rawRetType", rawRetType);
		tmplt.put("classRetType", classRetType);
		tmplt.put("nsRetType", nsRetType);

		onProperty();
	}
	abstract void onProperty();

	public void put(String key, String replacement) {
		tmplt.put(key, replacement);
	}

	void templateLine(String tmplt) {
		out.print(indent);
		this.tmplt.println(out, tmplt);
	}

	public static void print(PrintStream out, String indent, Class clazz, PropWriter cb) {
		final String ns_dummy = clazz.getPackage() + ".?";
		for (Property prop: clazz.getProperties()) {
			if (!isKnownClassOrBuiltin(prop.getSignature()))
				continue;
			cb.onProperty(out, indent, clazz, ns_dummy, prop.isStatic(), prop.getSignature(), prop.getName());
		}
	}
}
