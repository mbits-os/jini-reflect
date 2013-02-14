package reflect.android;

import java.io.File;

import reflect.ParamsHint;
import reflect.SourceCodeParamsHint;

public class AndroidParamsHint extends SourceCodeParamsHint {

	public static final class HintCreator implements ParamsHint.HintCreator {

		private File m_sdk;

		HintCreator(String api) {
			final String androidSDK = System.getenv("ANDROID_SDK");
			if (androidSDK == null)
			{
				System.out.println("Environment variable ANDROID_SDK is missing.");
				return;
			}
			final File SDK = new File(androidSDK);
			m_sdk = new File(SDK, "sources" + File.separator + "android_" + api);
		}
		@Override public ParamsHint createHint() {
			return new AndroidParamsHint(m_sdk);
		}
		
	}

	private File m_api;

	public AndroidParamsHint(File api) { m_api = api; }
	@Override protected File getSourceRoot(String className) { return m_api; }

}
