package processing;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import similarities.ImageSimilarity;

public class ImageScore {
		public int id;
		public float score;
		
		
		public ImageScore(int _id, float _score) {
			id = _id;
			score = _score;
		}
		
		@Override
		public String toString() {
			return id + " : "+score;
		}
		
		public static void sort(List<ImageScore> image_scores, ImageSimilarity similarity) {
			if (similarity.getComparisonMeasure() == ImageSimilarity.DISTANCE_COMPARISON) 
				Collections.sort(image_scores, DISTANCE_COMPARATOR);
			else  //similarity
				Collections.sort(image_scores, SIMILARITY_COMPARATOR);
		}
		public static void sort(ImageScore[] image_scores, ImageSimilarity similarity) {
			if (similarity.getComparisonMeasure() == ImageSimilarity.DISTANCE_COMPARISON) 
				Arrays.sort(image_scores, DISTANCE_COMPARATOR);
			else  //similarity
				Arrays.sort(image_scores, SIMILARITY_COMPARATOR);
		}

		private static final Comparator<ImageScore> DISTANCE_COMPARATOR = new Comparator<ImageScore>() {
			public int compare(ImageScore o1, ImageScore o2) {
				if (o1.score > o2.score) return +1;
				if (o1.score < o2.score) return -1;
				return 0;
		}};
		private static final Comparator<ImageScore> SIMILARITY_COMPARATOR = new Comparator<ImageScore>() {
				public int compare(ImageScore o1, ImageScore o2) {
					if (o1.score > o2.score) return -1;
					if (o1.score < o2.score) return +1;
					return 0;					
		}};
		
}