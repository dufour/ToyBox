package toybox.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * A collection input/output utility functions. 
 * 
 * @author Bruno Dufour (dufour@iro.umontreal.ca)
 */
public final class IO {
	private IO() {
		// no instances
	}
	
	public static String readFull(InputStream s) throws IOException {
		return readFully(new BufferedReader(new InputStreamReader(s)));
	}
	
	public static String readFully(Reader r) throws IOException {
		if (r == null) {
            throw new IllegalArgumentException("Null stream");
        }
		
		StringBuilder sb = new StringBuilder();

		while (true) {
			int c = r.read();
			if (c < 0) {
				break;
			}

			sb.append((char) c);
		}

		return sb.toString();
	}
}
