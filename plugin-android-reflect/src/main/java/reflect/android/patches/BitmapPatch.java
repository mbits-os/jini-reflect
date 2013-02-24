package reflect.android.patches;

import java.io.PrintStream;

import reflect.api.Class;
import reflect.patches.Patch;

public class BitmapPatch extends Patch {
	public void onIncludes(PrintStream out)
	{
		out.println("#include <android/bitmap.h>");
	}

	@Override
	public void onNamespaceStart(PrintStream out) {
		out.println("\tstruct BitmapLockInfo");
		out.println("\t{");
		out.println("\t\tAndroidBitmapInfo m_info;");
		out.println("\t\tuint8_t*          m_pixels;");
		out.println("\t\tBitmapLockInfo(): m_pixels(NULL) {}");
		out.println("\t};");
		out.println();
	}

	@Override
	public void onObjectMembers(PrintStream out, String indent, Class clazz) {
		if (!clazz.getName().equals("android.graphics.Bitmap"))
			return;
		out.println();
		out.print(indent); out.println("//locking goes here");
		out.print(indent); out.println("BitmapLockInfo lock()");
		out.print(indent); out.println("{");
		out.print(indent); out.println("\tJNIEnv* env = jni::Env();");
		out.print(indent); out.println("\tBitmapLockInfo bli;");
		out.println();
		out.print(indent); out.println("\tint ret = AndroidBitmap_getInfo(env, m_this, &bli.m_info);");
		out.print(indent); out.println("\tif (ret != ANDROID_BITMAP_RESULT_SUCCESS) return bli;");
		out.print(indent); out.println("\tif (bli.m_info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) return bli;");
		out.print(indent); out.println("\tret = AndroidBitmap_lockPixels(env, m_this, (void**)&bli.m_pixels);");
		out.print(indent); out.println("\tif (ret != ANDROID_BITMAP_RESULT_SUCCESS) bli.m_pixels = NULL;");
		out.print(indent); out.println("\treturn bli;");
		out.print(indent); out.println("}");
		out.println();
		out.print(indent); out.println("void unlock()");
		out.print(indent); out.println("{");
		out.print(indent); out.println("\tAndroidBitmap_unlockPixels(jni::Env(), m_this);");
		out.print(indent); out.println("}");
	}
}
