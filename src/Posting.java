
public class Posting {
	private int docID;
	private int frequency;
	public Posting(int doc, int freq) {
		this.docID = doc;
		this.frequency = freq;
	}
	
	public int getDocID() {
		return this.docID;
	}
	
	public int getFreq() {
		return frequency;
	}
	public void incrementFrequency() {
		this.frequency ++;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Posting){
			Posting p = (Posting) o;
			return p.getDocID() == this.docID;
		}
		return false;
		
	}
}
