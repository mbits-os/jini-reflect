package reflect;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class CodeExceptions {
	private static Map<String, String> s_map = new HashMap<String, String>();
	public static void readExceptions() {
		BufferedReader br = null;
		try {
			InputStream    fis;
			String         line;
			fis = new FileInputStream("exceptions.txt");
			br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
			while ((line = br.readLine()) != null) {
				final String[] chunks = line.split(":");
			    if (chunks.length != 3) continue;
			    addException(chunks[0], chunks[1], chunks[2]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null)
				try { br.close(); } catch (IOException e) {}
		}
	}

	private static void addException(String context, String symbol, String resolved) {
		s_map.put(context + ":" + symbol, resolved);
	}
	public static String get(String context, String symbol) {
		if (s_map.containsKey(context + ":" + symbol))
			return s_map.get(context + ":" + symbol);
		if (s_map.containsKey("*:" + symbol))
			return s_map.get("*:" + symbol);
		return null;
	}

}
