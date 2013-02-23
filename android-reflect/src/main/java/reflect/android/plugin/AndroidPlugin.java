package reflect.android.plugin;

import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.Namespace;
import reflect.android.API;
import reflect.android.patches.BitmapPatch;
import reflect.api.Classes;
import reflect.patches.Patches;
import reflect.plugin.Plugin;

public class AndroidPlugin implements Plugin {
	public AndroidPlugin() {
		Patches.put("android.graphics.Bitmap", new BitmapPatch());
	}

	@Override public String getName() { return "Android API"; }

	@Override public boolean wantsArguments() { return true; }
	@Override public void onAddArguments(ArgumentGroup group) {
		group.addArgument("-a", "--android").metavar("API")
			.dest("targetAPI")
			.required(true)
			.type(Integer.class)
			.help("Android API Level (e.g. -a 17 for Android 4.2)");
	}

	@Override
	public void onReadArguments(Namespace ns) throws Exception {
		int sdk = ns.getInt("targetAPI");
		final API android = new API();
		Classes.addApi(android);

		android.setTargetApi(sdk);
	}
}

