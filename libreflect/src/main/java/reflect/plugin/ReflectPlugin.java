package reflect.plugin;

import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.Namespace;

import com.mbits.plugins.Plugin;

public interface ReflectPlugin extends Plugin {
	boolean wantsArguments();
	void onAddArguments(ArgumentGroup group);
	void onReadArguments(Namespace ns) throws Exception;
}
