/*
 * Copyright (C) 2013 midnightBITS
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
 
package reflect.android.patches;

import java.io.PrintStream;

import reflect.api.Class;
import reflect.patches.Patch;

/**
 * Adds support for the Android NDK's <code>AndroidBitmapInfo</code> and API
 * to lock and unlock pixmap inside the Bitmap.
 * 
 * <p>To lock/unlock and access the pixmap and info:
 * 
 * <pre class="prettyprint">
 * Bitmap bm = ...
 * BitmapLockInfo bmi = bm.lock();
 * if (bmi.m_pixels != NULL) {
 *     ... = bmi.m_info.width;
 *     //...
 *     bm.unlock();
 * }
 * </pre>
 */
public class BitmapPatch extends Patch {
	/**
	 * @hide
	 */
	public BitmapPatch() {}

	/**
	 * Adds <code>&lt;android/bitmap&gt;</code> to the list of includes.
	 * @see Patch#onIncludes(PrintStream)
	 */
	@Override
	public void onIncludes(PrintStream out)
	{
		out.println("#include <android/bitmap.h>");
	}

	/**
	 * Introduces <code>BitmapLockInfo</code>, a structure holding
	 * the information to lock/unlock the pixmap.
	 * @see Patch#onNamespaceStart(PrintStream)
	 */
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

	/**
	 * Introduces <code>android::graphics::Bitmap::lock()</code> and
	 * <code>android::graphics::Bitmap::unlock()</code> - two methods
	 * to acquire and release the pixmap data.
	 * @see Patch#onObjectMembers(PrintStream, String, Class)
	 */
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
