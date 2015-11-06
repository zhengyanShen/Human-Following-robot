**
 * <p>Demo of CvBase.</p>
 *
 * <p>This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.</p>
 *
 * <p>This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.</p>
 *
 * <p>You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA</p>
 *
 * <p>Copyright (c) 2011 Marsette A. Vona</p>
 **/

package l5;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

import ohmm.*;

/**
 * <p>Demo of {@link CvBase}.</p>
 *
 * <p>The main action is in {@link #process}.  Also shows how to handle custom
 * command line parameters and mouse and key events.</p>
 *
 * @author Marsette A. Vona
 **/
public class CvDemo extends CvBase {
  
  /** Default max FPS. **/
  public static final int DEF_MAX_FPS = 5;

  /** Application name.**/
  public static final String APPNAME = "CvDemo";

  /** Time to quit? **/
  protected boolean allDone = false;

  /** Clicked pixel coords pending RGB dump, or -1 if none. **/
  protected int[] dumpXY = new int[] {-1, -1};

  /** Processed image buffer, allocated on first call to {@link #process}. **/
  protected IplImage procImg = null;

  /** Sets options for the demo. **/
  public CvDemo() { super(APPNAME); maxFPS = DEF_MAX_FPS; }

  /** Help for the extra command line parameter. **/
  protected void cmdHelpExt() {
    System.out.println("F: desired max FPS, default "+DEF_MAX_FPS);
  }

  /** Shows the extra command line parameter. **/
  protected String cmdHelpExtParams() { return "[F] "; }

  /** Help for the extra GUI keyboard commands. **/
  protected void guiHelpExt() {
    System.out.println("f/s -- increase/decrease max FPS");
    System.out.println("d -- done processing");
  }

  /** Shows how to do image processing. **/
  protected IplImage process(IplImage frame) { 

    //handoff clicked pixel coords from handleMouse()
    int x = -1, y = -1;
    synchronized (dumpXY)
      { x = dumpXY[0]; y = dumpXY[1]; dumpXY[0] = dumpXY[1] = -1; }

    //show pixel color at clicked pixel coords, if any
    if ((frame != null) && (x >= 0) && (y >= 0)) {
      System.out.println("pixel: ("+x+", "+y+")");
      CvScalar s = cvGet2D(frame, y, x); //get pixel at row y, column x
      System.out.println("RGB: "+s.red()+", "+s.green()+", "+s.blue());
    }

    //get dimensions of the grabbed frame
    int w = frame.width(), h = frame.height();

    //cannot modify grabbed frame bits!

    //allocate processed image buffer once only
    if (procImg == null)
      procImg = IplImage.create(cvSize(w, h), IPL_DEPTH_8U, 3);

    //technically this is not needed here because we are about to copy over all
    //pixels, but this shows one way to clear an image
    cvSet(procImg, CvScalar.BLACK, null);
    
    //copy pixels to output
    cvCopy(frame, procImg);

    //draw a blue circle on top
    cvCircle(procImg,
             cvPoint(w/2, h/2), //center
             (int) Math.min(w/4, h/4), //radius
             CvScalar.BLUE, //color
             1, 8, 0); //line thickness, type, shift

    return procImg; //return a reference to the processed image
  }

  /** Are we {@link #allDone}? **/
  protected boolean doneProcessing() { return allDone; }

  /** Handle our custom keypresses. **/
  protected boolean handleKeyExt(int code) {
    switch (code) {
    case 'f': case 's':
      maxFPS += (code == 'f') ? 1 : ((maxFPS > 1) ? -1 : 0);
      System.out.println("max FPS: "+maxFPS);
      break;
    case 'd': allDone = true; break;
    default: msg("unhandled keycode: "+code);
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
      synchronized (dumpXY) { dumpXY[0] = x; dumpXY[1] = y; }
    super.handleMouse(event, x, y, flags);
  }

  /** Releases allocated memory. **/
  public void release() { 
    //important to explicitly null and check for non-null to avoid double
    //releasing even if this is called more than once, which it may be since
    //CvBase.finalize() calls release()
    if (procImg != null) { procImg.release(); procImg = null; }
    super.release();
  }

  /** Handle extra command line parameters. **/
  public int initExt(int argc, String argv[], int ate) {

    //are there any more command line params after init() finished eating them?
    if(argc > ate) {
      try {
        maxFPS = Integer.parseInt(argv[ate]); ate++;
        System.out.println("max FPS: "+maxFPS);
      } catch (NumberFormatException nfe) {
        System.err.println("max FPS not an int, using default: "+DEF_MAX_FPS);
      }
    } else {
      System.out.println("using default max FPS: "+maxFPS);
    }

    return ate;
  }
  
  /** Program entry point. **/
  public static void main(String argv[]) {
    CvDemo cvd = new CvDemo();
    cvd.init(argv.length, argv);
    cvd.mainLoop();
    cvd.release();
  }
}
