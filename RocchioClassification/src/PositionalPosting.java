

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PositionalPosting implements Comparable<PositionalPosting> {
	
	private int id;
	private double score;
	private List<Integer> positions;

	// ------------------------------------------------------------------------------------------------------

	public PositionalPosting(int id) {
		this.id = id;
		this.positions = new ArrayList<Integer>();
	}
	
	// ------------------------------------------------------------------------------------------------------

	public PositionalPosting(int id, List<Integer> positions, double score) {
		this.id = id;
		this.positions = positions;
		this.score = score;
	}

	// ------------------------------------------------------------------------------------------------------

	public void addPosition(int position) {
		this.positions.add(position);
	}

	// ------------------------------------------------------------------------------------------------------

	public int getId() {
		return id;
	}

	// ------------------------------------------------------------------------------------------------------

	public List<Integer> getPositions() {
		return this.positions;
	}
	
	// ------------------------------------------------------------------------------------------------------
	
	public double getScore() {
		return this.score;
	}
	
	// ------------------------------------------------------------------------------------------------------
	
	public PositionalPosting merge(PositionalPosting other) {
		List<Integer> mergedPositions = new ArrayList<Integer>();
		mergedPositions.addAll(this.positions);
		mergedPositions.addAll(other.getPositions());
		Collections.sort(mergedPositions, Collections.reverseOrder());
		return new PositionalPosting(this.id, mergedPositions, score);
	}

	// ------------------------------------------------------------------------------------------------------

	public void setPositions(List<Integer> positions) {
		this.positions = positions;
	}
	
	// ------------------------------------------------------------------------------------------------------
	
	public void setScore(double score) {
		this.score = score;
	}
	
	// ------------------------------------------------------------------------------------------------------
	
	@Override
	public int compareTo(PositionalPosting o) {
		return Double.valueOf(o.score).compareTo(this.score);
	}
	
}
