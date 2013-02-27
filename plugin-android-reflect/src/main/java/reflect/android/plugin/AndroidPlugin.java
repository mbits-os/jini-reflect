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
 
package reflect.android.plugin;

import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.Namespace;
import reflect.android.API;
import reflect.android.patches.BitmapPatch;
import reflect.api.Classes;
import reflect.patches.Patches;
import reflect.plugin.ReflectPlugin;

/**
 * Android API Plugin. Adds the API and sources to the {@link reflect.api.Classes}
 * and the {@link reflect.android.patches.BitmapPatch} to the
 * {@link reflect.patches.Patches}. 
 */
public class AndroidPlugin implements ReflectPlugin {
	/**
	 * Constructs a new instance of <code>AndroidPlugin</code>.
	 * Called by the Plugin Manager. 
	 */
	public AndroidPlugin() {
		Patches.put("android.graphics.Bitmap", new BitmapPatch());
	}

	/**
	 * Plugin name
	 * @return "Android API"
	 * @see com.mbits.plugins.Plugin#getName()
	 */
	@Override public String getName() { return "Android API"; }

	/**
	 * Reports the need for the program arguments.
	 * 
	 * @return true
	 * @see reflect.plugin.ReflectPlugin#wantsArguments()
	 */

	@Override public boolean wantsArguments() { return true; }

	/**
	 * Adds <tt>-a <em>API</em></tt> and <tt>--android <em>API</em></tt> arguments.
	 * @param group the group provided by the Plugin Manager.
	 * @see reflect.plugin.ReflectPlugin#onAddArguments(net.sourceforge.argparse4j.inf.ArgumentGroup) ReflectPlugin.onAddArguments(...)
	 */
	@Override public void onAddArguments(ArgumentGroup group) {
		group.addArgument("-a", "--android").metavar("API")
			.dest("targetAPI")
			.required(true)
			.type(Integer.class)
			.help("Android API Level (e.g. -a 17 for Android 4.2)");
	}

	/**
	 * Reads the arguments defined in {@link #onAddArguments(net.sourceforge.argparse4j.inf.ArgumentGroup) onAddArguments(...)}
	 * and sets the Android API Level. 
	 * @see reflect.plugin.ReflectPlugin#onReadArguments(net.sourceforge.argparse4j.inf.Namespace) ReflectPlugin.onReadArguments(...)
	 * @see reflect.android.API
	 */
	@Override
	public void onReadArguments(Namespace ns) throws Exception {
		int sdk = ns.getInt("targetAPI");
		final API android = new API();
		Classes.addApi(android);

		android.setTargetApi(sdk);
	}
}

