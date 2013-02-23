package reflect.cpp;

import java.util.List;

public class TypeUtils {
	protected static boolean s_utf8 = false;
	public static String repeat(String s, int n) {
		if (n == 0) return "";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; ++i)
			sb.append(s);
		return sb.toString();
	}

	public static String arrayed(int arrays, String inner) {
		return repeat("jni::Array< ", arrays) + inner + repeat(" >", arrays);
	}

	public static String getType(String signature, String className) {
		return getType(signature, className, false);
	}
	public static String getType(String signature, String className, boolean allowUtf8Conversion) {
		int arrays = 0;
		while (signature.charAt(arrays) == '[') ++arrays;
		if (arrays > 1) return "jobjectArray";

		switch(signature.charAt(arrays))
		{
		case 'Z': return arrayed(arrays, "bool");
		case 'B': return arrayed(arrays, "jbyte");
		case 'C': return arrayed(arrays, "jchar");
		case 'S': return arrayed(arrays, "jshort");
		case 'I': return arrayed(arrays, "jint");
		case 'J': return arrayed(arrays, "jlong");
		case 'F': return arrayed(arrays, "jfloat");
		case 'D': return arrayed(arrays, "jdouble");
		case 'V': return "void";
		case 'L':
			return arrayed(arrays,
					getClass(signature.substring(arrays + 1, signature.length()-1), className, allowUtf8Conversion)
					);
		}
		return signature;
	}

	public static String getClass(String signature, String className) {
		return j2c(innerGetClass(signature, className, false));
	}

	public static String getClass(String signature, String className, boolean allowUtf8Conversion) {
		return j2c(innerGetClass(signature, className, allowUtf8Conversion));
	}

	public static String innerGetClass(String signature, String className, boolean allowUtf8Conversion) {
		if (allowUtf8Conversion && s_utf8 && signature.equals("java.lang.String"))
			return "const char*";

		int pkgPos = className.lastIndexOf('.');

		// a.b.c.D$E @ a.b.c.D$E --> E
		// a.b.c.D @ a.b.c.D --> D
		if (signature.equals(className))
		{
			int pos = signature.lastIndexOf('$');
			if ( pos >= 0) return signature.substring(pos + 1);
			pos = signature.lastIndexOf('.');
			if ( pos >= 0) return signature.substring(pos + 1);
			return signature;
		}

		// a.b.c.D$E$F @ a.b.c.D --> E$F
		if (signature.startsWith(className + "$"))
			return signature.substring(className.length() + 1);

		// a.b.c.D$E$F @ a.b.c.X --> D$E$F
		final String pkg = className.substring(0, pkgPos + 1);
		if (signature.startsWith(pkg))
			return signature.substring(pkgPos + 1);

		// a.b.c.D$E$F @ a.b.y.X --> c.D$E$F
		// TODO
		
		// java.lang.D$E @ a.b.c.X --> D$E
		if (signature.startsWith("java.lang."))
			return signature.substring(10);

		return signature;
	}

	public static String j2c(String j) {
		return j.replace(".", "::").replace("$", "::");
	}

	public static List<String> s_classes = null; 
	public static boolean isKnownClass(String className) {
		if (s_classes == null) return true;
		int pos = className.lastIndexOf('.');
		if (pos != -1) {
			if (className.substring(0, pos).equals("java.lang"))
				return true;
		}
		pos = className.lastIndexOf('$');
		if (pos != -1) className = className.substring(0, pos);

		for(String s: s_classes)
			if (s.equals(className))
				return true;

		return false;
	}

	public static boolean isKnownClassOrBuiltin(String type) {
		int array = 0;
		while (array < type.length() && type.charAt(array) == '[')
			++array;

		if (array < type.length() && type.charAt(array) == 'L')
			return isKnownClass(type.substring(array + 1, type.length() - 1));

		return true;
	}
}
