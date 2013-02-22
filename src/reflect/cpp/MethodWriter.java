package reflect.cpp;

import java.io.PrintStream;

import reflect.android.api.Class;
import reflect.android.api.Method;
import reflect.android.api.Param;

public abstract class MethodWriter extends TypeUtils {
	abstract void onMethod(PrintStream out, Object o, Class clazz, String indent, Method.Type type, String retType, String name, Param[] pars, int version);

	private static boolean methodHasOnlyKnownClasses(Method meth) {
		if (!isKnownClassOrBuiltin(meth.getReturnType()))
			return false;

		for (Param param: meth.getParameterTypes())
		{
			if (!isKnownClassOrBuiltin(param.getSignature()))
				return false;
		}
		return true;
	}
	public void printMethods(PrintStream out, Object o, String indent, Class clazz, MethodWriter cb) {
		for (Class.MethodGroup group: clazz.getGroups())
		{
			if (group.m_methods.size() == 1) {
				Method meth = group.m_methods.get(0);
				if (!methodHasOnlyKnownClasses(meth))
					continue;

				cb.onMethod(
						out, o, clazz, indent, meth.getType(),
						meth.getReturnType(), meth.getName(), meth.getParameterTypes(),
						0);
				continue;
			}
			int ver = 0;
			for (Method meth: group.m_methods) {
				if (!methodHasOnlyKnownClasses(meth))
					continue;

				cb.onMethod(
						out, o, clazz, indent, meth.getType(),
						meth.getReturnType(), meth.getName(), meth.getParameterTypes(),
						++ver);
			}
		}
	}
	public void printMethods(PrintStream out, String indent, Class clazz, MethodWriter cb) {
		printMethods(out, null, indent, clazz, cb);
	}

}
