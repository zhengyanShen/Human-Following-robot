package l5;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Fuzzy control rule
 *
 */
public abstract class FzRule {
	protected int NULL = -1;

	protected abstract int fireRule(int xIndex, int yIndex);

	/**
	 * fire the rules according the rule table
	 * @param elesx the table x array
	 * @param elesy the table y array
	 * @return all the fired rules
	 */
	public Map<Integer, FzElement> fire(FzElement[] elesx, FzElement[] elesy) {
		Map<Integer, FzElement> angleVelocitys = new HashMap<Integer, FzElement>();
		for (FzElement xEle : elesx) {
			for (FzElement yEle : elesy) {
				int ruleIndex = fireRule(xEle.getIndex(), yEle.getIndex());
				if (ruleIndex != NULL) {
					// calculate the result degree by AND
					double degree = FzElement.and(xEle.getDegree(),
							yEle.getDegree());
					FzElement e = angleVelocitys.get(ruleIndex);
					if (e == null) {
						// the result does not exist, create and add it
						e = new FzElement(ruleIndex, degree);
						angleVelocitys.put(ruleIndex, e);
					} else {
						// the result already exist, need aggregate it by OR
						e.setDegree(FzElement.or(e.getDegree(), degree));
					}
				}
			}
		}
		return angleVelocitys;
	}

	// unit test
	public static void main(String[] args) {
		FzRule t = new FzAVRule();
		// XD: RapidLeft 0.3 Left 0.7
		// X: Left 0.5 Center 0.6
		// AVResult: BigTurnLeft 0.5 TurnLeft 0.6
		FzElement[] elesx = {
				new FzElement(FzSets.XD.RAPIDLEFT.ordinal(), 0.3),
				new FzElement(FzSets.XD.LEFT.ordinal(), 0.7) };
		FzElement[] elesy = {
				new FzElement(FzSets.X.LEFT.ordinal(), 0.5),
				new FzElement(FzSets.X.CENTER.ordinal(), 0.6)};
		Map<Integer, FzElement> result = t.fire(elesx, elesy);
		assert (result.size() == 2);
		FzElement bigTurnleftEle = result.get(FzSets.AV.BIGTURNLEFT.ordinal());
		FzElement turnleftEle = result.get(FzSets.AV.TURNLEFT.ordinal());
		assert (bigTurnleftEle != null);
		assert (bigTurnleftEle.getDegree() == 0.5);
		assert (bigTurnleftEle.getIndex() == FzSets.AV.BIGTURNLEFT.ordinal());
		assert (turnleftEle != null);
		assert (turnleftEle.getDegree() == 0.6);
		assert (turnleftEle.getIndex() == FzSets.AV.TURNLEFT.ordinal());
		
		// XD: RapidLeft 0.7 Left 0.2
		// X: FarLeft 0.5
		// AVResult: null
		FzElement[] elesx1 = {
				new FzElement(FzSets.XD.RAPIDLEFT.ordinal(), 0.7),
				new FzElement(FzSets.XD.LEFT.ordinal(), 0.2) };
		FzElement[] elesy1 = {
				new FzElement(FzSets.X.FARLEFT.ordinal(), 0.5)};
		Map<Integer, FzElement> result1 = t.fire(elesx1, elesy1);
		assert (result1.size() == 0);
		
		assert (false) : "all tests passed!";
	}
}
