package similarities;


public interface ImageSimilarity {
	public static byte DISTANCE_COMPARISON = 1;
    public static byte SIMILARITY_COMPARISON = 0;
    
	float calculateSimilarity(int[] a, int[] b);
	byte getComparisonMeasure();
	boolean isDistanceMeasure();
}
