package reflect.patches;

import java.io.PrintStream;

import reflect.android.api.Class;

public class Patch {
	public void headerIncludes(PrintStream out) {}
	public void codeIncludes(PrintStream out) {}
	public void constructorDeclarations(PrintStream out, String indent, Class clazz) {}
	public void additionalOperations(PrintStream out, String indent, Class clazz) {}
}
