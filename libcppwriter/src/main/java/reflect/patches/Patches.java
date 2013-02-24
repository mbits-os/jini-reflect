package reflect.patches;

import java.util.HashMap;
import java.util.Map;

/**
 * The repository for patches. This class is used by the
 * {@link reflect.cpp.CppWriter CppWriter} to locate patches for classes
 * it now transforms. The patch is looked up only once per header file,
 * which means patch for only the outer-most class is looked up.
 * 
 * <p>Plugins should add their patches during their own constructor.
 * 
 * @see reflect.cpp.CppWriter CppWriter
 * @see reflect.android.plugin.AndroidPlugin#AndroidPlugin() AndroidPlugin
 */
public class Patches {
	static class Impl {
		private Map<String, Patch> m_patches = new HashMap<String, Patch>();
		private static Patch s_dummy = new Patch();

		public Patch getPatch(String className) {
			if (m_patches.containsKey(className))
				return m_patches.get(className);
			return s_dummy;
		}
		
	}

	private Patches() {}
	private static Impl impl = null;
	private static boolean hasImpl() {
		if (impl == null)
			impl = new Impl();
		return impl != null;
	}

	/**
	 * The look-up interface used by {@link reflect.cpp.CppWriter CppWriter}. 
	 * 
	 * @param className The name of the class to look up
	 * @return Pointer to the patch, if found, <code>null</code> otherwise
	 */
	public static Patch getPatch(String className) {
		if (!hasImpl()) return null;
		return impl.getPatch(className);
	}

	/**
	 * The registration interface for plugins. 
	 * 
	 * @param className The name of the class to register
	 * @param patch The patch for the registered class
	 */
	public static void put(String className, Patch patch) {
		if (!hasImpl()) return;
		impl.m_patches.put(className, patch);
	}
}
