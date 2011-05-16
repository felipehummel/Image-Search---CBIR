package similarities;

public class EuclidianDistanceSimilarity implements ImageSimilarity {
	
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
	final public byte getComparisonMeasure() {
		return ImageSimilarity.DISTANCE_COMPARISON;
	}
	
	@Override
	public String toString() {
		return "Euclidian Distance";
	}

	@Override
	final public boolean isDistanceMeasure() {
		return true;
	}
}
