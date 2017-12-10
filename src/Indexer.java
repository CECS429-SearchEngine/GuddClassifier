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

public class Indexer {

	int totalDocs = 0;
	private Map<String, List<Integer>> td; // Term to document IDs
	
	public Indexer() {
		td = new HashMap<String, List<Integer>> ();
	}
	
	public void indexDirectory(String path) throws IOException {
		final Path currentWorkingPath = Paths.get(path).toAbsolutePath();
		int totalDocs = 0;
		// the list of file names that were processed.
		List<String> fileNames = new ArrayList<String>();
		
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
				if (file.toString().endsWith(".txt")) {
					// we have found a .json file; add its name to the fileName list,
					// then index the file and increase the document ID counter.
					addTokens(file.toFile(), documentID++);
				}
				return FileVisitResult.CONTINUE;
			}

			// don't throw exceptions if files are locked/other errors occur.
			public FileVisitResult visitFileFailed(Path file, IOException e) {
				return FileVisitResult.CONTINUE;
			}
		});
	}
	
	private void addTokens(File file, int docId) {
		String term;
		totalDocs ++;
		// Map to keep track of term Frequency inside each document.
		SimpleTokenStream sp = new SimpleTokenStream(file);
		
		while (sp.hasNextToken()) {
			String type = sp.nextToken();
			
			if (type == null) continue;	// skip the proceeding instructions if the term is an empty string.
			type = Normalizer.normalize(type);
			
			// We separate the hyphened type to create a set types that will be added into the index.
			if (type.contains("-")) {
				Set<String> types = Normalizer.splitHypenWords(type);
				
				for (String each : types) {
					term = Normalizer.stem(each);
					addTD(term, docId);
				}
			} else {
				// Add the type to the KGramIndex and the term (stemmed type) into the PositionalInvertedIndex
				term = Normalizer.stem(type);
				addTD(term, docId);
			}
			
		}

	}
	private void addTD(String term, int docId) {
		if(!td.containsKey(term)) { // Add term and documentID
			List<Integer>docs = new ArrayList<Integer>();
			docs.add(docId);
			td.put(term, docs);
		} else {
			List<Integer>docs =td.get(term);
			if(!docs.contains(docId)) {
				docs.add(docId);
				td.replace(term, docs);
			}
		}
	}
	public Map<String, List<Integer>> getTD() {
		return td;
	}
}
