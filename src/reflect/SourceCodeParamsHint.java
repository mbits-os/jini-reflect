package reflect;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public abstract class SourceCodeParamsHint implements ParamsHint {

	private String m_package;
	private Vector<ClassHint> m_hints;

	protected abstract File getSourceRoot(String className);

	static class Tokenizer {
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

	static class StringPair {
		String value;
		String nextToken;
		StringPair(String val, String t)
		{
			value = val;
			nextToken = t;
		}
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			sb.append(value);
			sb.append(" ");
			sb.append(nextToken);
			return sb.toString();
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

	private boolean readPackage(Tokenizer tok) throws IOException
	{
		m_package = tok.nextToken();
		if (m_package == null || m_package.equals(";"))
		{
			m_package = null;
			return false;
		}
		skipTo(tok, ";");
		System.out.println("Package: " + m_package);
		return true;
	}

	public void addImport(String imp)
	{
		if (imp == null) return;
		//System.out.println("Import: " + imp);
	}

	private boolean readImport(Tokenizer tok) throws IOException
	{
		final String imp = tok.nextToken();
		if (imp == null || imp.equals(";"))
			return false;

		addImport(imp);

		skipTo(tok, ";");
		return true;
	}

	private static boolean skipBlock(Tokenizer tok, String open, String close) throws IOException {
		int depth = 1;
		String t = tok.nextToken();
		while (t != null)
		{
			if (t.equals(open)) ++depth;
			if (t.equals(close))
			{
				if (--depth == 0)
					return true;
			}
		}
		return false;
	}

	private static boolean skipBlock(Tokenizer tok) throws IOException {
		return skipBlock(tok, "{", "}");
	}

	private static boolean skipGenerics(Tokenizer tok) throws IOException {
		return skipBlock(tok, "<", ">");
	}

	private static boolean skipIndex(Tokenizer tok) throws IOException {
		return skipBlock(tok, "[", "]");
	}

	private StringPair readType(Tokenizer tok, String firstToken, String nextToken) throws IOException {
		if (nextToken.equals("<"))
		{
			if (!skipGenerics(tok))
				return new StringPair(null, null);
			nextToken = tok.nextToken();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("?");
		while (nextToken != null && nextToken.equals("["))
		{
			sb.append("[");
			if (!skipIndex(tok))
				return new StringPair(null, null);
		}
		//array
		sb.append(firstToken);
		return new StringPair(sb.toString(), nextToken);
	}

	private StringPair readType(Tokenizer tok, String firstToken) throws IOException {
		String t = tok.nextToken();
		if (t.equals("<") || t.equals("["))
			return readType(tok, firstToken, t);
		return new StringPair("?"+firstToken, t);
	}

	private boolean readMethod(Tokenizer tok, ClassHint _class, String retType, String name, String indent) throws IOException {
		MethodHint _meth = new MethodHint(retType, name);
		String t = tok.nextToken();
		while (t != null)
		{
			if (t.equals(")"))
			{
				t = tok.nextToken();
				while (t != null && !t.equals(";") && !t.equals("{"))
					t = tok.nextToken();
				if (t == null)
					return false;
				if (t.equals("{") && !skipBlock(tok))
					return false;
				_class.add(_meth);
				return true;
			}
		}
		return false;
	}

	private boolean readClass(Tokenizer tok, String ctor, String typeName, String indent) throws IOException {
		skipTo(tok, "{");
		String t = tok.nextToken();
		final String type;
		if (m_package != null)
			type = m_package + "." + typeName;
		else
			type = typeName;
		ClassHint _class = new ClassHint(type);

		while (t != null)
		{
			if (t.equals("}"))
			{
				m_hints.add(_class);
				return true;
			}

    		if (t.equals("@")) {
    			t = ignoreAnnotation(tok);
    			if (t == null) return false;
    		}

    		if (t.equals("class") || t.equals("interface"))
    		{
    			t = tok.nextToken();
    			if (t == null)
    				return false;
    			System.out.println(indent + "Class: " + typeName + "$" + t);

    			if (!readClass(tok, t, typeName + "$" + t, indent + "    "))
    				return false;

    			t = tok.nextToken();
    			continue;
    		}

    		if (t.equals("{")) // static { ... }
    		{
    			if (!skipBlock(tok))
    				return false;
    			t = tok.nextToken();
    			continue;
    		}

    		// a method or a property
    		// method:   <type> <name> "(" [<type> <name> ["," <type> <name>]*] ")" <block>
    		// property: <type> <name> [";" | "="]

    		StringPair _type;
    		if (t.equals(ctor)) //might bew Type(...) or Type prop; or Type meth(...);
    		{
    			_type = new StringPair("V", "<init>"); //assume it's a constructor
    			t = tok.nextToken();
    			if (t.equals("<") || t.equals("[")) // definitely not a ctor
    				_type = readType(tok, ctor, t);
    			else if (!t.equals("(")) // ctor would now have a open parenthesis
    				_type = new StringPair("L" + type + ";", t);
    		}
    		else
    			_type = readType(tok, t);

    		if (_type.nextToken == null) break;

    		System.err.print(indent + _type.toString());

    		t = tok.nextToken();
    		if (t == null)
    			return false;
    		if (t.equals("("))
    		{
    			if (!readMethod(tok, _class, _type.value, _type.nextToken, indent + "    "))
    			{
    				System.err.println();
    				return false;
    			}
    		}

    		if (!t.equals(";")) skipTo(tok, ";");
			System.err.println();
    		
    		t = tok.nextToken();
		}
		return false;
	}

	public boolean read(File java) throws IOException
	{
	    FileReader rd = new FileReader(java);
	    Tokenizer tok = new Tokenizer(rd);
	    int currentBracket = 0;
	    int ignoreBracket = 2; //Integer.MAX_VALUE;
	    try {
	    	String t = tok.nextToken();
	    	while (t != null) {
	    		if (t.equals("@")) {
	    			t = ignoreAnnotation(tok);
	    			if (t == null) return false;
	    		}

	    		if (t.equals("package")) {
	    			if (!readPackage(tok))
	    				return false;
	    			t = tok.nextToken();
	    			continue;
	    		}

	    		if (t.equals("import")) {
	    			if (!readImport(tok))
	    				return false;
	    			t = tok.nextToken();
	    			continue;
	    		}
	    		
	    		if (t.equals("class") || t.equals("interface"))
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
		m_hints = new Vector<ClassHint>();

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

		if (m_hints.size() == 0)
			return null;

		ClassHint[] ret = new ClassHint[m_hints.size()];
		return m_hints.toArray(ret);
	}
}
