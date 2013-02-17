package reflect.android.api;

import java.util.Vector;

public class Method extends Artifact {

	private String m_name;
	private Param[] m_params = null;
	private String m_return = null;

	public Method() {
		this(1, "<init>", "()V");
	}

	public Method(String name, String signature) {
		this(1, name, signature);
	}

	public Method(int since, String name, String signature) {
		super(since, signature);
		m_name = name;
		breakSignature();
	}

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
				//fall-through:
			default:
				final String type = new String(signature, paramStart, paramEnd - paramStart);
				params.add(new Param(type, position++));
				paramStart = paramEnd;
			}
			paramEnd++;
		}

		m_params = new Param[params.size()];
		m_params = params.toArray(m_params);
	}

	public String getName() { return m_name; }
	public Param[] getParameterTypes() { return m_params; }
	public String getReturnType() { return m_return; }
}
