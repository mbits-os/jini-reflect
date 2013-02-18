package reflect.android;

import java.util.Vector;

import reflect.android.api.Class.MethodGroup;
import reflect.android.api.Method;
import reflect.android.api.Param;
import reflect.java.MethodGroupHinter;
import reflect.java.MethodHinter;

public class AndroidMethodGroupHinter implements MethodGroupHinter {

	private MethodGroup m_group;

	public AndroidMethodGroupHinter(MethodGroup group) {
		m_group = group;
	}

	@Override public MethodHinter find(String retType, Vector<String> types) {
		for (Method method: m_group.m_methods)
		{
			if (matches(method, retType, types))
				return new AndroidMethodHinter(method);
		}
		return null;
	}

	private boolean matches(String hintedType, String apiType) {
		//System.out.println("       mathing: " + hintedType + " with " + apiType);
		if (hintedType.equals(apiType))
			return true;
		return false;
	}

	private boolean matches(Method method, String retType, Vector<String> types) {
		//System.out.print("    Matching\n      (");
		//for (String s: types) System.out.print(s);
		//System.out.println(")" + retType + "\n      " + method.getSignature());

		Param[] params = method.getParameterTypes();
		if (params.length != types.size())
			return false;

		if (!matches(retType, method.getReturnType()))
			return false;
		for (int i = 0; i < params.length; ++i)
		{
			if (!matches(types.get(i), params[i].getSignature()))
				return false;
		}
		return true;
	}

}
