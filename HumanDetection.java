package l5;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.googlecode.javacpp.*;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import ohmm.*;

import ohmm.CvBase;

/**
 * g6: (asac 502 565 500 610)
 * Key event:
 * t: start calibration phase
 * r: start grasp
 * e: start debug
 * w: stop debug
 * 
 * This class will detect the specify object.
 * There is two main function for this class, calibrate and detect.
 * The calibrate will store the current object position as the
 * standard position. the detect will detect the current object
 * position, and calculate the x diff and y diff with the standard position. 
 */
public class HumanDetection extends CvBase {

	/** Default max FPS. **/
	public static final int DEF_MAX_FPS = 5;

	/** Application name. **/
	public static final String APPNAME = "ObjectDetection";

	/** Time to quit? **/
	protected boolean allDone = false;

	/** Clicked pixel coords pending RGB dump, or -1 if none. **/
	protected int[] dumpXY = new int[] { -1, -1 };

	/** Processed image buffer, allocated on first call to {@link #process}. **/
	protected IplImage procImg = null;
	CvMemStorage storage = CvMemStorage.create();

	/** members for lab4 **/
	private CvSeq contour = new CvSeq();
	
	private CvScalar colorToBeDetected = null;
	// hsv img
	private IplImage hsvimg = null;

	// binary img
	private IplImage bimg = null;

	// img used for debug
	private IplImage debugimg = null;

	// debug flag
	private boolean debug = false;

	// calibrate phase flag
	private boolean calibrate = false;

	// start grasp flag
	private boolean run = false;
//	private static final int H_THRESHOLD = 10;
	private static final int H_THRESHOLD = 70;
	private static final int S_THRESHOLD = 50;
	private static final int V_THRESHOLD = 50;
	private static final File CALIBRATE_FILE = new File("calibrate.txt");
	private static final String SEP = " ";
	public static final int NON_OBJECT = -9999;

	// current object position
	private int[] humanP = new int[3];

	// calibrate object position
	private int[] calibrateP = new int[2];

	/**
	 * Sets options for the demo.
	 * 
	 * @throws IOException
	 **/
	public HumanDetection() throws IOException {
		super(APPNAME);
		maxFPS = DEF_MAX_FPS;
		readCalibratePos();
	}

	/** Help for the extra command line parameter. **/
	protected void cmdHelpExt() {
		System.out.println("F: desired max FPS, default " + DEF_MAX_FPS);
	}

	/** Shows the extra command line parameter. **/
	protected String cmdHelpExtParams() {
		return "[F] ";
	}

	/** Help for the extra GUI keyboard commands. **/
	protected void guiHelpExt() {
		System.out.println("f/s -- increase/decrease max FPS");
		System.out.println("d -- done processing");
	}

	/** Shows how to do image processing. **/
	protected IplImage process(IplImage frame) {

		// get dimensions of the grabbed frame
		int w = frame.width(), h = frame.height();

		// cannot modify grabbed frame bits!

		// allocate processed image buffer once only
		if (procImg == null)
			procImg = IplImage.create(cvSize(w, h), IPL_DEPTH_8U, 3);
		if (hsvimg == null)
			hsvimg = IplImage.create(cvSize(w, h), IPL_DEPTH_8U, 3);
		if (bimg == null)
			bimg = IplImage.create(cvSize(w, h), IPL_DEPTH_8U, 1);
		if (debugimg == null)
			debugimg = IplImage.create(cvSize(w, h), IPL_DEPTH_8U, 1);

		// technically this is not needed here because we are about to copy over
		// all
		// pixels, but this shows one way to clear an image
		cvSet(procImg, CvScalar.BLACK, null);

		// copy pixels to output
		cvCopy(frame, procImg);
		detectObject(frame);
		if (debug && debugimg != null) {
			return debugimg;
		} else {
			return procImg;
		}
	}

