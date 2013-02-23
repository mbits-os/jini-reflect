package reflect.cpp;

import java.io.PrintStream;

import reflect.api.Class;
import reflect.api.Param;

public abstract class ParamWriter extends TypeUtils {

	abstract void onParameter(PrintStream out, Class clazz, String sep, String type, String name);
	abstract void onParameter(StringBuilder sb, Class clazz, String sep, String type, String name);

	static private void print(PrintStream out, Class clazz, Param[] pars, ParamWriter cb) {
		boolean first = true;
		for (Param par: pars)
		{
			final String _sep = first ? "" : ", ";
			first = false;
			cb.onParameter(out, clazz, _sep, par.getSignature(), par.getName());
		}
	}
	static private void print(StringBuilder sb, Class clazz, Param[] pars, ParamWriter cb) {
		boolean first = true;
		for (Param par: pars)
		{
			final String _sep = first ? "" : ", ";
			first = false;
			cb.onParameter(sb, clazz, _sep, par.getSignature(), par.getName());
		}
	}
	static private void firstArg(PrintStream out, String firstArg, Param[] pars) {
		out.print(firstArg);
		if (pars.length > 0)
			out.print(", ");
	}
	static private void firstArg(StringBuilder sb, String firstArg, Param[] pars) {
		sb.append(firstArg);
		if (pars.length > 0)
			sb.append(", ");
	}
	static public void printNameAndType(PrintStream out, Class clazz, Param[] pars) {
		print(out, clazz, pars, s_nameAndType);
	}
	static public void printName(PrintStream out, Class clazz, Param[] pars) {
		print(out, clazz, pars, s_name);
	}
	static public void printType(PrintStream out, Class clazz, Param[] pars) {
		print(out, clazz, pars, s_type);
	}
	static public void printNameAndType(PrintStream out, String firstArg, Class clazz, Param[] pars) {
		firstArg(out, firstArg, pars);
		print(out, clazz, pars, s_nameAndType);
	}
	static public void printName(PrintStream out, String firstArg, Class clazz, Param[] pars) {
		firstArg(out, firstArg, pars);
		print(out, clazz, pars, s_name);
	}
	static public void printType(PrintStream out, String firstArg, Class clazz, Param[] pars) {
		firstArg(out, firstArg, pars);
		print(out, clazz, pars, s_type);
	}
	static public void printNameAndType(StringBuilder sb, Class clazz, Param[] pars) {
		print(sb, clazz, pars, s_nameAndType);
	}
	static public void printName(StringBuilder sb, Class clazz, Param[] pars) {
		print(sb, clazz, pars, s_name);
	}
	static public void printType(StringBuilder sb, Class clazz, Param[] pars) {
		print(sb, clazz, pars, s_type);
	}
	static public void printNameAndType(StringBuilder sb, String firstArg, Class clazz, Param[] pars) {
		firstArg(sb, firstArg, pars);
		print(sb, clazz, pars, s_nameAndType);
	}
	static public void printName(StringBuilder sb, String firstArg, Class clazz, Param[] pars) {
		firstArg(sb, firstArg, pars);
		print(sb, clazz, pars, s_name);
	}
	static public void printType(StringBuilder sb, String firstArg, Class clazz, Param[] pars) {
		firstArg(sb, firstArg, pars);
		print(sb, clazz, pars, s_type);
	}

	static private ParamWriter s_nameAndType = new ParamWriter() {

		@Override void onParameter(PrintStream out, Class clazz, String sep, String type, String name) {
			out.print(sep);
			out.print(getType(type, clazz.getName(), true));
			out.print(" ");
			out.print(name);
		}
		@Override void onParameter(StringBuilder sb, Class clazz, String sep, String type, String name) {
			sb.append(sep);
			sb.append(getType(type, clazz.getName(), true));
			sb.append(" ");
			sb.append(name);
		}
		
	};
	static private ParamWriter s_name = new ParamWriter() {

		@Override void onParameter(PrintStream out, Class clazz, String sep, String type, String name) {
			out.print(sep);
			out.print(name);
		}
		@Override void onParameter(StringBuilder sb, Class clazz, String sep, String type, String name) {
			sb.append(sep);
			sb.append(name);
		}
		
	};
	static private ParamWriter s_type = new ParamWriter() {

		@Override void onParameter(PrintStream out, Class clazz, String sep, String type, String name) {
			out.print(sep);
			out.print(getType(type, clazz.getName(), true));
		}
		@Override void onParameter(StringBuilder sb, Class clazz, String sep, String type, String name) {
			sb.append(sep);
			sb.append(getType(type, clazz.getName(), true));
		}
	};
}
