package reflect.cpp;

import java.io.PrintStream;

import reflect.android.api.Class;
import reflect.android.api.Param;

public abstract class ParamWriter extends TypeUtils {

	abstract void onParameter(PrintStream out, Class clazz, String sep, String type, String name);
	static private void print(PrintStream out, Class clazz, String sep, Param[] pars, ParamWriter cb) {
		boolean first = true;
		for (Param par: pars)
		{
			final String _sep = first ? "" : sep;
			first = false;
			cb.onParameter(out, clazz, _sep, par.getSignature(), par.getName());
		}
	}
	static private void firstArg(PrintStream out, String sep, String firstArg, Param[] pars) {
		out.print(firstArg);
		if (pars.length > 0)
			out.print(sep);
	}
	static public void printNameAndType(PrintStream out, Class clazz, String sep, Param[] pars) {
		print(out, clazz, sep, pars, s_nameAndType);
	}
	static public void printName(PrintStream out, Class clazz, String sep, Param[] pars) {
		print(out, clazz, sep, pars, s_name);
	}
	static public void printType(PrintStream out, Class clazz, String sep, Param[] pars) {
		print(out, clazz, sep, pars, s_type);
	}
	static public void printNameAndType(PrintStream out, Class clazz, String sep, String firstArg, Param[] pars) {
		firstArg(out, sep, firstArg, pars);
		print(out, clazz, sep, pars, s_nameAndType);
	}
	static public void printName(PrintStream out, Class clazz, String sep, String firstArg, Param[] pars) {
		firstArg(out, sep, firstArg, pars);
		print(out, clazz, sep, pars, s_name);
	}
	static public void printType(PrintStream out, Class clazz, String sep, String firstArg, Param[] pars) {
		firstArg(out, sep, firstArg, pars);
		print(out, clazz, sep, pars, s_type);
	}

	static private ParamWriter s_nameAndType = new ParamWriter() {

		@Override void onParameter(PrintStream out, Class clazz, String sep, String type, String name) {
			out.print(sep);
			out.print(getType(type, clazz.getName()));
			out.print(" ");
			out.print(name);
		}
		
	};
	static private ParamWriter s_name = new ParamWriter() {

		@Override void onParameter(PrintStream out, Class clazz, String sep, String type, String name) {
			out.print(sep);
			out.print(name);
		}
		
	};
	static private ParamWriter s_type = new ParamWriter() {

		@Override void onParameter(PrintStream out, Class clazz, String sep, String type, String name) {
			out.print(sep);
			out.print(getType(type, clazz.getName()));
		}
	};
}
