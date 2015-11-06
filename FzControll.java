package l5;

import java.util.Collection;
import java.util.Map;

import ohmm.OHMM;
import ohmm.OHMMDrive;

/**
 * Implements fuzzy control algorithm in following paper:
 *   M. Tarokh* andP. Ferrari. 
 *   Case study: robotic person following using fuzzy control and image segmentation.
 *   Journal of Robotic Systems Volume 20, Issue 9, pages 557-568, September 2003.
 * 
 * 
 * process:
 * 1. fuzzifier human position, human speed
 *     a. normalize input
 *     b. call membership function to get the degree
 * 2. fire rule according the rule table
 * 3. aggregate rule
 *     a. aggregate rule use fuzzy or(max), get the degree of the rule output
 *     b. clip the membership function by the degree
 *     c. aggregate
 * 4. defuzzification, calculate the centroid of the aggreated output
 * 5. denormalize, get the result
 *
 */
public class FzControll {

	private FzMemberShipFuncs fzMemberShipFuncs = new FzMemberShipFuncs();

	// normalizer for human x position
	private FzNormalizer xNormalizer = new FzNormalizer(-140, 140);
	// normalizer for human x speed
	private FzNormalizer xdNormalizer = new FzNormalizer(-140, 140);
	// normalizer for human y position
	private FzNormalizer yNormalizer;
	// normalizer for human y speed
	private FzNormalizer ydNormalizer = new FzNormalizer(-100, 100);
	// normalizer for robot velocity
	private FzNormalizer vNormalizer = new FzNormalizer(0, 250);
	// normalizer for robot angle velocity
	private FzNormalizer avNormalizer = new FzNormalizer(-0.8, 0.8);

	// velocity rule
	private FzRule vrule = new FzVRule();

	// angle velocity rule
	private FzRule avrule = new FzAVRule();

	private OHMMDrive ohmm;

	private HumanDetection humanDetect;

	public FzControll(OHMMDrive ohmm, HumanDetection humanDetect) {
		this.ohmm = ohmm;
		this.humanDetect = humanDetect;
	}

