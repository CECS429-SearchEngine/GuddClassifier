import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class BayesianClassification {
	
	public static void main(String [] args) {
		String [] authors = {"federalist-papers/MADISON", "federalist-papers/HAMILTON", "federalist-papers/JAY", "federalist-papers/DISPUTED"};
		//String [] authors = {"ex/c1" , "ex/c2" , "ex/c3", "dx" };
		Map<String, Indexer> classTerms = new HashMap<String, Indexer>(); // authors to terms
		try {
			// Index the directory for the training set
			for(int i = 0; i < authors.length - 1; i++) {
				Indexer j = new Indexer();
				j.indexDirectory("/Users/crystalchun/Developer/Java/Classification/" + authors[i]);
				classTerms.put(authors[i], j);
			}
			
			Map<String, Map<String, Double>> probs = getProbabilities(classTerms, getDiscrim(classTerms, 50));
			
			// Index disputed documents
			SimpleIndexer si = new SimpleIndexer();
			si.indexDirectory("/Users/crystalchun/Developer/Java/Classification/" + authors[authors.length - 1]);
			
			// Classifying the disputed documents
			System.out.println("\n\nDISPUTED DOCUMENTS: ");
			for(int i : si.getTerms().keySet()) {
				System.out.println("Document number: " + i + " Document name: " + si.getFileNames().get(i));
				classify(classTerms, si.getTerms().get(i), probs);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the discriminating set of terms.
	 * @param classTerms - The classes (authors) and their respective Indexers.
	 * @param k - The number of discriminating terms.
	 * @return The list of the top k discriminating terms.
	 */
	public static List<String> getDiscrim(Map<String, Indexer> classTerms, int k) {
		PriorityQueue<TermScore> pq = new PriorityQueue<TermScore>(8000, new TermScore()); // Priority queue for the terms
		List<String> discriminatingTerms = new ArrayList<String>(); // List of discriminating terms
	
		int n = 0;
		// Calculates I(T,C) for every term and class pair
		for(String author : classTerms.keySet()) {
	
			Indexer i = classTerms.get(author);
			n = Indexer.getTotalDocsCorpus(); // Total number of documents in whole corpus
			int n1x = i.getTotalDocs(); // Total number of documents in this class
			Map<String, List<Posting>> td = i.getTD();
	
			for(String term: td.keySet()) {
				int n11 = td.get(term).size(); // Total number of documents with this term in this class
				int nx1 = Indexer.getAllTerms().get(term); // Total number of documents with this term regardless of class
				int n01 = nx1 - n11; // Documents with term not in class
				int n10 = n1x - n11; // Documents in class without term
				int n00 = n - (n01 + n10 + n11); // Documents not in class & without term
	
				
				// Calculate I(T,C) = score
				TermScore ts = new TermScore(term, calcScore(n, n00, n01, n10, n11, n1x, nx1));
				pq.add(ts); // Add the score to the priority queue
				
			} 
		}
		
		// Gets the top scoring discriminating terms
		for(int i = 0; i < k && !pq.isEmpty(); i++) {
			TermScore term = pq.poll();
			if(!discriminatingTerms.contains(term.getTerm()))
				discriminatingTerms.add(term.getTerm());
			else
				i--;
		}
	
		return discriminatingTerms;
	}

	/**
	 * Calculates I(T,C) for one term in one class
	 * @param n - The total number of documents in the training set.
	 * @param n00 - The number of documents that don't have the term and aren't in this class.
	 * @param n01 - The number of documents with the term that aren't in this class.
	 * @param n10 - The number of documents in this class that don't have the term.
	 * @param n11 - The number of documents in this class that have this term.
	 * @param n1x - The total number of documents in this class.
	 * @param nx1 - The total number of documents with this term.
	 * @return The I(T, C) for one term in one class.
	 */
	public static double calcScore(double n,double n00, double n01, double n10, double n11, double n1x, double nx1) {
		double nx0 = n00 + n10;
		double n0x = n01 + n00;

		double operand1 = (n11/n)*(Math.log((n*n11)/(n1x*nx1))/Math.log(2.0));
		double operand2 = (n10/n)*(Math.log((n*n10)/(n1x*nx0))/Math.log(2.0));
		double operand3 = (n01/n)*(Math.log((n*n01)/(n0x*nx1))/Math.log(2.0));
		double operand4 = (n00/n)*(Math.log((n*n00)/(n0x*nx0))/Math.log(2.0));

		double ans = 0;
		
		ans += Double.isNaN(operand1)? 0 : operand1;
		ans += Double.isNaN(operand2)? 0 : operand2;
		ans += Double.isNaN(operand3)? 0 : operand3;
		ans += Double.isNaN(operand4)? 0 : operand4;
		return ans;
	}
	
	/**
	 * Finding the probabilities of every discriminating term in a class. 
	 * @param classTerms - The classes (authors) and their respective indexes.
	 * @param terms - The list of discriminating terms
	 * @returns All the probabilities between a class (author) and a discriminating term
	 */
	public static Map<String, Map<String, Double>> getProbabilities(Map<String, Indexer> classTerms, List<String> terms) {
		Map<String, Map<String, Double>> classTermProbabilities = new HashMap<String, Map<String, Double>> (); // Maps a class to the term and probability
		
		for(String author: classTerms.keySet()) {
			Map<String, List<Posting>> td = classTerms.get(author).getTD();
			Map<String, Double> termProbability = new HashMap<String, Double>(); // Maps term to its probability of being in this class
			
			// Summation of all distinguishing terms' frequencies in this class
			double freqTermsInThisClass = 0;
			for(String dt: terms) {
				if(td.containsKey(dt)) {
					for(Posting p : td.get(dt)) {
						freqTermsInThisClass += p.getFreq();
					}
						
				}
			}
			
			double totalNumberTermsInDiscrim = terms.size();
			
			// For every term in discriminating term set, calculate the probability of being in this class
			for(String t: terms) {
				double freq = 1; // ftc - the term frequency of just one term
				
				if(td.containsKey(t)) {
					// Get the term's frequency
					for(Posting p : td.get(t)) {
						freq += p.getFreq();
					}
				}
				
				double denominator = freqTermsInThisClass + totalNumberTermsInDiscrim;
				double probability = freq / denominator;

				//System.out.println("Author: " + author + " term: " + t + " probability: " + probability);
				termProbability.put(t, probability);
			}
			classTermProbabilities.put(author, termProbability);
		}
		return classTermProbabilities;
	}
	
	/** 
	 * Classifying a disputed document.
	 * @param classTerms - The classes (authors) mapped to their indexers.
	 * @param terms - The list of terms in the disputed document.
	 * @param classProbabilities - all the probabilities between a class (author) and a discriminating term. 
	 * @return The class this disputed document belongs to.
	 */
	public static String classify(Map<String, Indexer> classTerms, List<String>terms, Map<String, Map<String, Double>> classProbabilities) {
		double n = Indexer.getTotalDocsCorpus();
		double highest = Double.NEGATIVE_INFINITY;
		String classBelongsIn = "";
		
		// Calculating Cd for every class
		for(String author : classTerms.keySet()) {
			Indexer index = classTerms.get(author);
			double nc = index.getTotalDocs();

			double score = Math.log10(nc/n); // probability of being in a class: log(p(c))

			Map<String, Double> probabilities = classProbabilities.get(author);
			double sum = 0;
			
			// Get the probability of a term being in this class
			for(String term : terms) { // All the terms in the disputed document
				if(probabilities.containsKey(term)) { // Only adds the probabilities of terms that are in the discriminating set of terms.
					sum += Math.log10(probabilities.get(term)); // Sum(log(p(t|c)))
				}
			}
			
			score += sum;
			
			System.out.println(author +" " +score);
			if(score > highest) {
				highest = score;
				classBelongsIn = author;
			}
		}
		
		System.out.println(highest + " " + classBelongsIn);
		return classBelongsIn;
	}
}
