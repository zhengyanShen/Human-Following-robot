package l5;

/**
 * fuzzy sets
 *
 */
public class FzSets {

	/**
	 * the robot angle velocity sets
	 */
	public enum AV {
		BIGTURNLEFT, TURNLEFT, NOCHANGE, TURNRIGHT, BIGTURNRIGHT;
	}

	/**
	 * the robot drive velocity sets
	 */
	public enum V {
		VERYSLOW, SLOW, NOMINAL, FAST, VERYFAST;
	}

	/**
	 * the human x position sets: 
	 * the left/right vertical distance between the human and the camera center 
	 * in the camera view
	 */
	public enum X {
		FARLEFT, LEFT, CENTER, RIGHT, FARRIGHT;
	}

	/**
	 * the human x velocity(derivative) sets: 
	 * the speed human moving left/right in the camera view 
	 */
	public enum XD {
		RAPIDLEFT, LEFT, NOCHANGE, RIGHT, RAPIDRIGHT;
	}

	/**
	 * the human y position sets: the distance between the human and the robot
	 * In this program, we using human height to represent the y position
	 */
	public enum Y {
		VERYSMALL, SMALL, MEDIUM, LARGE, VERYLARGE;
	}

	/**
	 * the human y velocity(derivative) sets: the speed human moving away/close to the robot
	 */
	public enum YD {
		RAPIDCLOSINGIN, CLOSINGIN, STEADY, MOVINGAWAY, RAPIDMOVINGAWAY;
	}
}
