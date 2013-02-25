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

import java.io.File;

import reflect.api.Class;
import reflect.java.ClassHinter;
import reflect.java.SourceCodeParamsHint;

public class AndroidParamsHint extends SourceCodeParamsHint {

	private File m_sdk;
	private API m_api;
	private int m_targetAPI;

	public AndroidParamsHint(File sdk, API api, int targetAPI) { m_sdk = sdk; m_api = api; m_targetAPI = targetAPI; }
	@Override protected File getSourceRoot(String className) { return m_sdk; }
	@Override protected boolean hasClass(String className) { return m_api.find(className, m_targetAPI) != null; }
	@Override protected ClassHinter getClass(String className)
	{
		Class clazz = m_api.find(className, m_targetAPI);
		if (clazz == null)
			return null;
		return new AndroidClassHinter(clazz);
	}
}
