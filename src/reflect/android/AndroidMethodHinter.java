package reflect.android;

import java.util.Vector;

import reflect.android.api.Method;
import reflect.android.api.Param;
import reflect.java.MethodHinter;

public class AndroidMethodHinter implements MethodHinter {

	private Method m_method;

	public AndroidMethodHinter(Method method) {
		m_method = method;
	}

	@Override public void setHints(Vector<String> names) {
		Param[] params = m_method.getParameterTypes();
		int len = names.size();

		if (len > params.length)
			len = params.length;
		
		for(int i = 0; i < len; ++i)
		{
			String name = names.get(i);
			if (name == null) continue;
			params[i].setName(name);
		}
	}

}
