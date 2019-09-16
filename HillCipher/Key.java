package HillCipher;
import java.security.SecureRandom;
import java.util.ArrayList;


public class Key {
	
	private SecureRandom random;
	
	private MatrixFinite encryptKey;
	
	private MatrixFinite decryptKey;
	
	private int blockLenght;
	
	private FiniteRing alphabeth;
	
	private int maxdepth = 1000;
	
	public StringConverter converter = new StringConverter();
	
	
	/**
	 * 
	 * @param blockLength the length of the blocks that will be encrypted. Specifies the size of the key. 
	 * @param seed a byte that seeds the random number generator. If the same seed is used, it will always generate the same key.
	 * @see byte[]
	 */
	public Key(int blockLength, byte[] seed) {
		
		this.random = new SecureRandom(seed);
		
		initializeKey(blockLength);
		
		this.generateKey();
		
	}

	
	/**
	 * 
	 * @param blockLength the length of the blocks that will be encrypted. Specifies the size of the key. 
	 * @param entries the entries of the matrix, starting from the top left, continues until the the end of the row, before the next row. Must be an invertible matrix.
	 * @param seed a byte that seeds the random number generator. If the same seed is used, it will always generate the same key.
	 * @see byte[]
	 */
	public Key(int blockLength, ArrayList<Integer> entries, byte[] seed) {
		this(blockLength, seed);
		
		this.setKey(entries);
		
	}
	
	
	public Key(int blockLenght) {
		
		this.random = new SecureRandom();
		
		this.initializeKey(blockLenght);
		
		this.generateKey();
		
	}
		
	/**
	 * Initializes the object 
	 * @param blockLength the length of the blocks that will be encrypted. Specifies the size of the key. 
	 */
	private void initializeKey(int blockLength) {
		this.blockLenght = blockLength;
		
		int alphabet = this.converter.getNrElements().getN();
		
		this.alphabeth = new FiniteRing(alphabet);
		
		this.encryptKey = new MatrixFinite(blockLenght, alphabet);
	}
	
	/**
	 * The algorithm used to encrypt a string using Hill cipher
	 * @param plaintext the string which should be encrypted
	 * @return a string that is encrypted using the key
	 */
	public String encrypt(String plaintext) {
		
		MatrixFinite message = converter.stringToBlocks(plaintext, this.getBlockLenght());
		
		MatrixFinite encryptedMessage = this.encrypt(message);
		
		String ciphertext = converter.matrixToString(encryptedMessage);
		
		return ciphertext;
	}
	
	/**
	 * The algorithm used to decrypt a string using Hill cipher
	 * @param ciphertext the string which should be decrypted
	 * @return the plaintext using the decrypt-key
	 */
	public String decrypt(String ciphertext) {
		
		MatrixFinite message = converter.stringToBlocks(ciphertext, this.getBlockLenght());
		
		MatrixFinite decryptedMessage = this.decrypt(message);
		
		String plaintext = converter.matrixToString(decryptedMessage);
		
		return plaintext;
	}
	
	
	/**
	 * This method generates a key using the random number generator. 
	 * @param depth specifies how many iterations the program should run before it gives up finding an invertible matrix"
	 */
	private void generateKey(int depth) {
		
		if(depth >= this.maxdepth) {
			throw new IllegalStateException("Could not find an invertible key");
		}
		
		ArrayList<Integer> randomList = new ArrayList<Integer>();
		
		for(int i = 0; i < this.blockLenght*this.blockLenght; i++) {
			randomList.add(this.alphabeth.mod(random.nextInt()));
		}
		try {
			this.encryptKey.setMatrix(randomList.iterator());
			this.decryptKey = this.encryptKey.inverse();
		}
		catch (IllegalArgumentException e) {
			depth++;
			this.generateKey(depth);
		}
	}
	
	/**
	 * Generates the random key, where maxdepth specifies how many times the algorithm tries to find an invertible matrix.
	 */
	private void generateKey() {
		this.generateKey(0);
	}
	
	/**
	 * Makes it possible to use a self-made key, instead of generating one. 
	 * @param entries the entries of the matrix, starting from the top left, continues until the the end of the row, before the next row. Must be an invertible matrix.
	 */
	public void setKey(ArrayList<Integer> entries) {
		try {
			this.encryptKey.setMatrix(entries.iterator());
			this.decryptKey = this.encryptKey.inverse();
		}
		catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("The matrix is not invertible");
		}
	}
	
	/**
	 * 
	 * @return the encryption-key
	 */
	public MatrixFinite getEncryptKey() {
		return encryptKey;
	}


	/**
	 * 
	 * @return the decryption-key
	 */
	public MatrixFinite getDecryptKey() {
		return decryptKey;
	}


	/**
	 * 
	 * @return the block length
	 */
	public int getBlockLenght() {
		return blockLenght;
	}


	/**
	 * 
	 * @return a {@link FiniteRing} with the same number of elements as in the string used to give numerical values to {@link Char}.
	 */
	public FiniteRing getAlphabet() {
		return this.alphabeth;
	}


	/**
	 * 
	 * @return the max recursion depth this object uses to generate a key.
	 */
	public int getMaxdepth() {
		return maxdepth;
	}

	
	public void setMaxdepth(int maxdepth) {
		this.maxdepth = maxdepth;
	}

	
	
	public FiniteRing getAlphabeth() {
		return alphabeth;
	}

	
	
	public void setAlphabeth(FiniteRing alphabeth) {
		this.alphabeth = alphabeth;
	}
	

	
	public StringConverter getConverter() {
		return converter;
	}
	

	/**
	 * Makes it possible to choose your own {@link StringConverter}.
	 * Write a string, where the position in the string correlates to what numerical value the {@link Char} is converted to.
	 * @param string the string, should not have any repeated {@link Char}. All characters in the plaintext must appear in the string. 
	 */
	public void setConverter(String string) {
		this.converter.setalphabet(string);
		this.alphabeth = this.converter.getNrElements();
	}
	

	/**
	 * The multiplication of the key with each block of letters, which is at the core of the Hill cipher algorithm. 
	 * @param message the plaintext represented as a {@link MatrixFinite} which is to be encrypted.
	 * @return a matrix representation of the encrypted string. Each row has length blockLengt.
	 */
	private MatrixFinite encrypt(MatrixFinite message) {
		return this.encryptKey.matrixRightTransMult(message);
	}
	
	
	/**
	 * The multiplication of the inverse of the key with each block of letters, that returns 
	 * @param ciphertext the ciphertext represented as a {@link MatrixFinite} which is to be decrypted.
	 * @return a matrix representation of the plaintext. Each row has length blockLengt.
	 */
	private MatrixFinite decrypt(MatrixFinite ciphertext) {
		return this.decryptKey.matrixRightTransMult(ciphertext);
	}
	
	

}
