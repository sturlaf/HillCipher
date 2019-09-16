package HillCipher;

import java.util.ArrayList;
import java.util.List;


public class VectorFinite {
	
	private ArrayList<Integer> vector;
	private int dim;
	private FiniteRing ring;
	
	
	public VectorFinite(Iterable<Integer> numbers, int n) {
		ArrayList<Integer> vector = new ArrayList<Integer>();
		for(int e : numbers) {
			vector.add(e % n);
		}
		
		this.vector = vector;
		this.dim = vector.size();
		this.ring = new FiniteRing(n);
	}
	
	
	//add a new element of the end of the vector. This increases the dimension of the vector. 
	public void addElement(int number) {
		this.dim++;
		this.vector.add(number % this.ring.getN());
	}
	
	
	//Add all elements from another vector at the end of this.
	//Useful for implementing Gauss-elimination
	public void addAllElements(VectorFinite vec) {
		int len = vec.getDim();
		this.dim += len;
		this.vector.addAll(vec.getVector());
	}
	
	
	//Adds this vector with the vector vec elementwise.
	public void addVec(VectorFinite vec) {
		
		for(int i = 0; i < this.getDim(); i++) {
			
			this.setElement(i, this.getRing().add(this.getElement(i), vec.getElement(i)));
		}
	}
	
	//Multiply vector by a scalar
	public void scalarMult(int scalar) {

		for(int i = 0; i < this.dim; i++) {
			
			
			this.setElement(i,this.ring.mult(scalar, this.getElement(i)));
		}
	}
	
	
	//Finds the dot-product between two vectors
	public int dotProduct(VectorFinite vec) {
		
		if (this.dim != vec.getDim()) {
			throw new IllegalArgumentException("Vectors should have the same length");
		}
		
		int sum = 0;
		
		for (int i = 0; i < this.getDim(); i++) {
			sum += this.ring.mult(this.getElement(i), vec.getElement(i));
		}
		
		return sum % this.getRing().getN();
	}
	
	
	
	//Multiplies the matrix with this vector from the right
	public VectorFinite matrix( MatrixFinite matrix) {
		
		ArrayList<Integer> ans = new ArrayList<Integer>();
		
		for (VectorFinite row : matrix.getMatrix()) {
			ans.add(this.dotProduct(row));
		}
		
		VectorFinite vec = new VectorFinite(ans, this.ring.getN());
		return vec;
	}
	
	
	//Splits an vector at a specified index, returns the two halves.
	public VectorFinite[] split(int index) {
		if((index > this.getDim()) || index < 0) {
			throw new IllegalArgumentException("Out of bounds");
		}
		
		List<Integer> vec1 = this.vector.subList(0, index);
		List<Integer> vec2 = this.vector.subList(index +1, this.dim);
		
		
		VectorFinite vector1 = new VectorFinite(vec1, this.ring.getN());
		VectorFinite vector2 = new VectorFinite(vec2, this.ring.getN());
		
		return new VectorFinite[] {vector1, vector2};
	}
	
	
	public ArrayList<Integer> getVector() {
		return vector;
	}


	public int getDim() {
		return dim;
	}


	public FiniteRing getRing() {
		return ring;
	}

	
	
	
	//Adds a multiple of a vector vec to this vector. Can be used for row reduction.
	public void addMultipleOfVec(VectorFinite vec, int scalar) {
		
		//Don't want to make changes to the vector vec
		VectorFinite clone = vec.clone();
		
		//Multiplies the vector vec with a scalar
		clone.scalarMult(scalar);
		
		
		this.addVec(clone);
	}
	
	
	@SuppressWarnings("unchecked")
	public VectorFinite clone() {
		ArrayList<Integer> clone = (ArrayList<Integer>) this.vector.clone();
		return new VectorFinite(clone, this.ring.getN());
	}

	
	public int getElement(int index) {
		return this.vector.get(index);
	}
	
	
	public void setElement(int index, int scalar) {
		this.vector.set(index, scalar);
	}
	
	
	public String toString() {
		return this.vector.toString();
	}

}
