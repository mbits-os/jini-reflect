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
 
package reflect.java;

import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.Map;

class Tokenizer {
	private StreamTokenizer m_st;
	private static Map<String, Integer> s_ignore = new HashMap<String, Integer>();
	static {
		s_ignore.put("native", 0);
		s_ignore.put("private", 0);
		s_ignore.put("protected", 0);
		s_ignore.put("public", 0);
		s_ignore.put("static", 0);
		s_ignore.put("final", 0);
		s_ignore.put("abstract", 0);
		s_ignore.put("transient", 0);
		s_ignore.put("volatile", 0);
		s_ignore.put("synchronized", 0);
	};

	Tokenizer(FileReader rd) throws IOException
	{
	    m_st = new StreamTokenizer(rd);

	    //m_st.parseNumbers();
	    m_st.wordChars('_', '_');
	    m_st.ordinaryChar('/');
	    m_st.ordinaryChars(0, ' ');
	    m_st.slashSlashComments(true);
	    m_st.slashStarComments(true);
	}

	String nextToken() throws IOException
	{
		int token = m_st.nextToken();
		switch (token) {
		case StreamTokenizer.TT_NUMBER:
			double num = m_st.nval;
			if (num > -0.01 && num < 0.01)
				return ".";
			return String.valueOf(num);
		case StreamTokenizer.TT_WORD:
			String word = m_st.sval;
			if (word.trim().length() == 0)
				return nextToken();
			if (s_ignore.containsKey(word))
				return nextToken();
			return word;
		case '"':
			String dquoteVal = m_st.sval;
			return "\"" + dquoteVal + "\"";
		case '\'':
			String squoteVal = m_st.sval;
			return "\'" + squoteVal + "\'";
		case StreamTokenizer.TT_EOF:
			return null;
		default:
			char ch = (char) m_st.ttype;
			if (Character.isWhitespace(ch))
				return nextToken();
			return String.valueOf(ch);
		}
	}
	void pushBack() { m_st.pushBack(); }
}