	/**
	 * detect object on the current frame
	 * @param frame
	 */
	private void detectObject(IplImage frame) {
		// convert to hsv img
		cvCvtColor(frame, hsvimg, CV_BGR2HSV_FULL);

		// handoff clicked pixel coords from handleMouse()
		int x = -1, y = -1;
		synchronized (dumpXY) {
			x = dumpXY[0];
			y = dumpXY[1];
			dumpXY[0] = dumpXY[1] = -1;
		}

		// show pixel color at clicked pixel coords, if any
		if ((frame != null) && (x >= 0) && (y >= 0)) {
			System.out.println("pixel: (" + x + ", " + y + ")");
			colorToBeDetected = averagePixel(hsvimg, y, x, 7);
			System.out.println("hsv: H " + colorToBeDetected.val(0) + ", S " + colorToBeDetected.val(1)
					+ ", V " + colorToBeDetected.val(2));
		}

		if (colorToBeDetected != null) {

			humanP = getHumanXHY(colorToBeDetected);
			if (humanP[0] != NON_OBJECT && humanP[1] != NON_OBJECT) {
				if (calibrate && (x >= 0) && (y >= 0)) {
					// calibrate phase
					System.arraycopy(humanP, 0, calibrateP, 0, 2);
					storeCalibratePos(humanP);
					calibrate = false;
				}
			}
		}
	}

	/**
	 * get the human x and height and y
	 * @param blobColor
	 * @return
	 */
	private int[] getHumanXHY(CvScalar blobColor) {
		CvScalar hsv_min = cvScalar(blobColor.val(0) - H_THRESHOLD,
				blobColor.val(1) - S_THRESHOLD, blobColor.val(2) - V_THRESHOLD,
				0);
		CvScalar hsv_max = cvScalar(blobColor.val(0) + H_THRESHOLD,
				blobColor.val(1) + S_THRESHOLD, blobColor.val(2) + V_THRESHOLD,
				0);
		cvInRangeS(hsvimg, hsv_min, hsv_max, bimg);
		cvErode(bimg, bimg, null, 1);
		// dilate 6 times to remove the big black spot
		// inside the human body.
		cvDilate(bimg, bimg, null, 6);
		// copy the originam bimg for debug
		cvCopy(bimg, debugimg);
		cvFindContours(bimg, storage, contour, Loader.sizeof(CvContour.class),
				CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE);

		CvRect r = null;
		int maxArea = -1;
		for (CvSeq c = contour; c != null && !c.isNull(); c = c.h_next()) {
			if (c.elem_size() > 0) {
				CvRect r1 = cvBoundingRect(c, 0);
				int area = r1.width() * r1.height();
				if (area > maxArea) {
					r = r1;
					maxArea = area;
				}
			}
		}
		int x = NON_OBJECT;
		int h = NON_OBJECT;
		int y = NON_OBJECT;
		if (r != null) {
			x = r.x() + r.width() / 2;
			h = r.height();
			y = r.y() + r.height() / 2;
			if (debug) {
				System.out.println("x = " + x + " h = " + h + " y = " + y);
			}
			// draw the rectangle
			cvRectangle(procImage, cvPoint(r.x(), r.y()),
					cvPoint(r.x() + r.width(), r.y() + r.height()),
					CvScalar.RED, 1, CV_AA, 0);
		}

		cvClearMemStorage(storage);
		return new int[] { x, h, y };
	}

	/**
	 * calculate the average color to improve the precise
	 * @param bimg2
	 * @param y
	 * @param x
	 * @param layerNum
	 * @return
	 */
	private CvScalar averagePixel(IplImage bimg2, int y, int x, int layerNum) {
		double sum0 = 0;
		double sum1 = 0;
		double sum2 = 0;
		int min = x - layerNum;
		int max = x + layerNum;
		for (int i = min; i <= max; i++) {
			for (int j = min; j <= max; j++) {
				CvScalar c = cvGet2D(hsvimg, y, x);
				sum0 += c.val(0);
				sum1 += c.val(1);
				sum2 += c.val(2);
			}
		}
		double squareL = (double) (layerNum * 2 + 1);
		return cvScalar(sum0 / (squareL * squareL), sum1 / (squareL * squareL),
				sum2 / (squareL * squareL), 0);
	}

