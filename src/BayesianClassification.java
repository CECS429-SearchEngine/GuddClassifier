import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class BayesianClassification {
	
	public static void getDiscrim(Map<String, Indexer> classTerms) {
		Map<String, Integer> allTerms = new HashMap<String, Integer> (); // This is all terms in the whole corpus and it maps it to the number of documents with this term regardless of class
		int n = 0; // Total number of documents in whole corpus
		Map<String, Double> termScore = new HashMap<String, Double> (); // Term to I(T,C) score
		PriorityQueue<Map<String, Double>> pq = new PriorityQueue<Map<String, Double>>();
		for(String author : classTerms.keySet()) {
			Indexer i = classTerms.get(author);
			n += i.totalDocs;
			Map<String, List<Integer>> td = i.getTD();
			for(String term: td.keySet()) {
				
				if(allTerms.containsKey(term)) {
					allTerms.replace(term, allTerms.get(term) + td.get(term).size());
				} else {
					allTerms.put(term, td.get(term).size());
				}
			}
		}
		for(String author : classTerms.keySet()) {
			Indexer i = classTerms.get(author);
			int n1x = i.totalDocs; // Total number of documents in this class
			Map<String, List<Integer>> td = i.getTD();
			
			for(String term: td.keySet()) {
				int n11 = td.get(term).size(); // Total number of documents with this term in this class
				int nx1 = allTerms.get(term); // Total number of documents with this term regardless of class
				int n01 = nx1 - n11; // Documents with term not in class
				int n10 = n1x - n11; // Documents in class without term
				int n00 = n - (n01 + n10 + n11); // Documents not in class & without term
				// Calculate I(T,C)
				
				
			}
		}
	}
	
	public static void main(String [] args) {
		
		String [] authors = {"HAMILTON", "JAY", "MADISON"};
		Map<String, Indexer> classTerms = new HashMap<String, Indexer>(); // authors to terms
		try {
			for(String author : authors) {
				Indexer i = new Indexer();
				i.indexDirectory("/Users/crystalchun/Developer/Java/Classification/federalist-papers/" + author);
				Map<String, List<Integer>> td = i.getTD();
				for(String each: td.keySet()) {
					System.out.println(each + " - " + td.get(each));
				}
				classTerms.put(author, i);
			}
			getDiscrim(classTerms);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public double calcScore(double n,double n00, double n01, double n10, double n11, double n1x, double nx1) {
		double nx0 = n00 + n10;
		double n0x = n01 + n00;
		double ans = (n11/n)*(Math.log((n*n11)/(n1x*nx1))) + (n10/n)*(Math.log((n*n10)/(n1x*nx0))) + (n01/n)*(Math.log((n*n01)/(n0x*nx1))) + (n00/n)*(Math.log((n*n00)/(n0x*nx0)));
		return ans;
	}
}
