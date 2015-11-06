package l5;

import java.util.LinkedList;
import java.util.List;

/**
 * fuzzy membership functions:
 * 
 * In this project, all the fuzzy sets has the same shape membership function.
 * Though these fuzzy set has different x range - some 
 * is [0, 1] and the other is [-1, 1] - we can still use this single 
 * class to represent the function.
 * 
 * We can use some function transform formula to transfer between the
 * [-1, 1] and the [0, 1] one. BTW, The function 
 * represents by this class is in range [-1, 1].
 *
 */
public class FzMemberShipFuncs {

	private FzMemberShipFunc[] memberShipFuncs = new FzMemberShipFunc[5];

	public FzMemberShipFuncs() {

		/**
		 * trapezoid membership function for set1
		 *  degree = 1            (-1.0 <= v <= -0.8)
		 *  degree = -2.5 * v - 1 (-0.8 <= v <= -0.4)
		 *  degree = 0       
		 */
		memberShipFuncs[0] = new FzMemberShipFunc() {

			@Override
			public double getDegree(double v) {
				if (v >= -1.0 && v <= -0.8) {
					return 1;
				} else if (v >= -0.8 && v <= -0.4) {
					return -2.5 * v - 1;
				} else {
					return 0;
				}
			}

			@Override
			public boolean isInSupportRange(double v) {
				return v >= -1.0 && v < -0.4;
			}

			@Override
			public double getCenter() {
				return -0.7;
			}

			@Override
			public double getArea(double clip) {
				return getClippedTrapezoidArea(clip, 0.6, 0.2, 1);
			}
		};
		/**
		 * symmetric triangular membership function for set2
		 *  degree = 2.5 * v + 2 (-0.8 <= v <= -0.4)
		 *  degree = -2.5 * v    (-0.4 <= v <= 0)
		 *  degree = 0         
		 */
		memberShipFuncs[1] = new FzMemberShipFunc() {

			@Override
			public double getDegree(double v) {
				if (v >= -0.8 && v <= -0.4) {
					return 2.5 * v + 2;
				} else if (v >= -0.4 && v <= 0) {
					return -2.5 * v;
				} else {
					return 0;
				}
			}

			@Override
			public boolean isInSupportRange(double v) {
				return v > -0.8 && v < 0;
			}

			@Override
			public double getCenter() {
				return -0.4;
			}

			@Override
			public double getArea(double clip) {
				return getClippedTriangleArea(clip, 0.8, 1);
			}
		};
		/**
		 * symmetric triangular membership function for set3
		 *  degree = 2.5 * v + 1  (-0.4 <= v <= 0)
		 *  degree = -2.5 * v + 1 (0 <= v <= 0.4)
		 *  degree = 0    
		 */
		memberShipFuncs[2] = new FzMemberShipFunc() {

			@Override
			public double getDegree(double v) {
				if (v >= -0.4 && v <= 0) {
					return 2.5 * v + 1;
				} else if (v >= 0 && v <= 0.4) {
					return -2.5 * v + 1;
				} else {
					return 0;
				}
			}

			@Override
			public boolean isInSupportRange(double v) {
				return v > -0.4 && v < 0.4;
			}

			@Override
			public double getCenter() {
				return 0;
			}

			@Override
			public double getArea(double clip) {
				return getClippedTriangleArea(clip, 0.8, 1);
			}
		};
		/**
		 * symmetric triangular membership function for set4
		 *  degree = 2.5 * v      (0 <= v <= 0.4)
		 *  degree = -2.5 * v + 2 (0.4 <= v <= 0.8)
		 *  degree = 0     
		 */
		memberShipFuncs[3] = new FzMemberShipFunc() {

			@Override
			public double getDegree(double v) {
				if (v >= 0 && v <= 0.4) {
					return 2.5 * v;
				} else if (v >= 0.4 && v <= 0.8) {
					return -2.5 * v + 2;
				} else {
					return 0;
				}
			}

			@Override
			public boolean isInSupportRange(double v) {
				return v > 0 && v < 0.8;
			}

			@Override
			public double getCenter() {
				return 0.4;
			}

			@Override
			public double getArea(double clip) {
				return getClippedTriangleArea(clip, 0.8, 1);
			}
		};
		/**
		 * trapezoid membership function for set5
		 *  degree = 2.5 * v - 1 (0.4 <= v <= 0.8)
		 *  degree = 1           (0.8 <= v <= 1)
		 *  degree = 0      
		 */
		memberShipFuncs[4] = new FzMemberShipFunc() {

			@Override
			public double getDegree(double v) {
				if (v >= 0.4 && v <= 0.8) {
					return 2.5 * v - 1;
				} else if (v >= 0.8 && v <= 1) {
					return 1;
				} else {
					return 0;
				}
			}

			@Override
			public boolean isInSupportRange(double v) {
				return v > 0.4 && v <= 1;
			}

			@Override
			public double getCenter() {
				return 0.7;
			}

			@Override
			public double getArea(double clip) {
				return getClippedTrapezoidArea(clip, 0.6, 0.2, 1);
			}
		};
	}

