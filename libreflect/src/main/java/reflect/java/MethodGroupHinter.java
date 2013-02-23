package reflect.java;

import java.util.Vector;

public interface MethodGroupHinter {
	public MethodHinter find(String retType, Vector<String> types);
}
