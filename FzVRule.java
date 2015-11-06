package l5;

/**
 * 
 * Fuzzy control rule table
 * 
 * Given the human x position and x velocity, get the velocity 
 * The rule table is slightly different from the paper, the upper right
 * and bottom left corner is also filled with content, because we do
 * not want the robot stop following except the human disappeared from the camera.
 * velocity rule table:
 * -------------------------------------------------------------------------------------
 * |                            |           human y position                           |
 * |                            |-------------------------------------------------------
 * |                            | VerySmall   Small     Medium     Large      VeryLarge|
 * -------------------------------------------------------------------------------------
 * |          | RapidClosingIn  | Nominal  | Slow     | VerySlow | VerySlow | VerySlow |
 * |          |                 |-------------------------------------------------------
 * |          | ClosingIn       | Fast     | Nominal  | Slow     | VerySlow | VerySlow |
 * |  human   |                 |-------------------------------------------------------
 * |    y     | Steady          | VeryFast | Fast     | Nominal  | Slow     | VerySlow |
 * | velocity |                 |-------------------------------------------------------
 * |          | MovingAway      | VeryFast | VeryFast | Fast     | Nominal  | Slow     |
 * |          |                 |-------------------------------------------------------
 * |          | RapidMovingAway | VeryFast | VeryFast | VeryFast | Fast     | Nominal  |
 * -------------------------------------------------------------------------------------
 * 
 * 
 */
public class FzVRule extends FzRule {

	/**
	 * velocity rule table
	 */
	private static FzSets.V[][] VRULE_TABLE = new FzSets.V[][]{
		{FzSets.V.NOMINAL, FzSets.V.SLOW, FzSets.V.VERYSLOW, FzSets.V.VERYSLOW, FzSets.V.VERYSLOW},
		{FzSets.V.FAST, FzSets.V.NOMINAL, FzSets.V.SLOW, FzSets.V.VERYSLOW, FzSets.V.VERYSLOW},
		{FzSets.V.VERYFAST, FzSets.V.FAST, FzSets.V.NOMINAL, FzSets.V.SLOW, FzSets.V.VERYSLOW},
		{FzSets.V.VERYFAST, FzSets.V.VERYFAST, FzSets.V.FAST, FzSets.V.NOMINAL, FzSets.V.SLOW},
		{FzSets.V.VERYFAST, FzSets.V.VERYFAST, FzSets.V.VERYFAST, FzSets.V.FAST, FzSets.V.NOMINAL}
	};
	
	public int fireRule(int ydIndex, int yIndex) {
		if (VRULE_TABLE[ydIndex][yIndex] == null)
		{
			return NULL;
		}
		return VRULE_TABLE[ydIndex][yIndex].ordinal();
	}
	
