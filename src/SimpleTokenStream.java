import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class SimpleTokenStream implements TokenStream{

	private Scanner reader;
	public SimpleTokenStream(File file) {
		try {
			this.reader = new Scanner(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public String nextToken() {
		// TODO Auto-generated method stub
		return this.reader.next().toLowerCase();
	}

	@Override
	public boolean hasNextToken() {
		return this.reader.hasNext();
	}

}
