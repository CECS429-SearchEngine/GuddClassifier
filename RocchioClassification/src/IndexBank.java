

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexBank {
	
	private PositionalInvertedIndex PII;
	private List<Double> scores;
	private List<Double> docLengths;
	List<String> fileNames;
	
	// ------------------------------------------------------------------------------------------------------
	
	public IndexBank() {
		this.PII = new PositionalInvertedIndex();
		this.scores = new ArrayList<Double>();
		this.docLengths = new ArrayList<Double>();
		this.fileNames = new ArrayList<String>();
	}

	// ------------------------------------------------------------------------------------------------------
	
	public List<Double> getDocLengths() {
		return this.docLengths;
	}
	
	public List<Double> getScores() {
		return this.scores;
	}

	// ------------------------------------------------------------------------------------------------------
	
	public PositionalInvertedIndex getPositionalInvertedIndex() {
		return this.PII;
	}
	
	// ------------------------------------------------------------------------------------------------------
	
	public Map<Integer, Map<String, Double>> indexDirectory(String path) throws IOException {
		final Path currentWorkingPath = Paths.get(path).toAbsolutePath();
		
		// the list of file names that were processed.
		Map<Integer, Map<String, Double>> UV = new HashMap<Integer, Map<String, Double>>();
		List<String> filenames = new ArrayList<String>();
		// This is our standard "walk through all .json files" code.
		Files.walkFileTree(currentWorkingPath, new SimpleFileVisitor<Path>() {
			int documentID = 0;

			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
				// make sure we only process the current working directory.
				if (currentWorkingPath.equals(dir)) {
					return FileVisitResult.CONTINUE;
				}
				return FileVisitResult.SKIP_SUBTREE;
			}

			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				// only process .json files
				String[] split = file.toString().split("/");
				if (file.toString().endsWith(".txt")) {
					// we have found a .json file; add its name to the fileName list,
					// then index the file and increase the document ID counter.
					filenames.add(split[split.length - 1]);
					UV.put(documentID, addTokens(file.toFile(), documentID));
					documentID++;
				}
				return FileVisitResult.CONTINUE;
			}

			// don't throw exceptions if files are locked/other errors occur.
			public FileVisitResult visitFileFailed(Path file, IOException e) {
				return FileVisitResult.CONTINUE;
			}
		});
		this.fileNames = filenames;
		return UV;
	}
	
	// ------------------------------------------------------------------------------------------------------	
	
	public void reset() {
		this.PII.resetIndex();
		this.docLengths = new ArrayList<Double>();
	}
	
	// ------------------------------------------------------------------------------------------------------
	
	private Map<String, Double> addTokens(File file, int docId) {
		int position = 0;	// keep track of the positions for each token.
		// Map to keep track of term Frequency inside each document.
		Map<String, Integer> termFrequency = new HashMap<String, Integer>();
		TokenStream dp = null;
		try {
			dp = new SimpleTokenStream(file);
		} catch (IOException e) {
			System.out.println(e.toString());
			System.exit(1);
		}
		
		while (dp.hasNextToken()) {
			String type = dp.nextToken();
			
			if (type == null) continue;	// skip the proceeding instructions if the term is an empty string.
			type = Normalizer.normalize(type);
			this.PII.add(type, docId, position);
			addTermFrequency(termFrequency, type);
			position++;	// Increment for each token.
		}
		Map<String, Double> uv = calcUnitVector(termFrequency);
		
		// Uncomment 124-127 to print the first 30 components of the normalized vector for paper_52.txt
//		String filename = file.toString().split("/")[file.toString().split("/").length - 1];
//		if (filename.equals("paper_52.txt")) {
//			printUnitVectorComponents(uv, filename);
//		}
		return uv;
	}
	
	// ------------------------------------------------------------------------------------------------------
	
	private void addTermFrequency(Map<String, Integer> termFrequency, String term) {
		if (!termFrequency.containsKey(term)) {
			termFrequency.put(term, 0);
		}
		termFrequency.replace(term, termFrequency.get(term) + 1);
	}
	
	// ------------------------------------------------------------------------------------------------------

	private Map<String, Double> calcUnitVector(Map<String, Integer> termFrequency) {
		double sum = 0;
		Map<String, Double> unitVector = new HashMap<String, Double>();
		for (String term : termFrequency.keySet()) {
			double score = calculateScore(termFrequency.get(term));
			unitVector.put(term, score);
			sum += (score * score); 
		}
		double length = Math.sqrt(sum);
		
		// Uncomment 153, 156, and 158 to see the magnitude of the 
//		double sum2 = 0;
		for (String term : termFrequency.keySet()) {
			unitVector.replace(term, unitVector.get(term) / length);
//			sum2 += Math.pow(unitVector.get(term), 2);
		}
//		System.out.println(Math.sqrt(sum2));
		return unitVector;
	}

	// ------------------------------------------------------------------------------------------------------
	
	private double calculateScore(int frequency) {
		return 1 + Math.log(frequency);
	}
	
	// ------------------------------------------------------------------------------------------------------
	
	private void printUnitVectorComponents(Map<String, Double> uv, String filename) {
		System.out.println("Printing first 30 component of normalized vector for " + filename);
		int component = 0;
		for (String term : uv.keySet()) {
			if (component++ >= 30) {
				break;
			}
			System.out.println("\t" + term + ": " + uv.get(term));
		}
	}
}
