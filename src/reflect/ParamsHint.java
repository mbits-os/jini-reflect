package reflect;

public interface ParamsHint {
	public static interface HintCreator {
		public abstract ParamsHint createHint();
	}
	public abstract void getHints(String className);
}