	public FzElement[] fuzzify(double v) {
		List<FzElement> eles = new LinkedList<FzElement>();
		for (int i = 0; i < memberShipFuncs.length; i++) {
			FzMemberShipFunc f = memberShipFuncs[i];
			if (f.isInSupportRange(v)) {
				double degree = f.getDegree(v);
				if (degree != 0)
					eles.add(new FzElement(i, f.getDegree(v)));
			}
		}
		return eles.toArray(new FzElement[] {});
	}

	public double getArea(double clip, int funcIndex) {
		return memberShipFuncs[funcIndex].getArea(clip);
	}

	public double getCenter(int funcIndex) {
		return memberShipFuncs[funcIndex].getCenter();
	}

	/**
	 * calculate the clipped triangle area.
	 * @param clip
	 * the clipped height start from the top
	 * @param bottom
	 * the triangle bottom length
	 * @param height
	 * the triangle original height
	 * @return
	 */
	private double getClippedTriangleArea(double clip, double bottom,
			double height) {
		double h = height - clip;
		double u = bottom * clip / height;
		return (u + bottom) * h / 2;
	}

	/**
	 * calculate the clipped trapezoid area.
	 * @param clip
	 * the clipped height start from the top
	 * @param bottom
	 * the trapezoid original bottom length
	 * @param upper
	 * the trapezoid original upper length
	 * @param height
	 * the triangle original height
	 * @return
	 */
	private double getClippedTrapezoidArea(double clip, double bottom,
			double upper, double height) {
		double h = height - clip;
		double u = (bottom - upper) * clip / height + upper;
		return (u + bottom) * h / 2;
	}