	// unit test
	public static void main(String[] args) {
		FzVRule t = new FzVRule();

		//RapidClosingIn VerySmall Nominal
		assert (FzSets.V.NOMINAL.ordinal() == t.fireRule(
				FzSets.YD.RAPIDCLOSINGIN.ordinal(), FzSets.Y.VERYSMALL.ordinal()));
		//RapidClosingIn Small Slow
		assert (FzSets.V.SLOW.ordinal() == t.fireRule(
				FzSets.YD.RAPIDCLOSINGIN.ordinal(), FzSets.Y.SMALL.ordinal()));
		//RapidClosingIn Medium VerySlow
		assert (FzSets.V.VERYSLOW.ordinal() == t.fireRule(
				FzSets.YD.RAPIDCLOSINGIN.ordinal(), FzSets.Y.MEDIUM.ordinal()));
		//RapidClosingIn Large VerySlow
		assert (FzSets.V.VERYSLOW.ordinal() == t.fireRule(
				FzSets.YD.RAPIDCLOSINGIN.ordinal(), FzSets.Y.LARGE.ordinal()));
		//RapidClosingIn VeryLarge null
		assert (t.NULL == t.fireRule(
				FzSets.YD.RAPIDCLOSINGIN.ordinal(), FzSets.Y.VERYLARGE.ordinal()));

		//ClosingIn VerySmall Fast
		assert (FzSets.V.FAST.ordinal() == t.fireRule(
				FzSets.YD.CLOSINGIN.ordinal(), FzSets.Y.VERYSMALL.ordinal()));
		//ClosingIn Small Nominal
		assert (FzSets.V.NOMINAL.ordinal() == t.fireRule(
				FzSets.YD.CLOSINGIN.ordinal(), FzSets.Y.SMALL.ordinal()));
		//ClosingIn Medium Slow
		assert (FzSets.V.SLOW.ordinal() == t.fireRule(
				FzSets.YD.CLOSINGIN.ordinal(), FzSets.Y.MEDIUM.ordinal()));
		//ClosingIn Large VerySlow
		assert (FzSets.V.VERYSLOW.ordinal() == t.fireRule(
				FzSets.YD.CLOSINGIN.ordinal(), FzSets.Y.LARGE.ordinal()));
		//ClosingIn VeryLarge null
		assert (t.NULL == t.fireRule(
				FzSets.YD.CLOSINGIN.ordinal(), FzSets.Y.VERYLARGE.ordinal()));
		
		//Steady VerySmall VeryFast
		assert (FzSets.V.VERYFAST.ordinal() == t.fireRule(
				FzSets.YD.STEADY.ordinal(), FzSets.Y.VERYSMALL.ordinal()));
		//Steady Small Fast
		assert (FzSets.V.FAST.ordinal() == t.fireRule(
				FzSets.YD.STEADY.ordinal(), FzSets.Y.SMALL.ordinal()));
		//Steady Medium Nominal
		assert (FzSets.V.NOMINAL.ordinal() == t.fireRule(
				FzSets.YD.STEADY.ordinal(), FzSets.Y.MEDIUM.ordinal()));
		//Steady Large Slow
		assert (FzSets.V.SLOW.ordinal() == t.fireRule(
				FzSets.YD.STEADY.ordinal(), FzSets.Y.LARGE.ordinal()));
		//Steady VeryLarge VerySlow
		assert (FzSets.V.VERYSLOW.ordinal() == t.fireRule(
				FzSets.YD.STEADY.ordinal(), FzSets.Y.VERYLARGE.ordinal()));

		//MovingAway VerySmall null
		assert (t.NULL == t.fireRule(
				FzSets.YD.MOVINGAWAY.ordinal(), FzSets.Y.VERYSMALL.ordinal()));
		//MovingAway Small VeryFast
		assert (FzSets.V.VERYFAST.ordinal() == t.fireRule(
				FzSets.YD.MOVINGAWAY.ordinal(), FzSets.Y.SMALL.ordinal()));
		//MovingAway Medium Fast
		assert (FzSets.V.FAST.ordinal() == t.fireRule(
				FzSets.YD.MOVINGAWAY.ordinal(), FzSets.Y.MEDIUM.ordinal()));
		//MovingAway Large Nominal
		assert (FzSets.V.NOMINAL.ordinal() == t.fireRule(
				FzSets.YD.MOVINGAWAY.ordinal(), FzSets.Y.LARGE.ordinal()));
		//MovingAway VeryLarge Slow
		assert (FzSets.V.SLOW.ordinal() == t.fireRule(
				FzSets.YD.MOVINGAWAY.ordinal(), FzSets.Y.VERYLARGE.ordinal()));

		//RapidMovingAway VerySmall null
		assert (t.NULL == t.fireRule(
				FzSets.YD.RAPIDMOVINGAWAY.ordinal(), FzSets.Y.VERYSMALL.ordinal()));
		//RapidMovingAway Small VeryFast
		assert (FzSets.V.VERYFAST.ordinal() == t.fireRule(
				FzSets.YD.RAPIDMOVINGAWAY.ordinal(), FzSets.Y.SMALL.ordinal()));
		//RapidMovingAway Medium VeryFast
		assert (FzSets.V.VERYFAST.ordinal() == t.fireRule(
				FzSets.YD.RAPIDMOVINGAWAY.ordinal(), FzSets.Y.MEDIUM.ordinal()));
		//RapidMovingAway Large Fast
		assert (FzSets.V.FAST.ordinal() == t.fireRule(
				FzSets.YD.RAPIDMOVINGAWAY.ordinal(), FzSets.Y.LARGE.ordinal()));
		//RapidMovingAway VeryLarge Nominal
		assert (FzSets.V.NOMINAL.ordinal() == t.fireRule(
				FzSets.YD.RAPIDMOVINGAWAY.ordinal(), FzSets.Y.VERYLARGE.ordinal()));
		

		assert (false) : "all tests passed!";
	}
}
