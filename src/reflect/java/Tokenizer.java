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
