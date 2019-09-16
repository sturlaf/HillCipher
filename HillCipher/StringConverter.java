package HillCipher;

import java.security.SecureRandom;
import java.util.ArrayList;

public class StringConverter {
	
	private String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ∆ÿ≈";
	private FiniteRing nrElements;
	private SecureRandom random = new SecureRandom(); 
	
	
	public FiniteRing getNrElements() {
		return this.nrElements;
	}

	/**
	 * Generates a new {@link StringConverter} with the predefined string as alphabet. 
	 */
	public StringConverter() {
		this.nrElements = new FiniteRing(this.alphabet.length());
	}
	
	/**
	 * A method to customize what string the alphabet should be, and what numerical value each {@link Char} should have. 
	 * <p>
	 * Example: 
	 * <p>
	 * this.setalphabet("ABC");
	 * this.singleNumToString(1); -> 'B'
	 * this.singleCharToNum('C'); -> 2
	 * @param s the string with each {@link Char} wanted once, at the place where their numerical value should be. 
	 */
	public void setalphabet(String s) {
		this.alphabet = s.strip();
		this.nrElements = new FiniteRing(this.alphabet.length());
	}
	
	
	private char singleNumToString(int num) {
		return this.alphabet.charAt(this.nrElements.mod(num));
	}
	
	
	public String vectorToString(VectorFinite block) {
		
		String result = "";
		
		ArrayList<Integer> numbers = block.getVector();
		
		for( int num : numbers) {
			result += this.singleNumToString(num);
		}
		
		return result;
	}
	
	
	private int singleCharToNum(char c) {
		int i = this.alphabet.indexOf(c);
		if (i == -1) {
			throw new IllegalArgumentException("Char was not found in the alphabeth");
		}
		
		return i;
	}
	
	
	public VectorFinite stringToVector(String s) {
		
		ArrayList<Integer> numbers = new ArrayList<Integer>();
		
		for(int i = 0; i < s.length(); i++) {
			
			numbers.add(this.singleCharToNum(s.charAt(i)));
		}
		
		VectorFinite block = new VectorFinite(numbers, this.nrElements.getN());
		
		return block;
	}
	
	
	public MatrixFinite stringToBlocks(String s, int blockLength) {
		
		int reminder = s.length() % blockLength;
		
		s = addRandomString(s, reminder);
		
		ArrayList<VectorFinite> message = new ArrayList<VectorFinite>();
		
		int quotient = s.length()/blockLength;
		
		for(int col = 0; col < quotient; col++) {
			
			String subString = s.substring(col*blockLength, (col + 1)*blockLength);
			
			VectorFinite vector = this.stringToVector(subString);
			
			message.add(vector);
		}
		
		MatrixFinite M = new MatrixFinite(blockLength, this.nrElements.getN());
		
		M.setMatrix(message);
		
		return M;
	}

	
	//Adds num number of random char at the end of string s
	private String addRandomString(String s, int num) {
		for(int i = 0; i < num; i++) {
			s += this.alphabet.charAt(this.nrElements.mod(random.nextInt()));
		}
		return s;
	}
	
	
	public String matrixToString(MatrixFinite message) {
		
		String result = "";
		
		ArrayList<VectorFinite> list = message.getMatrix();
		
		for( VectorFinite block : list) {
			result += this.vectorToString(block);
		}
		
		//Generates a new random integer in proportion to the length of the message
		int randInt = random.nextInt(message.getMatrix().size());
		
		result = addRandomString(result, randInt);
		
		return result;
	}

}
