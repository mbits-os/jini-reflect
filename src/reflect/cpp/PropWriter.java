package reflect.cpp;

import java.io.PrintStream;

import reflect.android.api.Class;
import reflect.android.api.Property;

public abstract class PropWriter extends TypeUtils {
	abstract void onProperty(PrintStream out, String indent, Class clazz, boolean isStatic, String type, String name);

	public static void print(PrintStream out, String indent, Class clazz, PropWriter cb) {
		for (Property prop: clazz.getProperties()) {
			if (!isKnownClassOrBuiltin(prop.getSignature()))
				continue;
			cb.onProperty(out, indent, clazz, prop.isStatic(), prop.getSignature(), prop.getName());
		}
	}
}