	/**
	 * store the calibrate position to file
	 * @param p
	 */
	private void storeCalibratePos(int[] p) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(CALIBRATE_FILE));
			bw.write(String.valueOf(p[0]));
			bw.write(SEP);
			bw.write(String.valueOf(p[1]));
			bw.newLine();
			System.out.println("store calibrate position:" + p[0] + " " + p[1]);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.flush();
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * read the calibrate position from file
	 */
	private void readCalibratePos() {
		if (CALIBRATE_FILE.exists()) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(CALIBRATE_FILE));
				String[] line = br.readLine().split(SEP);
				calibrateP[0] = Integer.parseInt(line[0]);
				calibrateP[1] = Integer.parseInt(line[1]);
				System.out.println("read calibrate position:" + calibrateP[0]
						+ " " + calibrateP[1]);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/** Are we {@link #allDone}? **/
	protected boolean doneProcessing() {
		return allDone;
	}

	/** Handle our custom keypresses. **/
	protected boolean handleKeyExt(int code) {
		switch (code) {
		case 'W':
			debug = false;
			System.out.println("stop debug.");
			break;
		case 'E':
			debug = true;
			System.out.println("start debug.");
			break;
		case 'T':
			calibrate = true;
			System.out.println("start calibrate.");
			break;
		case 'R':
			run = true;
			System.out.println("start run.");
			break;
		case 'S':
			run = false;
			System.out.println("stop run.");
			break;
		case 's':
			maxFPS += (code == 'f') ? 1 : ((maxFPS > 1) ? -1 : 0);
			System.out.println("max FPS: " + maxFPS);
			break;
		case 'd':
			allDone = true;
			break;
		default:
			msg("unhandled keycode: " + code);
		}
		return true;
	}

	/**
	 * Show pixel color at mouse click.
	 * 
	 * Chains to default impl to show debug info about mouse events.
	 **/
	protected void handleMouse(int event, int x, int y, int flags) {
		if (event == CV_EVENT_LBUTTONDOWN)
			synchronized (dumpXY) {
				dumpXY[0] = x;
				dumpXY[1] = y;
			}
		super.handleMouse(event, x, y, flags);
	}

	/** Releases allocated memory. **/
	public void release() {
		// important to explicitly null and check for non-null to avoid double
		// releasing even if this is called more than once, which it may be
		// since
		// CvBase.finalize() calls release()
		if (storage != null) {
			storage.release();
			storage = null;
		}
		if (procImg != null) {
			procImg.release();
			procImg = null;
		}
		super.release();
	}

	/** Handle extra command line parameters. **/
	public int initExt(int argc, String argv[], int ate) {

		// are there any more command line params after init() finished eating
		// them?
		if (argc > ate) {
			try {
				maxFPS = Integer.parseInt(argv[ate]);
				ate++;
				System.out.println("max FPS: " + maxFPS);
			} catch (NumberFormatException nfe) {
				System.err.println("max FPS not an int, using default: "
						+ DEF_MAX_FPS);
			}
		} else {
			System.out.println("using default max FPS: " + maxFPS);
		}

		return ate;
	}

	/**
	 * get the x and y diff, the VisualServoing
	 * class will use these diff value to calculate
	 * the angle and linear velocity
	 * @return
	 */
	public int[] getXHY() {
		if (humanP[0] == NON_OBJECT || humanP[1] == NON_OBJECT) {
			return new int[] { NON_OBJECT, NON_OBJECT, NON_OBJECT };
		}
		return new int[] { humanP[0] - calibrateP[0], humanP[1], humanP[2] };
	}

	public int getCalibratedY() {
		return calibrateP[1];
	}

	public boolean isRun() {
		return run;
	}

	/** Program entry point. **/
	public static void main(String argv[]) {
		try {
			OHMMDrive ohmm = (OHMMDrive) OHMM.makeOHMM(new String[] { "-r",
					"/dev/ttyACM1" });
			Grasp g = new Grasp(ohmm);
			g.init();
			HumanDetection cvd = new HumanDetection();
			cvd.init(argv.length, argv);
			cvd.mainLoop();
			cvd.release();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
