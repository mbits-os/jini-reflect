package reflect.android;

import java.io.File;

import reflect.android.api.API;
import reflect.android.api.Class;
import reflect.java.ClassHinter;
import reflect.java.SourceCodeParamsHint;

public class AndroidParamsHint extends SourceCodeParamsHint {

	private File m_sdk;
	private API m_api;
	private int m_targetAPI;

	public AndroidParamsHint(File sdk, API api, int targetAPI) { m_sdk = sdk; m_api = api; m_targetAPI = targetAPI; }
	@Override protected File getSourceRoot(String className) { return m_sdk; }
	@Override protected boolean hasClass(String className) { return m_api.find(className, m_targetAPI) != null; }
	@Override protected ClassHinter getClass(String className)
	{
		Class clazz = m_api.find(className, m_targetAPI);
		if (clazz == null)
			return null;
		return new AndroidClassHinter(clazz);
	}
}
