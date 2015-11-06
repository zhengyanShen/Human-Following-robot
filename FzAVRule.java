package l5;

/**
 * 
 * Fuzzy control rule table
 * 
 * Given the human x position and x velocity, get the angle velocity 
 * The rule table is slightly different from the paper, the upper left
 * and bottom right corner is also filled with content, because we do
 * not want the robot stop following except the human disappeared from the camera.
 * angle velocity rule table:
 * ----------------------------------------------------------------------------------------------------
 * |                       |           human x position                                               |
 * |                       |---------------------------------------------------------------------------
 * |                       | FarLeft        Left           Center         Right          FarRight     |
 * ----------------------------------------------------------------------------------------------------
 * |          | RapidLeft  | BigTurnLeft | BigTurnLeft  | BigTurnLeft  | TurnLeft     | NoChange     |
 * |          |            |---------------------------------------------------------------------------
 * |          | Left       | BigTurnLeft | BigTurnLeft  | TurnLeft     | NoChange     | TurnRight    |
 * |  human   |            |---------------------------------------------------------------------------
 * |    x     | NoChange   | BigTurnLeft  | TurnLeft     | NoChange     | TurnRight    | BigTurnRight |
 * | velocity |            |---------------------------------------------------------------------------
 * |          | Right      | TurnLeft     | NoChange     | TurnRight    | BigTurnRight | BigTurnRight |
 * |          |            |---------------------------------------------------------------------------
 * |          | RapidRight | NoChange     | TurnRight    | BigTurnRight | BigTurnRight | BigTurnRight |
 * ----------------------------------------------------------------------------------------------------
 *           
 */
public class FzAVRule extends FzRule {

	/**
	 * angle velocity rule table
	 */
	private static FzSets.AV[][] AVRULE_TABLE = new FzSets.AV[][] {
			{FzSets.AV.BIGTURNLEFT, FzSets.AV.BIGTURNLEFT, FzSets.AV.BIGTURNLEFT, FzSets.AV.TURNLEFT, FzSets.AV.NOCHANGE},
			{FzSets.AV.BIGTURNLEFT, FzSets.AV.BIGTURNLEFT, FzSets.AV.TURNLEFT, FzSets.AV.NOCHANGE, FzSets.AV.TURNRIGHT},
			{FzSets.AV.BIGTURNLEFT, FzSets.AV.TURNLEFT, FzSets.AV.NOCHANGE, FzSets.AV.TURNRIGHT, FzSets.AV.BIGTURNRIGHT},
			{FzSets.AV.TURNLEFT, FzSets.AV.NOCHANGE, FzSets.AV.TURNRIGHT, FzSets.AV.BIGTURNRIGHT, FzSets.AV.BIGTURNRIGHT},
			{FzSets.AV.NOCHANGE, FzSets.AV.TURNRIGHT, FzSets.AV.BIGTURNRIGHT, FzSets.AV.BIGTURNRIGHT, FzSets.AV.BIGTURNRIGHT} 
    };

	@Override
	public int fireRule(int xdIndex, int xIndex) {
		if (AVRULE_TABLE[xdIndex][xIndex] == null) {
			return NULL;
		}
		return AVRULE_TABLE[xdIndex][xIndex].ordinal();
	}

