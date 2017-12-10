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
			n = i.getTotalDocs(); // Total number of documents in whole corpus
			int n1x = i.getTotalDocs(); // Total number of documents in this class
			Map<String, List<Integer>> td = i.getTD();
			
			for(String term: td.keySet()) {
				int n11 = td.get(term).size(); // Total number of documents with this term in this class
				int nx1 = i.getAllTerms().get(term); // Total number of documents with this term regardless of class
				int n01 = nx1 - n11; // Documents with term not in class
				int n10 = n1x - n11; // Documents in class without term
				int n00 = n - (n01 + n10 + n11); // Documents not in class & without term
				// Calculate I(T,C) = score
				TermScore ts = new TermScore(term, calcScore(n, n00, n01, n10, n11, n1x, nx1));
				pq.add(ts); // Add the score to the priority queue
			}
		}
		
		// Gets top 50 terms from priority queue and adds to list 
		for(int i = 0; i < 50 && !pq.isEmpty(); i++) {
			TermScore term = pq.poll();
			discriminatingTerms.add(term.getTerm());
			System.out.println(term.getTerm() + ": " + term.getScore());
		}
		return discriminatingTerms;
	}
	
	public static void main(String [] args) {
		
		String [] authors = {"c1", "c2", "c3"};
		Map<String, Indexer> classTerms = new HashMap<String, Indexer>(); // authors to terms
		try {
			for(String author : authors) {
				Indexer i = new Indexer();
				i.indexDirectory("/Users/crystalchun/Developer/Java/Classification/ex/" + author);
				classTerms.put(author, i);
			}
			getDiscrim(classTerms);
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

		double ans = 0;
		
		ans += Double.isNaN(operand1)? 0 : operand1;
		ans += Double.isNaN(operand2)? 0 : operand2;
		ans += Double.isNaN(operand3)? 0 : operand3;
		ans += Double.isNaN(operand4)? 0 : operand4;
		return ans;
	}
}
