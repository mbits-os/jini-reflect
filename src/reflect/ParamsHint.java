package reflect;

public interface ParamsHint {
	public static interface HintCreator {
		public abstract ParamsHint createHint();
	}
	public abstract ClassHint[] getHints(String className);
}
