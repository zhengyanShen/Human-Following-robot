package l5;

/**
 * Represent a element in a fuzzy set,
 * contained the element index(start from 0) and the degree for that element.
 */
public class FzElement {
	private int eleIndex = -1;
	private double degree = -1;

	public FzElement(int index, double degree) {
		this.eleIndex = index;
		this.degree = degree;
	}
	
	public void setDegree(double degree) {
		this.degree = degree;
	}

	public int getIndex() {
		return eleIndex;
	}

	public double getDegree() {
		return degree;
	}

	/**
	 * fuzzy logic operator OR
	 * @param degree1
	 * @param degree2
	 * @return
	 */
	public static double or(double degree1, double degree2) {
		return Math.max(degree1, degree2);
	}

	/**
	 * fuzzy logic operator AND
	 * @param degree1
	 * @param degree2
	 * @return
	 */
	public static double and(double degree1, double degree2) {

		return Math.min(degree1, degree2);
	}
	
	public String toString()
	{
		return String.valueOf(eleIndex) + " " + String.valueOf(degree);
	}
}
