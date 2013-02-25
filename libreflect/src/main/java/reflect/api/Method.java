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
 
package reflect.api;

import java.util.Vector;

public class Method extends Artifact {
	public enum Type { CONSTRUCTOR, METHOD, STATIC_METHOD };

	private String m_name;
	private Param[] m_params = null;
	private String m_return = null;
	private Type m_type;

	public Method(int since, String name, String signature) {
		super(since, signature);
		m_name = name;
		m_type = name.equals("<init>") ? Type.CONSTRUCTOR : Type.METHOD;
		breakSignature();
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Method))
			return false;
		Method m = (Method)o;
		return m_name.equals(m.m_name) &&
				getSignature().equals(m.getSignature());
	}

	public String toString() { return m_return + " " + getName() + " " + m_params.toString(); }

	private void breakSignature() {
		final String sig = getSignature();
		final char[] signature = new char[sig.length()-1];
		final Vector<Param> params = new Vector<Param>();
		
		sig.getChars(1, sig.length(), signature, 0);
		int paramStart = 0;
		int paramEnd = 0;
		int position = 0;

		while (paramEnd < signature.length) {
			switch(signature[paramEnd]) {
			case ')':
				++paramEnd;
				if (paramEnd < signature.length)
					m_return = new String(signature, paramEnd, signature.length - paramEnd);
				else
					m_return = "V";
				paramEnd = signature.length;
				break;
			case '[':
				break;
			case 'L':
				while (paramEnd < signature.length && signature[paramEnd] != ';')
					++paramEnd;
				if (paramEnd == signature.length)
					throw new RuntimeException("Error in method signature: " + getName() + sig);
				//fall-through:
			default:
				final String type = new String(signature, paramStart, paramEnd - paramStart + 1);
				params.add(new Param(type, position++));
				paramStart = paramEnd;
				++paramStart;
			}
			paramEnd++;
		}

		m_params = new Param[params.size()];
		m_params = params.toArray(m_params);
	}

	public String getName() { return m_name; }
	public Param[] getParameterTypes() { return m_params; }
	public String getReturnType() { return m_return; }
	public Type getType() { return m_type; }
	public void setType(Type type) { m_type = type; }
}
