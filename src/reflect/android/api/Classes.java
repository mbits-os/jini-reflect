package reflect.android.api;

import java.io.File;
import java.io.IOException;

import reflect.android.AndroidParamsHint;
import reflect.utils.ClassPathHack;

public class Classes {
	static class Impl {
		private File m_sdk_root;
		private File m_sdk;
		private API m_api;
		private int m_targetAPI;

		Impl(int targetAPI) throws IOException {
			final String androidSDK = System.getenv("ANDROID_SDK");
			if (androidSDK == null)
			{
				throw new RuntimeException("Environment variable ANDROID_SDK is missing.");
			}
			m_sdk_root = new File(androidSDK);
			m_api = new API();
			m_targetAPI = targetAPI;
			m_sdk    = new File(m_sdk_root, "sources" + File.separator + "android-" + targetAPI);
			ClassPathHack.addFile(new File(m_sdk_root, "platforms" + File.separator + "android-" + targetAPI + File.separator + "android.jar"));
			ClassPathHack.addFile(new File(m_sdk_root, "add-ons" + File.separator + "addon-google_apis-google-" + targetAPI + File.separator + "libs" + File.separator + "maps.jar"));
		}

		Class forName(String className) {
			Class clazz = m_api.get(className, m_targetAPI);
			if (clazz == null) return null;
			clazz.update();
			return clazz;
		}

		boolean read() { return m_api.read(m_sdk_root); }
	}

	private static Impl impl = null;

	public static Class forName(String className) {
		if (impl == null)
			return null;
		return impl.forName(className);
	}

	public static boolean setTargetApi(int target_api) {
		try {
			impl = new Impl(target_api);
			return impl.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	static void getHints(String className) {
		if (impl != null)
			new AndroidParamsHint(impl.m_sdk, impl.m_api, impl.m_targetAPI).getHints(className);
	}
	
	static public String[] classNames() {
		if (impl == null) return new String[0];
		return impl.m_api.getClasses(impl.m_targetAPI);
	}
}
