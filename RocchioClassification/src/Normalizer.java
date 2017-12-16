

import java.util.HashSet;
import java.util.Set;

public class Normalizer {

	public static String normalize(String token) {
		return token.replaceAll("^[^a-zA-Z0-9]+|[^a-zA-Z0-9]+$|\'", "");
	}
}
