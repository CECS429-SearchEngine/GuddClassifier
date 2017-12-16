import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RocchioClassification {

	public static int applyRocchio(Map<String, Map<String, Double>> classCentroid, Map<String, Double> d) {
		HashMap<String, Double> scores = new HashMap<String, Double>();
		
		// Calculate the euclidean distance between the document and class centroids.
		for (String author : classCentroid.keySet()) {
			double sum = 0;
			for (String term : d.keySet()) {
					if (classCentroid.get(author).containsKey((term)))
						sum += Math.pow(classCentroid.get(author).get(term) - d.get(term), 2);
					else {
						sum += Math.pow(d.get(term), 2);
					}
			}
			scores.put(author, Math.sqrt(sum));
		}
		
		double s1 = scores.get("HAMILTON");
		double s2 = scores.get("MADISON");
		double s3 = scores.containsKey("JAY") ? scores.get("JAY") : Double.MAX_VALUE;

		// classify the document
		if (classCentroid.size() == 3) {
			System.out.println(s1 + " HAMILTON, " + s2 + " MADISON, " + s3 + " JAY.");
			if (s1 < s2 && s1 < s3) return 0;
			if (s2 < s1 && s2 < s3) return 1;
			return 2;
		}
		System.out.println(s1 + " HAMILTON, " + s2 + " MADISON.");
		return s1 < s2 ? 0 : 1;
	}
	
	// ------------------------------------------------------------------------------------------------------
	
	public static Map<String, Map<String, Double>> trainRocchio(String[] C, String D) throws IOException {
		Map<String, Map<String, Double>> classCentroid = new HashMap<String, Map<String, Double>>();
		for (String author : C) {
			IndexBank ib = new IndexBank();
			Map<String, Double> centroid = new HashMap<String, Double>();
			Map<Integer, Map<String, Double>> uv = ib.indexDirectory(D + author);
			
			// Sum all the normalized vector for a given class
			for (Integer i : uv.keySet()) {
				for (String term : uv.get(i).keySet()) {
					if (!centroid.containsKey(term)) {
						centroid.put(term, uv.get(i).get(term));
					} else {
						centroid.replace(term, centroid.get(term) + uv.get(i).get(term));
					}
				}
			}
			
			// divide by the number of documents for a given class.
			for (String term : centroid.keySet()) {
				centroid.replace(term, centroid.get(term) / uv.size());
			}
			classCentroid.put(author, centroid);
		}
		return classCentroid;
	}
	
	
	// ------------------------------------------------------------------------------------------------------
	
	public static void printCentroidComponents(String[] authors, Map<String, Map<String, Double>> centroid) {
		for (String author : authors) {
			Map<String, Double> scores = centroid.get(author);
			System.out.println("Printing first 30 class centroid component for " + author);
			int component = 0;
			for (String term : scores.keySet()) {
				if (component++ > 30) {
					break;
				}
				System.out.println("\t" + term + ": " + scores.get(term));
			}
		}
	}
	
	// ------------------------------------------------------------------------------------------------------
	
	public static void main(String[] args) throws IOException {
		String[] authors = {"HAMILTON", "MADISON", "JAY"};
		String path = "/Users/kuminin/Downloads/federalist-papers/";
		Map<String, Map<String, Double>> classCentroid = trainRocchio(authors, path);
		
		// uncomment line 91 to print the first 30 components for a centroid of a class.
//		printCentroidComponents(authors, classCentroid);
		
		path = path + "DISPUTED";
		IndexBank ib = new IndexBank();
		Map<Integer, Map<String, Double>> uv = ib.indexDirectory(path);
		for (Integer d : uv.keySet()) {
			System.out.print(ib.fileNames.get(d) + " scores is : ");
			System.out.println("Author Classified as: " + authors[applyRocchio(classCentroid, uv.get(d))] + "\n");
		}
	}
}
