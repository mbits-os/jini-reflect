package reflect.android;

import java.io.File;

import reflect.SourceCodeParamsHint;

public class AndroidSourceCodeParamsHint extends SourceCodeParamsHint {

	private File m_api;

	AndroidSourceCodeParamsHint(String api) {
		final String androidSDK = System.getenv("ANDROID_SDK");
		if (androidSDK == null)
		{
			System.out.println("Environment variable ANDROID_SDK is missing.");
			return;
		}
		File SDK = new File(androidSDK);
		m_api = new File(SDK, "sources" + File.separator + "android_" + api);
	}

	@Override protected File getSourceRoot() { return m_api; }

}