	// unit test
	public static void main(String[] args) {
		FzAVRule t = new FzAVRule();
		// RapidLeft FarLeft null
		assert (t.NULL == t.fireRule(FzSets.XD.RAPIDLEFT.ordinal(),
				FzSets.X.FARLEFT.ordinal()));
		// RapidLeft Left BigTurnLeft
		assert (FzSets.AV.BIGTURNLEFT.ordinal() == t.fireRule(
				FzSets.XD.RAPIDLEFT.ordinal(), FzSets.X.LEFT.ordinal()));
		// RapidLeft Center BigTurnLeft
		assert (FzSets.AV.BIGTURNLEFT.ordinal() == t.fireRule(
				FzSets.XD.RAPIDLEFT.ordinal(), FzSets.X.CENTER.ordinal()));
		// RapidLeft Right TurnLeft
		assert (FzSets.AV.TURNLEFT.ordinal() == t.fireRule(
				FzSets.XD.RAPIDLEFT.ordinal(), FzSets.X.RIGHT.ordinal()));
		// RapidLeft FarRight NoChange
		assert (FzSets.AV.NOCHANGE.ordinal() == t.fireRule(
				FzSets.XD.RAPIDLEFT.ordinal(), FzSets.X.FARRIGHT.ordinal()));

		// Left FarLeft null
		assert (t.NULL == t.fireRule(FzSets.XD.LEFT.ordinal(),
				FzSets.X.FARLEFT.ordinal()));
		// Left Left BigTurnLeft
		assert (FzSets.AV.BIGTURNLEFT.ordinal() == t.fireRule(
				FzSets.XD.LEFT.ordinal(), FzSets.X.LEFT.ordinal()));
		// Left Center TurnLeft
		assert (FzSets.AV.TURNLEFT.ordinal() == t.fireRule(
				FzSets.XD.LEFT.ordinal(), FzSets.X.CENTER.ordinal()));
		// Left Right NoChange
		assert (FzSets.AV.NOCHANGE.ordinal() == t.fireRule(
				FzSets.XD.LEFT.ordinal(), FzSets.X.RIGHT.ordinal()));
		// Left FarRight TurnRight
		assert (FzSets.AV.TURNRIGHT.ordinal() == t.fireRule(
				FzSets.XD.LEFT.ordinal(), FzSets.X.FARRIGHT.ordinal()));

		// NoChange FarLeft BigTurnLeft
		assert (FzSets.AV.BIGTURNLEFT.ordinal() == t.fireRule(
				FzSets.XD.NOCHANGE.ordinal(), FzSets.X.FARLEFT.ordinal()));
		// NoChange Left TurnLeft
		assert (FzSets.AV.TURNLEFT.ordinal() == t.fireRule(
				FzSets.XD.NOCHANGE.ordinal(), FzSets.X.LEFT.ordinal()));
		// NoChange Center NoChange
		assert (FzSets.AV.NOCHANGE.ordinal() == t.fireRule(
				FzSets.XD.NOCHANGE.ordinal(), FzSets.X.CENTER.ordinal()));
		// NoChange Right TurnRight
		assert (FzSets.AV.TURNRIGHT.ordinal() == t.fireRule(
				FzSets.XD.NOCHANGE.ordinal(), FzSets.X.RIGHT.ordinal()));
		// NoChange FarRight BigTurnRight
		assert (FzSets.AV.BIGTURNRIGHT.ordinal() == t.fireRule(
				FzSets.XD.NOCHANGE.ordinal(), FzSets.X.FARRIGHT.ordinal()));

		// Right FarLeft TurnLeft
		assert (FzSets.AV.TURNLEFT.ordinal() == t.fireRule(
				FzSets.XD.RIGHT.ordinal(), FzSets.X.FARLEFT.ordinal()));
		// Right Left NoChange
		assert (FzSets.AV.NOCHANGE.ordinal() == t.fireRule(
				FzSets.XD.RIGHT.ordinal(), FzSets.X.LEFT.ordinal()));
		// Right Center TurnRight
		assert (FzSets.AV.TURNRIGHT.ordinal() == t.fireRule(
				FzSets.XD.RIGHT.ordinal(), FzSets.X.CENTER.ordinal()));
		// Right Right BigTurnRight
		assert (FzSets.AV.BIGTURNRIGHT.ordinal() == t.fireRule(
				FzSets.XD.RIGHT.ordinal(), FzSets.X.RIGHT.ordinal()));
		// Right FarRight null
		assert (t.NULL == t.fireRule(FzSets.XD.RIGHT.ordinal(),
				FzSets.X.FARRIGHT.ordinal()));

		// RapidRight FarLeft NoChange
		assert (FzSets.AV.NOCHANGE.ordinal() == t.fireRule(
				FzSets.XD.RAPIDRIGHT.ordinal(), FzSets.X.FARLEFT.ordinal()));
		// RapidRight Left TurnRight
		assert (FzSets.AV.TURNRIGHT.ordinal() == t.fireRule(
				FzSets.XD.RAPIDRIGHT.ordinal(), FzSets.X.LEFT.ordinal()));
		// RapidRight Center BigTurnRight
		assert (FzSets.AV.BIGTURNRIGHT.ordinal() == t.fireRule(
				FzSets.XD.RAPIDRIGHT.ordinal(), FzSets.X.CENTER.ordinal()));
		// RapidRight Right BigTurnRight
		assert (FzSets.AV.BIGTURNRIGHT.ordinal() == t.fireRule(
				FzSets.XD.RAPIDRIGHT.ordinal(), FzSets.X.RIGHT.ordinal()));
		// RapidRight FarRight null
		assert (t.NULL == t.fireRule(FzSets.XD.RAPIDRIGHT.ordinal(),
				FzSets.X.FARRIGHT.ordinal()));

		assert (false) : "all tests passed!";
	}
}
