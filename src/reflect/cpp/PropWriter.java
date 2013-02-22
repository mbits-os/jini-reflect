package reflect.cpp;

import java.io.PrintStream;

import reflect.android.api.Class;
import reflect.android.api.Property;

public abstract class PropWriter extends TypeUtils {
	abstract void onProperty(PrintStream out, Object o, String indent, Class clazz, boolean isStatic, String type, String name);

	public void printProps(PrintStream out, Object o, String indent, Class clazz, PropWriter cb) {
		for (Property prop: clazz.getProperties()) {
			if (!isKnownClassOrBuiltin(prop.getSignature()))
				continue;
			cb.onProperty(out, o, indent, clazz, prop.isStatic(), prop.getSignature(), prop.getName());
		}
	}
	public void printProps(PrintStream out, String indent, Class clazz, PropWriter cb) {
		printProps(out, null, indent, clazz, cb);
	}
}
