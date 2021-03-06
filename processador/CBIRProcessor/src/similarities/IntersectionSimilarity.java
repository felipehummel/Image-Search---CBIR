package similarities;


public class IntersectionSimilarity implements ImageSimilarity {

	@Override
	public float calculateSimilarity(int[] a, int[] b) {
		// TODO Auto-generated method stub
		float similarity = 0F;
		float sum_bottom = 0F;
		for (int i = 0; i < b.length; i++) {
			if (a[i]>b[i])
				similarity = similarity + (float)a[i];
			else
				similarity = similarity + (float)b[i];
			sum_bottom = sum_bottom + (float)a[i];
		}
		similarity = (float)1 - (float)similarity/(float)sum_bottom;
		if (similarity < 0) 
			similarity *= -1;
		return similarity;
	}

	@Override
	final public byte getComparisonMeasure() {
		return ImageSimilarity.DISTANCE_COMPARISON;
	}
	
	@Override
	public String toString() {
		return "Histogram Intersection";
	}

	@Override
	final public boolean isDistanceMeasure() {
		return true;
	}

}
