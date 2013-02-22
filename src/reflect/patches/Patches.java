package reflect.patches;

import java.util.HashMap;
import java.util.Map;

public class Patches {
	static class Impl {
		private Map<String, Patch> m_patches = new HashMap<String, Patch>();
		private static Patch s_dummy = new Patch();

		Impl() {
			m_patches.put("java.lang.String", new StringPatch());
			m_patches.put("android.graphics.Bitmap", new BitmapPatch());
		}

		public Patch getPatch(String className) {
			if (m_patches.containsKey(className))
				return m_patches.get(className);
			return s_dummy;
		}
		
	}

	private static Impl impl = null;
	private static boolean hasImpl() {
		if (impl == null)
			impl = new Impl();
		return impl != null;
	}

	public static Patch getPatch(String className) {
		if (!hasImpl()) return null;
		return impl.getPatch(className);
	}
}
