package similarities;

import java.util.List;

import processing.ImageScore;

public interface ImageSimilarity {
	public static boolean DISTANCE_COMPARISON = true;
    public static boolean SIMILARITY_COMPARISON = false;
    
	float calculateSimilarity(int[] a, int[] b);
	void sortResults(List<ImageScore> scores);
	boolean getComparisonMeasure(); 
}
