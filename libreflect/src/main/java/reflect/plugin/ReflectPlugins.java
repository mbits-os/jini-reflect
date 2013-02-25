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
 
package reflect.plugin;

import java.io.File;

import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import com.mbits.plugins.Plugins;

/**
 * Provides implementation of the <code>Plugins.Impl</code>, that uses <code>ReflectPlugin</code>.
 */
public class ReflectPlugins extends Plugins {
	/**
	 * @hide
	 */
	public ReflectPlugins() {}

	private static class Impl extends Plugins.Impl<ReflectPlugin> {
		public void onAddArguments(ArgumentParser parser) {
			for (ReflectPlugin plug: m_plugins) {
				if (!plug.wantsArguments()) continue;
				ArgumentGroup group = parser.addArgumentGroup(plug.getName() + " arguments");
				plug.onAddArguments(group);
			}
		}

		public void onReadArguments(Namespace ns) throws Exception {
			for (ReflectPlugin plug: m_plugins) {
				if (!plug.wantsArguments()) continue;
				plug.onReadArguments(ns);
			}
		}
	}

	private static Impl impl = null;
	private static boolean hasImpl() {
		if (impl == null)
			impl = new Impl();
		return impl != null;
	}

	/**
	 * Loads plugins from specified directory.
	 * 
	 * @param dir the directory for the plugins
	 */
	public static void loadPlugins(File dir) {
		if (!hasImpl()) return;
		try {
			impl.loadPlugins(dir, ReflectPlugin.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called before program arguments are parsed. Allows to
	 * enhance the argument parser with additional parameters.
	 * 
	 * @param parser the parser for the program arguments
	 */
	public static void onAddArguments(ArgumentParser parser) {
		if (!hasImpl()) return;
		impl.onAddArguments(parser);
	}

	/**
	 * Called after arguments have been parsed and builtin
	 * parameters have been extracted.
	 * 
	 * @param ns The container with the parameters.
	 * @throws Exception
	 */
	public static void onReadArguments(Namespace ns) throws Exception {
		if (!hasImpl()) return;
		impl.onReadArguments(ns);
	}

}
