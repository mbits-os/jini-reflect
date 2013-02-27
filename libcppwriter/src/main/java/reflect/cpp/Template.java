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
 
package reflect.cpp;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple template engine. Using the map of values and actions, it builds parameterized strings.
 * The expected forms of the macros are: <code>$macro</code>, <code>${macro}</code> and (only for
 * actions) <code>${macro: argument}</code>.
 *  
 * <pre class="prettyprint">Template tmplt = new Template();
 *tmplt.put("first", "Hello");
 *tmplt.put("second", "World");
 *tmplt.put("to_upper", new Template.Action() {
 *    &#x40;Override
 *    public void onCall(Object host, StringBuilder sb, String arg) {
 *        sb.append(arg.toUpperCase());
 *    }
 *    &#x40;Override
 *    public void onCall(Object host, StringBuilder sb) {}
 *});
 *System.out.println(tmplt.eval("${to_upper: $first}, $second!"));</pre>
 *<p>would produce
 *<pre class="nopretty">HELLO, World!</pre>
 */
public class Template {

	/**
	 * Action called to get a replacement for a <code>$macro</code>.
	 */
	public static interface Action {
		/**
		 * Called for argument-less macros (<code>$macro</code> and <code>${macro}</code>).
		 * 
		 * @param host the host recently set by {@link reflect.cpp.Template#setHost(java.lang.Object) setHost}.
		 * @param sb the destination for the replacement.
		 */
		void onCall(Object host, StringBuilder sb);
		/**
		 * Called for macros with an argument (<code>${macro: arg}</code>).
		 * 
		 * @param host the host recently set by {@link reflect.cpp.Template#setHost(java.lang.Object) setHost}.
		 * @param sb the destination for the replacement.
		 * @param arg the argument of the macro.
		 */
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

	/**
	 * Constructs a new instance of <code>Template</code>.
	 */
	public Template() {}

	/**
	 * Clears the current contents of the macro map.
	 */
	public void clear() { m_actions.clear(); }

	/**
	 * Sets the host for the macro expansion. This object will be provided
	 * to all actions called for the template.
	 * 
	 * @param host the host for the actions.
	 */
	public void setHost(Object host) { m_host  = host; }

	/**
	 * Adds value macro.
	 * 
	 * @param key the macro name
	 * @param replacement the value of the macro
	 */
	public void put(String key, String replacement) {
		m_actions.put(key, new StringAction(replacement));
	}

	/**
	 * Adds action macro.
	 * @param key the macro name
	 * @param action the macro replacement action.
	 */
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

	/**
	 * Evaluates macros in the given template and adds the result
	 * to the <code>StringBuilder</code>.
	 * 
	 * @param sb the result of the operation
	 * @param tmplt the template to evaluate
	 */
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

	/**
	 * Evaluates macros in the given template and returns it as a string.
	 * 
	 * @param tmplt the template to evaluate
	 * @returns evaluated template
	 */
	public String eval(String tmplt) {
		StringBuilder sb = new StringBuilder();
		eval(sb, tmplt);
		return sb.toString();
	}
	/**
	 * Prints the evaluated template. Shortcut for <code>out.println(template.eval(tmplt))</code>.
	 * 
	 * @param out the stream to print to
	 * @param tmplt the template to evaluate
	 */
	public void println(PrintStream out, String tmplt) {
	    out.println(eval(tmplt));
	}
}
