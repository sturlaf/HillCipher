package HillCipher;

import java.util.ArrayList;


public class FiniteRing {
	
	private int n;
	private ArrayList<Integer> inverse;
	
	/**
	 * Generates a new finite ring, and calculates all inverses. 
	 * @param nrElements the number of elements the ring should have. 
	 */
	public FiniteRing(int nrElements) {
		this.n = nrElements;
		this.inverse = new ArrayList<Integer>();
		for(int i = 0; i < nrElements; i++) {
			this.inverse.add(this.multInverse(i));
		}
	}
	

	public int addInverse(int a) {
		return (this.n - a) % this.n;
	}
	
	
	public int getN() {
		return n;
	}
	
	
	public int mod(int number) {
		
		int reducedNumber = number % this.getN();
		
		if ( reducedNumber < 0) {
			return this.addInverse(reducedNumber);
		}
		
		return reducedNumber;
	}



	public ArrayList<Integer> getInverse() {
		return inverse;
	}



	public int add(int a, int b) {	
		return (a + b) % n;
	}
	
	public int mult(int a, int b) {
		return (a*b) % n;
	}
	
	
	/**
	 * Calculates the inverse of the numbers in the ring. If a element has no inverse, it returns -1.
	 * @param a the number to calculate its inverse.
	 * @return the inverse of a mod n if it has an inverse, -1 otherwise. 
	 */
	private int multInverse(int a) {
		int t = 0;
		int newt = 1;
		int r = this.n;
		int newr = a;
		while (newr != 0) {
			int q = r / newr;
			int prov = newt;
			int prov2 = newr;
			newt = t - q* prov;
			newr = r - q* prov2;
			t = prov;
			r = prov2;
		}
		if (r > 1) {
			return -1;
		}
		if(t < 0) {
			t = t + n;
		}
		return t;
	}
	
	public int divide(int a ,int b) {
		return mult(a, multInv(b));
	}
	
	/**
	 * Returns the inverse of a, or -1 if a has no inverse. 
	 * @param a the number to calculate its inverse.
	 * @return the inverse of a, or -1 if a has no inverse modulo n. 
	 */
	public int multInv(int a) {
		a = a % this.n;
		
		int b = this.inverse.get(a);
		
		if( b == -1) {
			throw new IllegalArgumentException(a + "have no inverse");
		}
		else { return b; }
	}

}
