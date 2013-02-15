package reflect;

import java.util.Vector;

public class MethodHint {
	public static class ParamHint {
		public ParamHint(String type, String name) {
			m_type = type;
			m_name = name;
		}
		String m_type;
		String m_name;
	}
	String m_return;
	String m_name;
	Vector<ParamHint> m_params;

	MethodHint(String retType, String name) {
		m_return = retType;
		m_name = name;
		m_params = new Vector<ParamHint>();
	}

	public void addParam(String type, String name) {
		m_params.add(new ParamHint(type, name));
	}

	public String signature() {
		StringBuilder sb = new StringBuilder();
		sb.append(m_name);
		sb.append("(");
		for (ParamHint p: m_params)
			sb.append(p.m_type);
		sb.append(")");
		sb.append(m_return);
		return sb.toString();
	}
	
	public String toString() { return signature(); }
}
