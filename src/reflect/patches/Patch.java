package reflect.patches;

import java.io.PrintStream;

import reflect.api.Class;

public class Patch {
	public void onIncludes(PrintStream out) {}
	public void onConstructors(PrintStream out, String indent, Class clazz) {}
	public void onObjectMembers(PrintStream out, String indent, Class clazz) {}
	public void onNamespaceStart(PrintStream out) {}
	public void onNamespaceEnd(PrintStream out) {}
}
