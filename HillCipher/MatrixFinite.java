package HillCipher;

import java.util.ArrayList;
import java.util.Iterator;

public class MatrixFinite {
	
	//The dimension of the matrix
	private int row;
	
	//The matrix is over a finite ring.
	private FiniteRing finiteRing;
	
	//The matrix as a column-vector of row-vectors, where the rows are VectorFinite objects.
	private ArrayList<VectorFinite> matrix;
	
	
	/**
	 * Generates a new {@link MatrixFinite} with all entries equal to zero. 
	 * All algebraic operations satisfies the properties of the finite ring.
	 * @param row the number of rows the matrix should have. 
	 * @param finiteRing the number of elements the ring should have, stating which ring the entries of the matrix should come from.
	 */
	public MatrixFinite(int row, int finiteRing) {
		this.row = row;
		this.finiteRing = new FiniteRing(finiteRing);
		this.matrix = new ArrayList<VectorFinite>();
		for (int i = 0; i < row; i++) {
			ArrayList<Integer> row_prw = new ArrayList<Integer>();
			for (int j = 0; j < row; j++) {
				row_prw.add(0);
			}
			
			VectorFinite row_mid = new VectorFinite(row_prw, finiteRing);
			this.matrix.add(row_mid);
		}
	}
	
		
	/**
	 * Sets the values off the matrix. Starting with the first row left to right, then repeat with the next row.
	 * @param numbers an iterator holding the values of the entries. The first row fills first, left to right. 
	 */
	public void setMatrix(Iterator<Integer> numbers) {
		outerloop: 
		for (VectorFinite row_mid : this.matrix) {
			for (int i = 0; i < row_mid.getDim(); i++) {
				if (numbers.hasNext()) {
					row_mid.setElement(i, numbers.next());
				} else {
					break outerloop;
				}
			}
		}
	}

	
	/**
	 * Calculates the identity.
	 * @return the identity with the same dimensions as this.
	 */
	private MatrixFinite identity() {
		MatrixFinite id = new MatrixFinite(this.getRow(), this.getFiniteRing().getN());
		for (int i = 0; i < id.getRow(); i++) {
			id.setIndex(i, i, 1);
		}
		return id;
	}


	/**
	 * Reduces the matrix by gauss elimination.
	 * @return the eliminated matrix
	 */
	private MatrixFinite reduction() {
		
		MatrixFinite id = this.identity();
		
		MatrixFinite G = this.clone();
		
		//Construct a dim x 2*dim matrix, where the first dim columns are the ones from this, and the next from the identity
		G.forwardReduction(id);
		G.backSubstitution();
		
		return G;
		
	}
	
	
	/**
	 * Calculates inverse. If no inverse exist, it throws an {@link IllegalArgumentException}.
	 * @return the inverse of this matrix
	 */
	public MatrixFinite inverse() {
		
		MatrixFinite G = this.reduction();
		
		MatrixFinite[] pair = G.split();
		
		return pair[1];
	}


