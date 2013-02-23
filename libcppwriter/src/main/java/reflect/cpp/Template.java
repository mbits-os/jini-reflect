package reflect.cpp;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class Template {
	public static interface Action {
		void onCall(Object host, StringBuilder sb);
		void onCall(Object host, StringBuilder sb, String arg);
	}

	private static class StringAction implements Action {
		
		private String m_str;

		StringAction(String str) { m_str = str; }

		@Override public void onCall(Object host, StringBuilder sb) { sb.append(m_str); }

		@Override public void onCall(Object host, StringBuilder sb, String arg) { sb.append(m_str); }
		
	}

	private Map<String, Action> m_actions = new HashMap<String, Action>();
	private StreamTokenizer m_st = null;
	private Object m_host = null;

	public void clear() { m_actions.clear(); }

	public void setHost(Object host) { m_host  = host; }

	public void put(String key, String replacement) {
		m_actions.put(key, new StringAction(replacement));
	}

	public void put(String key, Action action) {
		m_actions.put(key, action);
	}

	private String nextToken() {
		int token;
		try {
			token = m_st.nextToken();
		} catch (IOException e) {
			return null;
		}
		switch (token) {
		case StreamTokenizer.TT_NUMBER:
			final String num = String.valueOf(m_st.nval);
			if (num.equals("0.0"))
				return ".";
			return num;
		case StreamTokenizer.TT_WORD:
			return m_st.sval;
		case StreamTokenizer.TT_EOF:
			return null;
		default:
			char ch = (char) m_st.ttype;
			return String.valueOf(ch);
		}
	}

	private void eval(StringBuilder sb) {
		String tok = nextToken();
		while (tok != null) {
			if (!tok.equals("$")) {
				sb.append(tok);
				tok = nextToken();
				continue;
			}
			tok = nextToken();
			if (tok == null) break;
			boolean block = false;
			if (tok.equals("{")) {
				block = true;
				tok = nextToken();
				if (tok == null) break;
			}

			final String name = tok;
			String arg = null;

			if (block) {
				tok = nextToken();
				if (tok == null) break;
				if (tok.equals(":")) {
					StringBuilder collect = new StringBuilder();
					tok = nextToken();
					if (tok == null) break;

					if (tok.trim().isEmpty()) {
						tok = nextToken();
						if (tok == null) break;
					}

					int depth = 0;
					while (tok != null) {
						if (tok.equals("{")) ++depth;
						if (tok.equals("}")) {
							if (depth == 0) {
								arg = eval(collect.toString());
								break;
							}
							--depth;
						}
						collect.append(tok);
						tok = nextToken();
					}
				}

				if (tok.trim().isEmpty()) {
					tok = nextToken();
					if (tok == null) break;
				}
				if (!tok.equals("}"))
					throw new RuntimeException("Unbalanced { found");
			}
			if (m_actions.containsKey(name)) {
				Action a = m_actions.get(name);
				if (arg == null || arg.isEmpty())
					a.onCall(m_host, sb);
				else
					a.onCall(m_host, sb, arg);
			} else {
				System.err.print("Unknown action: " + name);
				if (arg != null)
					System.err.print(":" + arg);
				System.err.println();
			}
			tok = nextToken();
		}
	}
	public void eval(StringBuilder sb, String tmplt) {
		StreamTokenizer st = m_st;
		StringReader rd = new StringReader(tmplt);
		m_st = new StreamTokenizer(rd);
	    m_st.wordChars('_', '_');
	    m_st.ordinaryChar('/');
	    m_st.ordinaryChar('\'');
	    m_st.ordinaryChar('\\');
	    m_st.ordinaryChar('"');
	    m_st.ordinaryChars(0, ' ');
	    try {
	    	eval(sb);
	    } finally {
	    	m_st = st;
	    }
	}
	public String eval(String tmplt) {
		StringBuilder sb = new StringBuilder();
		eval(sb, tmplt);
		return sb.toString();
	}
	public void println(PrintStream out, String tmplt) {
	    out.println(eval(tmplt));
	}
}
