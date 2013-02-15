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
	private Vector<String> m_imports = new Vector<String>();
	private static Map<String, String> s_builtins = new HashMap<String, String>();
	
	static {
		s_builtins.put("boolean", "Z");
		s_builtins.put("byte", "B");
		s_builtins.put("char", "C");
		s_builtins.put("short", "S");
		s_builtins.put("int", "I");
		s_builtins.put("long", "J");
		s_builtins.put("float", "F");
		s_builtins.put("double", "D");
		s_builtins.put("void", "V");
	}

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
			s_ignore.put("synchronized", 0);
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
		void pushBack() { m_st.pushBack(); }
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

	private static void skipBlocksTo(Tokenizer tokenizer, String tok) throws IOException {
		String t = tokenizer.nextToken();
		while (t != null && !t.equals(tok))
		{
			if (t.equals("{") && skipBlock(tokenizer)) return;
			if (t.equals("<") && skipGenerics(tokenizer)) return;
			t = tokenizer.nextToken();
		}
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
		m_imports.add(imp);
		System.out.println("Import: " + imp);
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
			t = tok.nextToken();
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

	private class MethodReader {
		private Tokenizer m_tok;
		private ClassHint m_parent;
		private String m_ctor;
		private String m_type;
		private Map<String, String> m_aliases = null;
		MethodReader(Tokenizer tok, ClassHint parent, String ctor, String type) {
			m_tok = tok;
			m_parent = parent;
			m_ctor = ctor;
			m_type = type;
		}

		private String tryClassName(String className) {
			try {
				Class<?> cand = Class.forName(className);
				if (cand != null) return "L" + cand.getName() + ";";
			} catch (ClassNotFoundException e) {
			}
			return null;
		}

		private String tryImport(String typeName) {
			typeName = "." + typeName;
			//System.out.println("Trying " + typeName + " with imports");
			for (String imp: m_imports)
			{
				//System.out.println("  > Trying " + imp);
				if (imp.endsWith(typeName)) // TODO: "import pa.cka.ge.Class;" and "Class.Subclass" case missing
				{
					String quick = tryClassName(imp);
					if (quick != null)
					{
						//System.out.println("  ! Found " + quick);
						return quick;
					}
					//now, where is the package, and where are the classes...
					//System.out.println("  ? Found " + imp);
					return "L" + imp + ";"; // TODO: temp;
				}
				
				if (imp.endsWith(".*"))
				{
					String cand = tryClassName(imp.substring(0, imp.length()-2) + typeName);
					if (cand != null)
						return cand;
				}
			}
			return null;
		}
		private String resolve(String typeName) {
			if (s_builtins.containsKey(typeName))
				return s_builtins.get(typeName);
			
			if (m_aliases != null && m_aliases.containsKey(typeName))
				return m_aliases.get(typeName);

			String cand = null;
			if (typeName.equals(m_ctor))
				cand = m_type;

			if (cand == null) cand = tryClassName(typeName);
			if (cand == null) cand = tryClassName(m_package + "." + typeName);
			if (cand == null) cand = tryClassName("java.lang." + typeName);
			if (cand == null) cand = tryImport(typeName);

			if (cand == null)
			{
				System.err.flush();
				System.out.flush();
				System.err.println("Could not resolve " + typeName);
			}

			return cand;
		}

		private void setAlias(String generic, String resolved) {
			if (m_aliases == null)
				m_aliases = new HashMap<String, String>();
			m_aliases.put(generic, resolved);
		}

		public boolean readGenerics() throws IOException {
			String t = m_tok.nextToken();
			while (t != null) {
				if (t.equals(">"))
					return true;

				String alias = t;
				
				t = m_tok.nextToken(); // ">" "," "extends"
				if (t.equals("extends")) {
					t = m_tok.nextToken();
					final String resolved = resolve(t);
					setAlias(alias, resolved == null ? "?" + t : resolved);
					t = m_tok.nextToken();
				} else {
					setAlias(alias, "Ljava/lang/Object;");
				}

				if (t.equals(","))
					t = m_tok.nextToken();
				if (t.equals(">"))
					return true;

				t = m_tok.nextToken();
			}
			return false;
		}
		public StringPair readType(String firstToken, String nextToken) throws IOException {
			if (nextToken.equals("<"))
			{
				if (!skipGenerics(m_tok))
					return new StringPair(null, null);
				nextToken = m_tok.nextToken();
			}

			String resolved = resolve(firstToken);

			StringBuilder sb = new StringBuilder();

			if (resolved == null)
				sb.append("?");

			while (nextToken != null && nextToken.equals("["))
			{
				sb.append("[");
				if (!skipIndex(m_tok))
					return new StringPair(null, null);
				nextToken = m_tok.nextToken();
			}

			int dots = 0;
			// the StreamTokenizer will convert a "lone" dot into a 0.0 number
			while (nextToken != null && (nextToken.equals(".") || nextToken.equals("0.0")))
			{
				++dots;
				nextToken = m_tok.nextToken();
			}

			if (dots != 0 && dots != 3)
				return new StringPair(null, null);
			if (dots == 3) // the "T... param" becomes "[T"
				sb.append("[");

			sb.append(resolved == null ? firstToken : resolved);
			return new StringPair(sb.toString(), nextToken);
		}

		public StringPair readType(String firstToken) throws IOException {
			String t = m_tok.nextToken();
			if (t.equals("<") || t.equals("["))
				return readType(firstToken, t);
			int dots = 0;
			if (firstToken.endsWith("..."))
			{
				if (firstToken.endsWith("...."))
					return new StringPair(null, null);

				dots = 3;
				firstToken = firstToken.substring(0, firstToken.length()-3);
			}
			String resolved = resolve(firstToken);
			if (resolved == null)
				resolved = "?"+(dots == 3 ? "[" + firstToken : firstToken);
			else if (dots == 3)
				resolved = "[" + resolved;
			return new StringPair(resolved, t);
		}
		
		public StringPair readCtorOrSelfType() throws IOException {
			StringPair _type = new StringPair("V", "<init>"); //assume it's a constructor
			String t = m_tok.nextToken();
			if (t.equals("<") || t.equals("[")) // definitely not a ctor
				_type = readType(m_ctor, t);
			else if (t.equals("(")) // ctor would now have a open parenthesis
				m_tok.pushBack();
			else
				_type = new StringPair("L" + m_type + ";", t);
			return _type;
		}

		public boolean readMethod(String retType, String name, String indent) throws IOException {
			MethodHint _meth = new MethodHint(retType, name);
			String t = m_tok.nextToken();
			while (t != null)
			{
				if (t.equals(")"))
				{
					t = m_tok.nextToken();
					while (t != null && !t.equals(";") && !t.equals("{"))
						t = m_tok.nextToken();
					if (t == null)
						return false;
					if (t.equals("{") && !skipBlock(m_tok))
						return false;
					m_parent.add(_meth);
					System.out.println(indent + _meth.toString());
					return true;
				}

				StringPair param = readType(t);
				if (param.nextToken == null)
					break;
				
				if (param.nextToken.equals(",") || param.nextToken.equals(")"))
				{
					t = param.nextToken;
					param.nextToken = null;
				}
				else
				{
					t = m_tok.nextToken();
					if (t.equals(","))
						t = m_tok.nextToken();
				}

				_meth.addParam(param.value, param.nextToken);
			}
			return false;
		}
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

    		if (t.equals("class") || t.equals("interface") || t.equals("enum"))
    		{
    			t = tok.nextToken();
    			if (t == null)
    				return false;
    			System.out.println(indent + "Class: " + typeName + "$" + t + " {");

    			try { 
	    			if (!readClass(tok, t, typeName + "$" + t, indent + "    "))
	    				return false;
    			} finally {
    				System.out.println(indent + "}");
    			}

    			t = tok.nextToken();
    			if (t != null && t.equals(";")) t = tok.nextToken();
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

    		MethodReader m_reader = new MethodReader(tok, _class, ctor, type);
    		StringPair _type;

    		if (t.equals("<")) // it's the <? extends Xyz> ? function(? _x);
    		{
    			if (!m_reader.readGenerics())
    				return false;
    			t = tok.nextToken();
    		}

    		if (t.equals(ctor)) //might be Type(...) or Type prop; or Type meth(...);
    		{
    			_type = m_reader.readCtorOrSelfType();
    		}
    		else
    			_type = m_reader.readType(t);

    		if (_type.nextToken == null) break;

   			t = tok.nextToken();

    		if (t == null)
    			return false;

    		if (t.equals("("))
    		{
    			if (!m_reader.readMethod(_type.value, _type.nextToken, indent))
    			{
    				return false;
    			}
    			t = tok.nextToken();
    			continue;
    		}

    		if (!t.equals(";")) skipBlocksTo(tok, ";");
    		
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
	    		
	    		if (t.equals("class") || t.equals("interface") || t.equals("enum"))
	    		{
	    			t = tok.nextToken();
	    			if (t == null)
	    				return false;
	    			System.out.println("Class: " + t + " {");

	    			try {
		    			if (!readClass(tok, t, t, "    "))
		    				return false;
		    		} finally {
	    				System.out.println("}");
	    			}

	    			++currentBracket;

	    			t = tok.nextToken();
	    			if (t != null && t.equals(";")) t = tok.nextToken();
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
