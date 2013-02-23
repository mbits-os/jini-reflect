package reflect.plugin;

import java.io.File;

import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import com.mbits.plugins.Plugins;

public class ReflectPlugins extends Plugins {
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

	public static void loadPlugins(File file) {
		if (!hasImpl()) return;
		impl.loadPlugins(file, ReflectPlugin.class);
	}

	public static void onAddArguments(ArgumentParser parser) {
		if (!hasImpl()) return;
		impl.onAddArguments(parser);
	}

	public static void onReadArguments(Namespace ns) throws Exception {
		if (!hasImpl()) return;
		impl.onReadArguments(ns);
	}

}
