package similarities;

import java.util.Collections;
import java.util.List;

import processing.ImageScore;


public class EuclidianDistanceSimilarity implements ImageSimilarity {
//	@Override
//	public final float calculateSimilarity(int[] a, int[] b) {
//		double distance = 0;
//	    for (int i = 0; i < a.length; i++) {
//	        distance = distance + Math.pow((a[i] - b[i]), 2);
//	    }
//	    return (float) Math.sqrt(distance);
//	}
	
	@Override
	public final float calculateSimilarity(int[] a, int[] b) {
		double distance = 0;
		int aux;
	    for (int i = 0; i < a.length; i++) {
	    	aux = a[i] - b[i];
	        distance = distance + (aux * aux);
	    }
	    return (float)Math.sqrt(distance);
	}

	@Override
	public void sortResults(List<ImageScore> scores) {
		Collections.sort(scores, ImageScore.DISTANCE_COMPARATOR);
	}
	
	@Override
	public boolean getComparisonMeasure() {
		return ImageSimilarity.DISTANCE_COMPARISON;
	}
	
	@Override
		public String toString() {
			return "Euclidian Distance";
		}
}
