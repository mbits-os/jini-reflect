package reflect.patches;

import java.io.PrintStream;

import reflect.android.api.Class;

public class BitmapPatch extends Patch {
	public void headerIncludes(PrintStream out)
	{
		out.println("#include <android/bitmap.h>");
	}

	@Override
	public void additionalOperations(PrintStream out, String indent, Class clazz) {
		if (!clazz.getName().equals("android.graphics.Bitmap"))
			return;
		out.println();
		out.print(indent); out.println("//locking goes here");
	}
}
