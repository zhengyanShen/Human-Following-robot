package l5;

/**
 * Fuzzy normalizer, map the value to/from [-1, 1] or [0, 1].
 * If the mapping destination is [-1, 1], the min and max should be
 * symmetric.
 *
 */
public class FzNormalizer {
	private double scale;
	private double min;

	public FzNormalizer(double min, double max) {
		if (min < 0) {
			scale = Math.abs(min);
		} else {
			scale = Math.abs(max - min);
		}
		this.min = min;
	}

	public double normalize(double v) {
		if (min > 0) {
			if (v < min) {
				return 0;
			} else {
				return (v - min) / scale;
			}
		}
		return v / scale;
	}

	public double deNormalize(double v) {
		if (min > 0) {
			return min + v * scale;
		}
		return v * scale;
	}
}
