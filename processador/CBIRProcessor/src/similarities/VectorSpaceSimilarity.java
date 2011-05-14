package similarities;

import java.util.Collections;
import java.util.List;

import processing.ImageScore;

public class VectorSpaceSimilarity implements ImageSimilarity{

	@Override
	public float calculateSimilarity(int[] a, int[] b) {
		int similarity = 0;
		int norm_a = 0;
		int norm_b = 0;
		for (int i = 0; i < b.length; i++) {
			similarity = similarity + a[i] * b[i];
			norm_a = norm_a + a[i]*a[i];
			norm_b = norm_b + b[i]*b[i];
		}
		return (float) ((float)similarity/(float)(Math.sqrt(norm_a)*Math.sqrt(norm_b)));
		
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