	/**
	 * This method reduces this matrix to a triangular matrix, and it reduces the {@link MatrixFinite} A with the same operations, 
	 * according to the rules of gauss-eleimination.
	 * @param A The matrix that is reduced together with this matrix. 
	 */
	private void forwardReduction( MatrixFinite A) {
		
		
		for(int i = 0; i < this.row; i++) {
			this.getMatrix().get(i).addAllElements(A.getMatrix().get(i));
		}
		
		
		for(int colNr = 0; colNr < this.row; colNr++) {
			
			//Finds a row with an invertible element on the diagonal
			try {
				
				//Find suitable row, and normalize it
				this.pivoting(colNr);
				
				
				for(int rowNr = colNr + 1; rowNr < this.getRow(); rowNr++) {
					
					
					int element = this.getMatrix().get(rowNr).getElement(colNr);
					
					
					this.scalarRowSubstraction(rowNr, colNr, element);
				}
				
			}
			catch (Exception e) { throw new IllegalArgumentException("The matrix has no inverse because " + e);}
		}
	}
	
	
	/**
	 * Used after forward-reduction is used, to eliminate all entries above the diagonal. Only gives sensible outputs when this
	 * matrix is invertible.
	 */
	private void backSubstitution() {
		
		for(int colNr = this.getRow() - 1; colNr > 0; colNr--) {
			
			for(int rowNr = colNr - 1; rowNr >= 0; rowNr--) {
				
				int element = this.getIndex(rowNr, colNr);
				this.scalarRowSubstraction(rowNr, colNr	, element);
			}
		}
	}
	
	
	/**
	 * Swaps row nr index with rows below until the diagonal element in row 
	 * nr index is invertible, or throws an exception if this is not possible.
	 * If an inverse exist, the algorithm will normalize the row nr index.
	 * @param index the index of the row where the diagonal element should be invertible.
	 * @param i the index of a row below row nr index, where the element (i, index) might be invertible.
	 */
	private void pivoting(int index, int i) {
		
		//If the algorithm has tried all possible rows without success, it throws an IllegalArgumentException.
		if(index >= this.getRow()) {
			throw new IllegalArgumentException("there exist no invertible pivot for the row ");
		}
		
		//Find the inverse of the diagonal element. If it has no inverse, it returns -1.
		int diag = this.getIndex(index, index);
		
		int diag_inv = this.getFiniteRing().multInv(diag);
		
		//If the inverse does not exist, the algorithm tries the next possible row.
		if(diag_inv == -1 ) {
			this.swapRows(index, i);
			this.pivoting(index, i + 1);
		}
		
		this.scalarRowMult(index, diag_inv);
	}
	
	
	/**
	 * Swaps rows such that row nr index has an invertible element on the diagonal, or throws exception else.
	 * @param index the index of the row
	 */
	private void pivoting(int index) {
		this.pivoting(index, index);
	}
	
	
	/**
	 * Only used after finishing Gauss-elimination, to get the respective matrices back. Should only be used when the
	 * dimension of this obcjet is row x (2*row).
	 * @return two row x row {@link MatrixFinite[]}.
	 */
	private MatrixFinite[] split() {
		
		//This method should only be invoked under special circumstances
		if (2*this.row != this.getMatrix().get(0).getDim()) {
			throw new IllegalStateException("The matrix had wrong dimensions");
		}
		
		MatrixFinite matrixA = new MatrixFinite(this.row, this.finiteRing.getN());
		MatrixFinite matrixB = new MatrixFinite(this.row, this.finiteRing.getN());
		
		ArrayList<VectorFinite> matA = new ArrayList<VectorFinite>();
		ArrayList<VectorFinite> matB = new ArrayList<VectorFinite>();
		
		
		for(VectorFinite row : this.matrix) {
			VectorFinite[] pair = row.split(this.row - 1);
			matA.add(pair[0]);
			matB.add(pair[1]);
		}
		
		matrixA.setMatrix(matA);
		matrixB.setMatrix(matB);
		
		return new MatrixFinite[] {matrixA, matrixB};
	}

	
	/**
	 * Specifies what element is at (row, col).
	 * @param row the index of the row
	 * @param col the index of the column
	 * @param element the element that should replace the existing entry. 
	 */
	private void setIndex(int row, int col, int element) {
		this.matrix.get(row).setElement(col, element);
	}
	
	
	/**
	 * Performs the row operation row(j) - scalar*row(i). Used for Gaussian elimination. 
	 * @param j index of the subtracted row.
	 * @param i index of the row which is used for subtraction.
	 * @param scalar specifies what multiple of row(i) should be subtracted.
	 */
	public void scalarRowSubstraction(int j, int i, int scalar) {
		
		VectorFinite vectorI = this.matrix.get(i);
		VectorFinite vectorJ = this.matrix.get(j);
		
		//Finds the additive inverse of the scalar in the finite field
		int scalar_addInv = this.finiteRing.addInverse(scalar);
		
		//Performs the row-operation
		vectorJ.addMultipleOfVec(vectorI, scalar_addInv);
	}
	
	
	/**
	 * Multiplies a row with a scalar. Used for Gaussian elimination. 
	 * @param row the index of the row
	 * @param scalar the scalar the row should be multiplied with.
	 */
	public void scalarRowMult(int row, int scalar) {
		this.matrix.get(row).scalarMult(scalar);
	}
	
	
	/**
	 * Swaps two rows. Used for Gaussian elimination. 
	 * @param i row with index i
	 * @param j row with index j
	 */
	public void swapRows(int i, int j) {
		
		VectorFinite rowJ = this.getMatrix().get(j);
		VectorFinite rowI = this.getMatrix().get(i);
		
		this.getMatrix().set(j, rowI);
		this.getMatrix().set(i, rowJ);
	}

	
	/**
	 * Multiplies this matrix with a column vector from the right
	 * @param vec the column vector to multiply this matrix with. 
	 * @return the result after the multiplication, a new vector
	 */
	public VectorFinite vectorMulti(VectorFinite vec) {
		return vec.matrix(this);
	}

	
	/**
	 * Returns the transpose of the matrix.
	 */
	public void transpose() {
		
		ArrayList<VectorFinite> mat = new ArrayList<VectorFinite>();
		
		for (int i = 0; i < this.row; i++) {
			ArrayList<Integer> col = new ArrayList<Integer>();
			
			for (int j = 0; j < this.row; j++) {
				col.add(this.getIndex(j, i));
			}
			
			VectorFinite column = new VectorFinite(col, col.size());
			mat.add(column);
		}
		
		this.matrix = mat;
	}
	
	
	/**
	 * A method that multiplies this matrix with a matrix from the right
	 * @param mat the matrix this object should be multiplied with
	 * @return the result of the matrix multiplication
	 */
	public MatrixFinite matrixRightMult(MatrixFinite mat) {
		
		MatrixFinite A = new MatrixFinite(this.row, this.finiteRing.getN());
		
		//If this is done to square a matrix, it has to be multiplied by a deep copy
		if(this == mat) {
			mat = this.clone();
		}
		
		//Changes rows to columns, for easy access
		mat.transpose();
		
		//array stores the values
		ArrayList<VectorFinite> array = new ArrayList<VectorFinite>();
		
		//Performs dot-product with rows from this, and columns from "mat".
		for (VectorFinite row : this.getMatrix()) {
			array.add(mat.vectorMulti(row));
		}
		
		//Change "mat" back to original
		mat.transpose();
		
		A.setMatrix(array);
		
		return A;
	}
	
	
	/**
	 * A method specifically designed for encryption and decryption in a {@link Key}. 
	 * @param blockStrings typically a matrix-representation of a string, which is calculated using {@link StringConverter}. Each block are thought of as column vectors.
	 * @return the result of this matrix multiplied by a series of column vectors, resulting in a new matrix
	 */
	public MatrixFinite matrixRightTransMult(MatrixFinite blockStrings) {
		
		MatrixFinite A = new MatrixFinite(blockStrings.getRow(), this.finiteRing.getN());
		
		//array stores the values
		ArrayList<VectorFinite> array = new ArrayList<VectorFinite>();
		
		//Performs dot-product with rows from this, and columns from "mat".
		for (VectorFinite row : blockStrings.getMatrix()) {
			array.add(row.matrix(this));
		}
		
		A.setMatrix(array);
		
		return A;
	}


