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

	private static int totalDocsCorpus = 0;
	private int totalDocs;
	
	// Term to document IDs for this class
	private Map<String, List<Posting>> td;
	
	// This is all terms in the whole corpus and it maps it to the number of documents with this term regardless of class
	private static Map<String, Integer> allTerms = new HashMap<String, Integer> ();
	
	public Indexer() {
		td = new HashMap<String, List<Posting>> ();
	}
	
	public void indexDirectory(String path) throws IOException {
		final Path currentWorkingPath = Paths.get(path).toAbsolutePath();
		
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
				// only process .txt files
				if (file.toString().endsWith(".txt")) {
					// we have found a .txt file;
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
		totalDocsCorpus ++;
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
		
		// Add term and documentID to the term index for this class
		if(!td.containsKey(term)) { 
			List<Posting>docs = new ArrayList<Posting>();
			docs.add(new Posting(docId, 1));
			td.put(term, docs);
			if(allTerms.containsKey(term)) {
				allTerms.replace(term, allTerms.get(term) + 1);
			} else {
				allTerms.put(term, 1);
			}
		} else { // Term has already been found for this class
			List<Posting>docs = td.get(term);
			
			// Is this the same document? 
			if(docs.get(docs.size() - 1).getDocID() < docId) { // No
				docs.add(new Posting(docId, 1)); // Add new posting
				td.replace(term, docs);
				allTerms.replace(term, allTerms.get(term) + 1); // increment num docs
			} else { // Yes
				docs.get(docs.size() - 1).incrementFrequency(); // Just increment its frequency
			}
		}
		
	}
	
	public Map<String, List<Posting>> getTD() {
		return td;
	}
	
	public static Map<String, Integer> getAllTerms() {
		return allTerms;
	}
	public int getTotalDocs() {
		return totalDocs;
	}
	public static int getTotalDocsCorpus() {
		return totalDocsCorpus;
	}
}
