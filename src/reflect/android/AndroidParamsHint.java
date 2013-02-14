package reflect.android;

import java.io.File;
import java.io.IOException;

import reflect.ParamsHint;
import reflect.SourceCodeParamsHint;
import reflect.utils.ClassPathHack;

public class AndroidParamsHint extends SourceCodeParamsHint {

	public static final class HintCreator implements ParamsHint.HintCreator {

		private File m_sdk;

		public HintCreator(String api) throws IOException {
			final String androidSDK = System.getenv("ANDROID_SDK");
			if (androidSDK == null)
			{
				throw new RuntimeException("Environment variable ANDROID_SDK is missing.");
			}
			final File SDK = new File(androidSDK);
			m_sdk    = new File(SDK, "sources" + File.separator + "android-" + api);
			File jar = new File(SDK, "platforms" + File.separator + "android-" + api + File.separator + "android.jar");
			ClassPathHack.addFile(jar);
		}

		@Override public ParamsHint createHint() {
			return new AndroidParamsHint(m_sdk);
		}
		
	}

	private File m_api;

	public AndroidParamsHint(File api) { m_api = api; }
	@Override protected File getSourceRoot(String className) { return m_api; }

}
