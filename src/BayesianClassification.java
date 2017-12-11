import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class BayesianClassification {
	
	public static List<String> getDiscrim(Map<String, Indexer> classTerms) {
		PriorityQueue<TermScore> pq = new PriorityQueue<TermScore>(50, new TermScore()); // Priority queue for the terms
		List<String> discriminatingTerms = new ArrayList<String>(); // List of discriminating terms

		int n = 0;
		for(String author : classTerms.keySet()) {
			Indexer i = classTerms.get(author);
			n = Indexer.getTotalDocsCorpus(); // Total number of documents in whole corpus
			int n1x = i.getTotalDocs(); // Total number of documents in this class
			Map<String, List<Posting>> td = i.getTD();
			//System.out.println(author);
			for(String term: td.keySet()) {
				int n11 = td.get(term).size(); // Total number of documents with this term in this class
				int nx1 = Indexer.getAllTerms().get(term); // Total number of documents with this term regardless of class
				int n01 = nx1 - n11; // Documents with term not in class
				int n10 = n1x - n11; // Documents in class without term
				int n00 = n - (n01 + n10 + n11); // Documents not in class & without term

				/*System.out.println("n :" + n + " n1x : " + n1x + " nx1 : " + nx1 
						+ "\n n11 : " + n11 + " n10 : " + n10 + " n01 : " + n01 + " n00 : " + n00);*/
				// Calculate I(T,C) = score
				TermScore ts = new TermScore(term, calcScore(n, n00, n01, n10, n11, n1x, nx1));
				pq.add(ts); // Add the score to the priority queue
				/*for(Posting p : td.get(term)) {
					System.out.println(term);
					System.out.println("DocID: " + p.getDocID() + " Frequency: " + p.getFreq());
				}*/
				
			} 
			
			//System.out.println(pq.peek().getScore() + " " + pq.peek().getTerm());
		}
		
		// k values
		// 4 = 2 hamilton, rest madison
		// 3 = about half and half
		// 2 = Mostly hamilton with 2 madison
		// 1 = all hamilton
		// 5+ = all madison
		// 31 to 80 = all madison and one jay
		// 200 = 2 hamilton and rest madison
		// 400 = 2 hamilton and rest madison
		for(int i = 0; i < 3 && !pq.isEmpty(); i++) {
			TermScore term = pq.poll();
			if(!discriminatingTerms.contains(term.getTerm()))
				discriminatingTerms.add(term.getTerm());
			else
				i--;
			//System.out.println(term.getTerm() + ": " + term.getScore());
		}
		return discriminatingTerms;
	}
	
	public static void main(String [] args) {
		
		String [] authors = {"federalist-papers/MADISON", "federalist-papers/HAMILTON", "federalist-papers/JAY", "federalist-papers/DISPUTED"};
		//String [] authors = {"ex/c1" , "ex/c2" , "ex/c3", "dx" };
		Map<String, Indexer> classTerms = new HashMap<String, Indexer>(); // authors to terms
		try {
			for(int i = 0; i < authors.length - 1; i++) {
				Indexer j = new Indexer();
				//i.indexDirectory("/Users/crystalchun/Developer/Java/Classification/federalist-papers/" + author);
				j.indexDirectory("/Users/crystalchun/Developer/Java/Classification/" + authors[i]);
				classTerms.put(authors[i], j);
			}
			Map<String, Map<String, Double>> probs = getProbabilities(classTerms, getDiscrim(classTerms));
			SimpleIndexer si = new SimpleIndexer();
			//si.indexDirectory("/Users/crystalchun/Developer/Java/Classification/federalist-papers/DISPUTED");
			si.indexDirectory("/Users/crystalchun/Developer/Java/Classification/" + authors[authors.length - 1]);
			System.out.println("\n\nDISPUTED DOCUMENTS: ");
			for(int i : si.getTerms().keySet()) {
				//System.out.println("Document number: " + i + " contents: " + si.getTerms().get(i));
				System.out.println("Document number: " + i);
				classify(classTerms, si.getTerms().get(i), probs);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static double calcScore(double n,double n00, double n01, double n10, double n11, double n1x, double nx1) {
		double nx0 = n00 + n10;
		double n0x = n01 + n00;

		double operand1 = (n11/n)*(Math.log((n*n11)/(n1x*nx1))/Math.log(2.0));
		double operand2 = (n10/n)*(Math.log((n*n10)/(n1x*nx0))/Math.log(2.0));
		double operand3 = (n01/n)*(Math.log((n*n01)/(n0x*nx1))/Math.log(2.0));
		double operand4 = (n00/n)*(Math.log((n*n00)/(n0x*nx0))/Math.log(2.0));
		//System.out.println(operand1 + " " + operand2 + " " + operand3 + " " + operand4);
		double ans = 0;
		
		ans += Double.isNaN(operand1)? 0 : operand1;
		ans += Double.isNaN(operand2)? 0 : operand2;
		ans += Double.isNaN(operand3)? 0 : operand3;
		ans += Double.isNaN(operand4)? 0 : operand4;
		return ans;
	}
	
	public static Map<String, Map<String, Double>> getProbabilities(Map<String, Indexer> classTerms, List<String> terms) {
		Map<String, Map<String, Double>> classTermProbabilities = new HashMap<String, Map<String, Double>> (); // Maps a class to the term and probability
		
		for(String author: classTerms.keySet()) {
			// For every class
			//System.out.println(author);
			Map<String, List<Posting>> td = classTerms.get(author).getTD();
			Map<String, Double> termProbability = new HashMap<String, Double>();
			
			// Summation of all distinguishing terms' frequencies in this class
			double freqTermsInThisClass = 0;
			for(String dt: terms) {
				if(td.containsKey(dt)) {
					for(Posting p : td.get(dt))
						freqTermsInThisClass += p.getFreq();
				}
			}
			double totalNumberTermsInDiscrim = terms.size();
			
			// For every term in discriminating term set
			for(String t: terms) {
				double freq = 1; // ftc
				
				if(td.containsKey(t)) {
					// Get the term's frequency
					for(Posting p : td.get(t)) {
						freq += p.getFreq();
					}
				}
				
				
				double denominator = freqTermsInThisClass + totalNumberTermsInDiscrim;
				double probability = freq / denominator;
				//System.out.println(terms.size());
				//System.out.println("Author: " + author + " term: " + t + " probability: " + probability);
				termProbability.put(t, probability);
				//System.out.println(t + " " + probability);
			}
			classTermProbabilities.put(author, termProbability);
		}
		return classTermProbabilities;
	}
	
	public static String classify(Map<String, Indexer> classTerms, List<String>terms, Map<String, Map<String, Double>> classProbabilities) {
		double n = Indexer.getTotalDocsCorpus();
		double highest = Double.NEGATIVE_INFINITY;
		String classBelongsIn = "";
		
		for(String author : classTerms.keySet()) {
			//System.out.println(author);
			Indexer index = classTerms.get(author);
			double nc = index.getTotalDocs();
			//double score = Math.log(nc/n);
			double score = Math.log10(nc/n);
			//System.out.println(score);
			Map<String, Double> probabilities = classProbabilities.get(author);
			double sum = 0;
			for(String term : terms) {
				if(probabilities.containsKey(term)) {
					//System.out.println("Term: " + term + " probability: " + probabilities.get(term));
					sum += Math.log10(probabilities.get(term));
				}
			}
			//System.out.println(sum);
			score += sum;
			//System.out.println(author +" " +score);
			if(score > highest) {
				highest = score;
				classBelongsIn = author;
			}
		}
		System.out.println(highest + " " + classBelongsIn);
		return classBelongsIn;
	}
}