	public void followHuman() {

		// wait for press r to start following human
		while (true) {
			if (humanDetect.isRun() == true) {

				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		yNormalizer = new FzNormalizer(humanDetect.getCalibratedY() - 40,
				humanDetect.getCalibratedY());
		int xhy[];
		// x: human x position
		// px: previous human x position
		// xd(x derivative): human vertical speed
		// y is similar.
		double x, px, xd, y, py, yd;
		px = HumanDetection.NON_OBJECT;
		py = HumanDetection.NON_OBJECT;
		while (true) {

			xhy = humanDetect.getXHY();
			if (xhy[0] == HumanDetection.NON_OBJECT
					&& xhy[1] == HumanDetection.NON_OBJECT) {
				// human not visible;
				System.out.println("human is not visible!");
				ohmm.driveSetVW(0, 0);
				px = HumanDetection.NON_OBJECT;
				py = HumanDetection.NON_OBJECT;
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			x = xhy[0];
			y = xhy[1];
			xd = px != HumanDetection.NON_OBJECT ? (x - px) : 0;
			yd = py != HumanDetection.NON_OBJECT ? (y - py) : 0;
			px = x;
			py = y;

			double[] vw = getVW(x, xd, y, yd);

			if (xhy[2] <= 30) {
				// if the human is too near the robot,
				// the height will be very small, the
				// robot will consider the human is far way
				// and continue to drive forward. So we use
				// the human center y position to check if
				// the human is too near the human.
				vw[0] = 0;
			}
			System.out.println("x = " + xhy[0] + " y = " + xhy[1]);
			System.out.println("v = " + vw[0] + " w = " + (-vw[1]));
			// in the paper, turn left is negative
			// in our robot, turn left is positive
			// so we need neg the angle velocity
			ohmm.driveSetVW((float) vw[0], -(float) vw[1]);

			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * calculate the v and w by fuzzy controll
	 * @param x
	 * @param xd
	 * @param y
	 * @param yd
	 * @return
	 */
	private double[] getVW(double x, double xd, double y, double yd) {
		double[] vw = new double[2];
		double xn, xdn, yn, ydn;

		// normalize
		xn = xNormalizer.normalize(x);
		if (xn > 1 || xn < -1) {
			xn = xn > 0 ? 1 : -1;
		}
		xdn = xdNormalizer.normalize(xd);
		if (xdn > 1 || xdn < -1) {
			xdn = xdn > 0 ? 1 : -1;
		}
		yn = yNormalizer.normalize(y);
		yn = yn > 1 ? 1 : yn;
		ydn = ydNormalizer.normalize(yd);
		if (ydn > 1 || ydn < -1) {
			ydn = ydn > 0 ? 1 : -1;
		}

		// fuzzify:
		// x, xd, yd using the original membership function
		FzElement[] xFzEle = fzMemberShipFuncs.fuzzify(xn);
		FzElement[] xdFzEle = fzMemberShipFuncs.fuzzify(xdn);
		FzElement[] ydFzEle = fzMemberShipFuncs.fuzzify(ydn);

		// y membership function is different, the range change from [-1,
		// 1] to [0, 1] and the membership function sharp keeps same as the
		// original one. so it can be mapping into the original function by
		// multiply by 2 and then left move 1
		FzElement[] yFzEle = fzMemberShipFuncs.fuzzify(yn * 2 - 1);

		// fire the rule
		Map<Integer, FzElement> angleVelocitys = avrule.fire(xdFzEle, xFzEle);
		Map<Integer, FzElement> velocitys = vrule.fire(ydFzEle, yFzEle);
		System.out.println("x = " + x + " xd = " + xd + " y = " + y + " yd = "
				+ yd);
		System.out.println("xn = " + xn + " xdn = " + xdn + " yn = " + yn
				+ " ydn = " + ydn);

		// calculate the angle velocity centroid
		double av = calculateCentroid(angleVelocitys.values(), 1, 0);

		// calculate the velocity centroid, the velocity member ship
		// function range changed from [-1, 1] to [0, 1]
		// so we need do some transformation to get
		// the correct result: the range changed from 2 to 1, so the scale is
		// 0.5
		// the min value changed from -1 to 0, so the x offset is 1
		double v = calculateCentroid(velocitys.values(), 0.5, 1);
		if (yn == 1) {
			// fix a bug for fuzzy controll
			// in fuzzy controll, the velocity
			// can not reach to 0, it cause the robot
			// moving forward forever even if it has already
			// near to the human
			v = 0;
		}

		// denormalize
		vw[0] = vNormalizer.deNormalize(v);
		vw[1] = avNormalizer.deNormalize(av);
		return vw;
	}

	/**
	 * calculate centroid of the fuzzy output
	 * using following formula:
	 * Sum(function center * area after clipped) / Sum(area after clipped)
	 * @param fzElements 
	 * fuzzy elements.
	 * @param scale 
	 * the scale of the original membership function
	 * @param xoffset
	 * the x axis offset of the original membership function
	 * @return
	 */
	private double calculateCentroid(Collection<FzElement> fzElements,
			double scale, int xoffset) {
		double weightSum = 0;
		double areaSum = 0;
		for (FzElement fe : fzElements) {
			int index = fe.getIndex();
			double clipped = 1 - fe.getDegree();
			double center = (fzMemberShipFuncs.getCenter(index) + xoffset)
					* scale;
			double area = fzMemberShipFuncs.getArea(clipped, index) * scale;
			weightSum += center * area;
			areaSum += area;
		}
		return areaSum == 0 ? 0 : weightSum / areaSum;
	}

	/** Program entry point. **/
	public static void main(String argv[]) {
		try {
			OHMMDrive ohmm = (OHMMDrive) OHMM.makeOHMM(new String[] { "-r",
					"/dev/ttyACM1" });
			final HumanDetection humanDetect = new HumanDetection();
			humanDetect.init(argv.length, argv);
			Thread t = new Thread() {
				public void run() {
					humanDetect.mainLoop();
					humanDetect.release();
				}
			};
			t.start();

			FzControll motionController = new FzControll(ohmm, humanDetect);
			motionController.followHuman();

			t.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
