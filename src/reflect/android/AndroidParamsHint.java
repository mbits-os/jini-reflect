package reflect.android;

import java.io.File;
import java.io.IOException;

import reflect.ParamsHint;
import reflect.SourceCodeParamsHint;
import reflect.android.api.API;
import reflect.android.api.Class;
import reflect.utils.ClassPathHack;

public class AndroidParamsHint extends SourceCodeParamsHint {

	public static final class HintCreator implements ParamsHint.HintCreator {

		private File m_sdk_root;
		private File m_sdk;
		private API m_api;
		private int m_targetAPI;

		public HintCreator(String api) throws IOException {
			final String androidSDK = System.getenv("ANDROID_SDK");
			if (androidSDK == null)
			{
				throw new RuntimeException("Environment variable ANDROID_SDK is missing.");
			}
			m_sdk_root = new File(androidSDK);
			m_api = new API();
			m_targetAPI = Integer.valueOf(api);
			m_sdk    = new File(m_sdk_root, "sources" + File.separator + "android-" + api);
			ClassPathHack.addFile(new File(m_sdk_root, "platforms" + File.separator + "android-" + api + File.separator + "android.jar"));
			ClassPathHack.addFile(new File(m_sdk_root, "add-ons" + File.separator + "addon-google_apis-google-" + api + File.separator + "libs" + File.separator + "maps.jar"));
		}

		@Override public ParamsHint createHint() {
			return new AndroidParamsHint(m_sdk, m_api, m_targetAPI);
		}

		public boolean read() { return m_api.read(m_sdk_root); }
		public String[] classes() { return m_api.getClasses(m_targetAPI); }

		public Class get(String clazz) {
			return m_api.get(clazz, m_targetAPI);
		}
		
	}

	private File m_sdk;
	private API m_api;
	private int m_targetAPI;

	public AndroidParamsHint(File sdk, API api, int targetAPI) { m_sdk = sdk; m_api = api; m_targetAPI = targetAPI; }
	@Override protected File getSourceRoot(String className) { return m_sdk; }

}
