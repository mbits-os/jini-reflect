package reflect.plugin;

import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.Namespace;

public interface Plugin {

	String getName();
	boolean wantsArguments();
	void onAddArguments(ArgumentGroup group);
	void onReadArguments(Namespace ns) throws Exception;
}
