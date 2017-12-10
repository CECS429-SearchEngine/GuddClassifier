import java.util.Comparator;

public class TermScore implements Comparator<TermScore>{
	private String term;
	private double score;
	public TermScore(String term, double score) {
		this.term = term;
		this.score = score;
	}
	
	public TermScore() {
		// TODO Auto-generated constructor stub
	}

	public double getScore() {
		return this.score;
	}
	public String getTerm() {
		return term;
	}
	@Override
	public int compare(TermScore o1, TermScore o2) {
		if(o2.getScore() < o1.getScore()) return -1;
		if(o2.getScore() > o1.getScore()) return 1;
		return 0;
	}

}
