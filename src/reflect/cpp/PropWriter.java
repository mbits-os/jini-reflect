package reflect.cpp;

import java.io.PrintStream;

import reflect.api.Class;
import reflect.api.Property;

public abstract class PropWriter extends TypeUtils {
	private PrintStream out;
	private Class clazz;
	private Template tmplt = new Template();
	private String indent;
	protected boolean isStatic;

	private void onProperty(PrintStream _out, String _indent, Class _clazz, String _ns_dummy, boolean _isStatic, String type, String name) {
		String var;
		String classRetType;
		String nsRetType;
		String rawRetType;

		out = _out;
		clazz = _clazz;
		indent = _indent;
		isStatic = _isStatic;

		tmplt.clear();
		tmplt.setHost(this);

		var = "m_" + name;
		rawRetType = type;

		classRetType = j2c(getType(rawRetType, clazz.getName()));
		nsRetType = j2c(getType(rawRetType, _ns_dummy));

		tmplt.put("name", name);
		tmplt.put("var", var);
		tmplt.put("rawRetType", rawRetType);
		tmplt.put("classRetType", classRetType);
		tmplt.put("nsRetType", nsRetType);

		onProperty();
	}
	abstract void onProperty();

	public void put(String key, String replacement) {
		tmplt.put(key, replacement);
	}

	void templateLine(String tmplt) {
		out.print(indent);
		this.tmplt.println(out, tmplt);
	}

	public static void print(PrintStream out, String indent, Class clazz, PropWriter cb) {
		final String ns_dummy = clazz.getPackage() + ".?";
		for (Property prop: clazz.getProperties()) {
			if (!isKnownClassOrBuiltin(prop.getSignature()))
				continue;
			cb.onProperty(out, indent, clazz, ns_dummy, prop.isStatic(), prop.getSignature(), prop.getName());
		}
	}
}
