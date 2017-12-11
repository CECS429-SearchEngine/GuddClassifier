import java.io.File;
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
import java.util.Set;

public class SimpleIndexer {
	private Map<Integer, List<String>> terms;
	public SimpleIndexer() {
		terms = new HashMap<Integer, List<String>> ();
	}
	public Map<Integer, List<String>> getTerms() {
		return terms;
	}
	public void indexDirectory(String path) throws IOException {
		final Path currentWorkingPath = Paths.get(path).toAbsolutePath();
		
		
		// This is our standard "walk through all .json files" code.
		Files.walkFileTree(currentWorkingPath, new SimpleFileVisitor<Path>() {
			int docID = 0;
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
				// make sure we only process the current working directory.
				if (currentWorkingPath.equals(dir)) {
					return FileVisitResult.CONTINUE;
				}
				return FileVisitResult.SKIP_SUBTREE;
				
			}

			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				// only process .txt files
				if (file.toString().endsWith(".txt")) {
					// we have found a .txt file;
					// then index the file and increase the document ID counter.
					addTokens(file.toFile(), docID ++);
				}
				return FileVisitResult.CONTINUE;
			}

			// don't throw exceptions if files are locked/other errors occur.
			public FileVisitResult visitFileFailed(Path file, IOException e) {
				return FileVisitResult.CONTINUE;
			}
		});
	}
	
	private void addTokens(File file, int docID) {
		String term;
		// Map to keep track of term Frequency inside each document.
		SimpleTokenStream sp = new SimpleTokenStream(file);
		List<String> allTerms = new ArrayList <String> ();
		while (sp.hasNextToken()) {
			String type = sp.nextToken();
			
			if (type == null) continue;	// skip the proceeding instructions if the term is an empty string.
			type = Normalizer.normalize(type);
			
			// We separate the hyphened type to create a set types that will be added into the index.
			if (type.contains("-")) {
				Set<String> types = Normalizer.splitHypenWords(type);
				
				for (String each : types) {
					term = Normalizer.stem(each);
					if(!allTerms.contains(term))
						allTerms.add(term);
				}
			} else {
				// Add the type to the KGramIndex and the term (stemmed type) into the PositionalInvertedIndex
				term = Normalizer.stem(type);
				if(!allTerms.contains(term))
					allTerms.add(term);
			}
			
		}
		terms.put(docID, allTerms);

	}
}