	// unit test
	public static void main(String[] args) {
		double threshold = 0.000001;
		FzMemberShipFuncs t = new FzMemberShipFuncs();
		// test fuzzify
		// boundary conditions
		// -1.0, -0.8, -0.4, 0, 0.4, 0.8, 1.0
		FzElement[] es = t.fuzzify(-1.0);
		assert (es.length == 1);
		assert (es[0].getIndex() == 0);
		assert (es[0].getDegree() == 1);

		es = t.fuzzify(-0.8);
		assert (es.length == 1);
		assert (es[0].getIndex() == 0);
		assert (es[0].getDegree() == 1);

		es = t.fuzzify(-0.4);
		assert (es.length == 1);
		assert (es[0].getIndex() == 1);
		assert (es[0].getDegree() == 1);

		es = t.fuzzify(0);
		assert (es.length == 1);
		assert (es[0].getIndex() == 2);
		assert (es[0].getDegree() == 1);

		es = t.fuzzify(0.4);
		assert (es.length == 1);
		assert (es[0].getIndex() == 3);
		assert (es[0].getDegree() == 1);

		es = t.fuzzify(0.8);
		assert (es.length == 1);
		assert (es[0].getIndex() == 4);
		assert (es[0].getDegree() == 1);

		es = t.fuzzify(1.0);
		assert (es.length == 1);
		assert (es[0].getIndex() == 4);
		assert (es[0].getDegree() == 1);

		// other conditions
		// -0.9, -0.7, -0.3, 0.2, 0.6, 0.85
		es = t.fuzzify(-0.9);
		assert (es.length == 1);
		assert (es[0].getIndex() == 0);
		assert (es[0].getDegree() == 1);

		es = t.fuzzify(-0.7);
		assert (es.length == 2);
		assert (es[0].getIndex() == 0);
		assert (es[0].getDegree() - 0.75 < threshold);
		assert (es[1].getIndex() == 1);
		assert (es[1].getDegree() - 0.25 < threshold);

		es = t.fuzzify(-0.3);
		assert (es.length == 2);
		assert (es[0].getIndex() == 1);
		assert (es[0].getDegree() - 0.75 < threshold);
		assert (es[1].getIndex() == 2);
		assert (es[1].getDegree() - 0.25 < threshold);

		es = t.fuzzify(0.2);
		assert (es.length == 2);
		assert (es[0].getIndex() == 2);
		assert (es[0].getDegree() - 0.5 < threshold);
		assert (es[1].getIndex() == 3);
		assert (es[1].getDegree() - 0.5 < threshold);

		es = t.fuzzify(0.6);
		assert (es.length == 2);
		assert (es[0].getIndex() == 3);
		assert (es[0].getDegree() - 0.5 < threshold);
		assert (es[1].getIndex() == 4);
		assert (es[1].getDegree() - 0.5 < threshold);

		es = t.fuzzify(0.85);
		assert (es.length == 1);
		assert (es[0].getIndex() == 4);
		assert (es[0].getDegree() == 1);

		// test get area
		assert (t.getArea(0.5, 0) - 0.25 < threshold);
		assert (t.getArea(1, 0) < threshold);
		assert (t.getArea(0, 0) - 0.4 < threshold);

		assert (t.getArea(0.5, 1) - 0.3 < threshold);
		assert (t.getArea(1, 1) - 0 < threshold);
		assert (t.getArea(0, 1) - 0.4 < threshold);

		assert (t.getArea(0.5, 2) - 0.3 < threshold);
		assert (t.getArea(1, 2) - 0 < threshold);
		assert (t.getArea(0, 2) - 0.4 < threshold);

		assert (t.getArea(0.5, 3) - 0.3 < threshold);
		assert (t.getArea(1, 3) - 0 < threshold);
		assert (t.getArea(0, 3) - 0.4 < threshold);

		assert (t.getArea(0.5, 4) - 0.25 < threshold);
		assert (t.getArea(1, 4) - 0 < threshold);
		assert (t.getArea(0, 4) - 0.4 < threshold);

		// test get center
		assert (t.getCenter(0) - -0.7 < threshold);
		assert (t.getCenter(1) - -0.4 < threshold);
		assert (t.getCenter(2) - 0 < threshold);
		assert (t.getCenter(3) - 0.4 < threshold);
		assert (t.getCenter(4) - 0.7 < threshold);

		assert (false) : "all tests passed!";
	}
}

/**
 * Represent a single membership function
 * @author pengfan
 *
 */
interface FzMemberShipFunc {

	/**
	 * calculate the fuzzy degree
	 * @param v
	 * @return
	 */
	public double getDegree(double v);

	/**
	 * is the input inside the support range
	 * @param v
	 * @return
	 */
	public boolean isInSupportRange(double v);

	/**
	 * get the center of the membership function
	 * @return
	 */
	public double getCenter();

	/**
	 * get the area of the membership function after been clipped 
	 * @param clip
	 * @return
	 */
	public double getArea(double clip);
}
