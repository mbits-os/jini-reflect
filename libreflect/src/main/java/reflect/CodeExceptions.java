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
 
package reflect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class CodeExceptions {
	private static Map<String, String> s_map = new HashMap<String, String>();
	public static void readExceptions(File appDir) {
		BufferedReader br = null;
		try {
			InputStream    fis;
			String         line;
			fis = new FileInputStream(new File(appDir, "../exceptions.txt"));
			br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
			while ((line = br.readLine()) != null) {
				final String[] chunks = line.split(":");
			    if (chunks.length != 3) continue;
			    addException(chunks[0], chunks[1], chunks[2]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null)
				try { br.close(); } catch (IOException e) {}
		}
	}

	private static void addException(String context, String symbol, String resolved) {
		s_map.put(context + ":" + symbol, resolved);
	}
	public static String get(String context, String symbol) {
		if (s_map.containsKey(context + ":" + symbol))
			return s_map.get(context + ":" + symbol);
		if (s_map.containsKey("*:" + symbol))
			return s_map.get("*:" + symbol);
		return null;
	}

}
