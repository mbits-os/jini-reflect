package reflect.android;

import java.util.Vector;

import reflect.android.api.Class.MethodGroup;
import reflect.java.MethodGroupHinter;
import reflect.java.MethodHinter;

public class AndroidMethodGroupHinter implements MethodGroupHinter {

	private MethodGroup m_group;

	public AndroidMethodGroupHinter(MethodGroup group) {
		m_group = group;
	}

	@Override public MethodHinter find(String retType, Vector<String> types) {
		return null;
	}

}
