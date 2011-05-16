package similarities;

import java.util.Collections;
import java.util.List;

import processing.ImageScore;

public class VectorSpaceSimilarity implements ImageSimilarity{

	@Override
	public float calculateSimilarity(int[] a, int[] b) {
		long similarity = 0L;
		long norm_a = 0L;
		long norm_b = 0L;
		for (int i = 0; i < b.length; i++) {
			similarity = similarity + a[i] * b[i];
			norm_a = norm_a + (long)a[i] * (long)a[i];
			norm_b = norm_b + (long)b[i] * (long)b[i];
		}
		return (float) (similarity/(Math.sqrt(norm_a)*Math.sqrt(norm_b)));
	}

	@Override
	public void sortResults(List<ImageScore> scores) {
		Collections.sort(scores, ImageScore.SIMILARITY_COMPARATOR);
	}

	@Override
	public boolean getComparisonMeasure() {
		return ImageSimilarity.SIMILARITY_COMPARISON;
	}
}
