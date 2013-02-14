package reflect.android;

import java.io.File;

import reflect.ParamsHint;
import reflect.SourceCodeParamsHint;

public class AndroidParamsHint extends SourceCodeParamsHint {

	public static final class HintCreator implements ParamsHint.HintCreator {

		private String m_sdk;

		HintCreator(String sdk) {
			m_sdk = sdk;
		}
		@Override public ParamsHint createHint() {
			return new AndroidParamsHint(m_sdk);
		}
		
	}

	private File m_api;

	public AndroidParamsHint(String api) {
		final String androidSDK = System.getenv("ANDROID_SDK");
		if (androidSDK == null)
		{
			System.out.println("Environment variable ANDROID_SDK is missing.");
			return;
		}
		File SDK = new File(androidSDK);
		m_api = new File(SDK, "sources" + File.separator + "android_" + api);
	}

	@Override protected File getSourceRoot(String className) { return m_api; }

}