	/**
	 * A method for getting values at a specified in the matrix.
	 * @param row the index of the row of the entry
	 * @param col the index of the row of the entry
	 * @return returns the {@link Integer} at (row, col) in the matrix.
	 */
	public int getIndex(int row, int col) {
		return this.matrix.get(row).getElement(col);
	}
	

	public String toString() {
		String s = "";
		for (VectorFinite row : this.matrix) {
			s += row.toString();
			s += '\n';
		}
		return s;
	}
	

	public ArrayList<VectorFinite> getMatrix() {
		return matrix;
	}

	
	public int getRow() {
		return row;
	}

	
	public FiniteRing getFiniteRing() {
		return this.finiteRing;
	}
	
	
	public void setMatrix(ArrayList<VectorFinite> mat) {
		this.matrix = mat;
	}
	
	
	/**
	 * A method that returns a clone of the matrix, such that various operations such that computing {@link #inverse()} etc.
	 * does not change the matrix.
	 */
	public MatrixFinite clone() {
		MatrixFinite M = new MatrixFinite(this.row, this.finiteRing.getN());
		ArrayList<VectorFinite> copy = new ArrayList<VectorFinite>();
		
		for(VectorFinite row : this.matrix) {
			copy.add(row.clone());
		}
		M.setMatrix(copy);
		
		return M; 
	}
}
