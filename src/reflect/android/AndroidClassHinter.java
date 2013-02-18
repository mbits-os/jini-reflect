package reflect.android;

import reflect.android.api.Class;
import reflect.java.ClassHinter;
import reflect.java.MethodGroupHinter;

public class AndroidClassHinter implements ClassHinter {

	private Class m_class;

	public AndroidClassHinter(Class clazz) {
		m_class = clazz;
	}

	@Override public void finished() { m_class.setHinted(true); }

	@Override public MethodGroupHinter getMethodGroup(String methodName) {
		Class.MethodGroup group = m_class.get(methodName);
		if (group == null)
			return null;
		return new AndroidMethodGroupHinter(group);
	}

}
