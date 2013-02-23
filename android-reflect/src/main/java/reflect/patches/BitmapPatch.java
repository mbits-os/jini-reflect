package reflect.patches;

import java.io.PrintStream;

import reflect.api.Class;

public class BitmapPatch extends Patch {
	public void onIncludes(PrintStream out)
	{
		out.println("#include <android/bitmap.h>");
	}

	@Override
	public void onNamespaceStart(PrintStream out) {
		out.println("\t//locking class goes here");
	}

	@Override
	public void onObjectMembers(PrintStream out, String indent, Class clazz) {
		if (!clazz.getName().equals("android.graphics.Bitmap"))
			return;
		out.println();
		out.print(indent); out.println("//locking goes here");
	}
}
