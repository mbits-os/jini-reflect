package reflect;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.Map;

public abstract class SourceCodeParamsHint implements ParamsHint {

	private String m_package;

	protected abstract File getSourceRoot(String className);

	static class Tokenizer {
		private StreamTokenizer m_st;
		private static Map<String, Integer> s_ignore = new HashMap<String, Integer>();
		static {
			s_ignore.put("private", 0);
			s_ignore.put("protected", 0);
			s_ignore.put("public", 0);
			s_ignore.put("static", 0);
			s_ignore.put("final", 0);
			s_ignore.put("abstract", 0);
			s_ignore.put("transient", 0);
			s_ignore.put("volatile", 0);
		};

		Tokenizer(FileReader rd) throws IOException
		{
		    m_st = new StreamTokenizer(rd);

		    //m_st.parseNumbers();
		    m_st.wordChars('_', '_');
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
	}

	private static void skipTo(Tokenizer tokenizer, String tok) throws IOException {
		String t = tokenizer.nextToken();
		while (t != null && !t.equals(tok))
			t = tokenizer.nextToken();
	}

	static String ignoreAnnotation(Tokenizer tok) throws IOException
	{
		tok.nextToken();
		String t = tok.nextToken();
		if (t.equals("("))
		{
			skipTo(tok, ")");
			if (t != null) t = tok.nextToken();
		}
		return t;
	}

	public void addImport(String imp)
	{
		if (imp == null) return;
		System.out.println("Import: " + imp);
	}

	private boolean readClass(Tokenizer tok, String ctor, String typeName, String indent) throws IOException {
		skipTo(tok, "{");
		return false;
	}
	
	public boolean read(File java) throws IOException
	{
	    FileReader rd = new FileReader(java);
	    Tokenizer tok = new Tokenizer(rd);
	    int currentBracket = 0;
	    int ignoreBracket = 2; //Integer.MAX_VALUE;
	    try
	    {
	    	String t = tok.nextToken();
	    	while (t != null)
	    	{
	    		if (t.equals("@")) {
	    			t = ignoreAnnotation(tok);
	    			if (t == null) return false;
	    		}

	    		if (t.equals("package"))
	    		{
	    			m_package = tok.nextToken();
	    			if (m_package == null || m_package.equals(";"))
	    			{
	    				m_package = null;
	    				return false;
	    			}
	    			skipTo(tok, ";");
	    			t = tok.nextToken();
	    			System.out.println("Package: " + m_package);
	    			continue;
	    		}

	    		if (t.equals("import"))
	    		{
	    			final String imp = tok.nextToken();
	    			if (imp == null || imp.equals(";"))
	    				return false;

	    			addImport(imp);

	    			skipTo(tok, ";");
	    			t = tok.nextToken();

	    			continue;
	    		}
	    		
	    		if (t.equals("class"))
	    		{
	    			t = tok.nextToken();
	    			if (t == null)
	    				return false;
	    			System.out.println("Class: " + t);

	    			if (!readClass(tok, t, t, "    "))
	    				return false;

	    			++currentBracket;

	    			t = tok.nextToken();
	    			continue;
	    		}

	    		if (t.equals("{")) ++currentBracket;
	    		else if (t.equals("}")) --currentBracket;

	    		//if (currentBracket == ignoreBracket && t.equals("{")) System.out.println(";");
	    		//if (currentBracket == ignoreBracket-1 && t.equals("}")) continue;
	    		if (currentBracket < ignoreBracket)
	    		{
		    		if (t.equals(";") || t.equals("{") || t.equals("}")) System.out.println(t);
		    		else System.out.print(t + " ");
	    		}
	    		t = tok.nextToken();
	    	}
	    } finally {
	    	rd.close();
	    }
		return true;
	}

	@Override public ClassHint[] getHints(String className) {
		File java = null;
		final File root = getSourceRoot(className);
		if (root != null)
		{
			String name = className.split("\\$")[0].replace('.', File.separatorChar) + ".java";
			java = new File(root, name);
		}

		if (java == null || !java.isFile())
			return null;

		try {
			if (!read(java))
				return null;
		} catch (IOException ex) {
			return null;
		}

		return null;
	}
}
