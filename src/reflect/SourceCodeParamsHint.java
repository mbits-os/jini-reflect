package reflect;

import java.io.File;

public abstract class SourceCodeParamsHint implements ParamsHint {

	protected abstract File getSourceRoot();

	@Override public ClassHint[] getHints(String className) {
		File java = null;
		return null;
	}
}
