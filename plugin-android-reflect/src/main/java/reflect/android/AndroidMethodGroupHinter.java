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
 
package reflect.android;

import java.util.Vector;

import reflect.api.Class.MethodGroup;
import reflect.api.Method;
import reflect.api.Param;
import reflect.java.MethodGroupHinter;
import reflect.java.MethodHinter;

public class AndroidMethodGroupHinter implements MethodGroupHinter {

	private MethodGroup m_group;

	public AndroidMethodGroupHinter(MethodGroup group) {
		m_group = group;
	}

	public MethodHinter find(String retType, Vector<String> types) {
		for (Method method: m_group.m_methods)
		{
			if (matches(method, retType, types))
				return new AndroidMethodHinter(method);
		}
		return null;
	}

	private boolean matches(String hintedType, String apiType) {
		//System.out.println("       mathing: " + hintedType + " with " + apiType);
		if (hintedType.equals(apiType))
			return true;
		return false;
	}

	private boolean matches(Method method, String retType, Vector<String> types) {
		//System.out.print("    Matching\n      (");
		//for (String s: types) System.out.print(s);
		//System.out.println(")" + retType + "\n      " + method.getSignature());

		Param[] params = method.getParameterTypes();
		if (params.length != types.size())
			return false;

		if (!matches(retType, method.getReturnType()))
			return false;
		for (int i = 0; i < params.length; ++i)
		{
			if (!matches(types.get(i), params[i].getSignature()))
				return false;
		}
		return true;
	}

}
