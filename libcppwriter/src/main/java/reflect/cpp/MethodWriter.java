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
import reflect.api.Method;
import reflect.api.Param;

public abstract class MethodWriter extends TypeUtils {
	private PrintStream out;
	private Class clazz;
	private Template tmplt = new Template();
	private String indent;
	private Param[] pars;
	protected Method.Type type;
	protected boolean isVoid;

	private void onMethod(PrintStream _out, Class _clazz, String _ns_dummy, String _indent, Method _meth, int _version) {
		String name;
		String var;
		String classRetType;
		String nsRetType;
		String rawRetType;

		out = _out;
		clazz = _clazz;
		indent = _indent;
		tmplt.clear();
		tmplt.setHost(this);

		if (_meth.getType() == Method.Type.CONSTRUCTOR) {
			name = "jini_newObject";
			var = "m_ctor";
			rawRetType = clazz.getName();
		} else {
			name = _meth.getName();
			var = "m_" + name;
			rawRetType = _meth.getReturnType();
		}
		classRetType = j2c(getType(rawRetType, clazz.getName()));
		nsRetType = j2c(getType(rawRetType, _ns_dummy));

		if (_version != 0)
			var += String.valueOf(_version);

		type = _meth.getType();
		pars = _meth.getParameterTypes();
		isVoid = rawRetType.equals("V");

		tmplt.put("name", name);
		tmplt.put("var", var);
		tmplt.put("rawRetType", rawRetType);
		tmplt.put("classRetType", classRetType);
		tmplt.put("nsRetType", nsRetType);
		tmplt.put("namesAndTypes", NAMES_AND_TYPES);
		tmplt.put("names", NAMES);
		tmplt.put("types", TYPES);

		onMethod();
	}

	abstract void onMethod();

	public void put(String key, String replacement) {
		tmplt.put(key, replacement);
	}

	void templateLine(String tmplt) {
		out.print(indent);
		this.tmplt.println(out, tmplt);
	}

	private static Template.Action NAMES_AND_TYPES = new Template.Action() {
		@Override public void onCall(Object host, StringBuilder sb, String arg) { ((MethodWriter)host).namesAndTypes(sb, arg); }
		@Override public void onCall(Object host, StringBuilder sb) { ((MethodWriter)host).namesAndTypes(sb); }
	};
	private static Template.Action NAMES = new Template.Action() {
		@Override public void onCall(Object host, StringBuilder sb, String arg) { ((MethodWriter)host).names(sb, arg); }
		@Override public void onCall(Object host, StringBuilder sb) { ((MethodWriter)host).names(sb); }
	};
	private static Template.Action TYPES = new Template.Action() {
		@Override public void onCall(Object host, StringBuilder sb, String arg) { ((MethodWriter)host).types(sb, arg); }
		@Override public void onCall(Object host, StringBuilder sb) { ((MethodWriter)host).types(sb); }
	};

	public void namesAndTypes(StringBuilder sb) {
		ParamWriter.printNameAndType(sb, clazz, pars);
	}
	public void names(StringBuilder sb) {
		ParamWriter.printName(sb, clazz, pars);
	}
	public void types(StringBuilder sb) {
		ParamWriter.printType(sb, clazz, pars);
	}

	public void namesAndTypes(StringBuilder sb, String firstArg) {
		ParamWriter.printNameAndType(sb, firstArg, clazz, pars);
	}
	public void names(StringBuilder sb, String firstArg) {
		ParamWriter.printName(sb, firstArg, clazz, pars);
	}
	public void types(StringBuilder sb, String firstArg) {
		ParamWriter.printType(sb, firstArg, clazz, pars);
	}

	private static boolean methodHasOnlyKnownClasses(Method meth) {
		if (!isKnownClassOrBuiltin(meth.getReturnType()))
			return false;

		for (Param param: meth.getParameterTypes())
		{
			if (!isKnownClassOrBuiltin(param.getSignature()))
				return false;
		}
		return true;
	}
	public static void print(PrintStream out, String indent, Class clazz, MethodWriter cb) {
		final String ns_dummy = clazz.getPackage() + ".?";

		for (Class.MethodGroup group: clazz.getGroups())
		{
			if (group.m_methods.size() == 1) {
				Method meth = group.m_methods.get(0);
				if (!methodHasOnlyKnownClasses(meth))
					continue;

				cb.onMethod(out, clazz, ns_dummy, indent, meth, 0);
				continue;
			}
			int ver = 0;
			for (Method meth: group.m_methods) {
				if (!methodHasOnlyKnownClasses(meth))
					continue;

				cb.onMethod(out, clazz, ns_dummy, indent, meth, ++ver);
			}
		}
	}
}
