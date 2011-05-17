package processing;

import java.util.Map.Entry;
import java.util.concurrent.Callable;

import processing.priorityqueue.DistanceHitQueue;
import processing.priorityqueue.HitQueue;
import processing.priorityqueue.SimilarityHitQueue;
import similarities.ImageSimilarity;

public class TwoEvidenceShardProcessor implements Callable<ImageScore[]>{
	private final int[] first_query_feature_array;
	private final int[] second_query_feature_array;
	private final Entry<Integer, int[]>[] first_feature_images_entries;
	private final Entry<Integer, int[]>[] second_feature_images_entries;
	private final int start;
	private final int end;
	private final ImageSimilarity similarity;
	public static float FIRST_FEATURE_WEIGHT = 0.9F;
	public static float SECOND_FEATURE_WEIGHT = 0F;
	/**
	 * start is inclusive and end is exclusive 
	 * @param _query_feature_array
	 * @param _keys
	 * @param _start
	 * @param _end
	 */
	public TwoEvidenceShardProcessor(int[] _first_query_feature_array, int[] _second_query_feature_array, 
			                         int _start, int _end, 
			                         Entry<Integer, int[]>[] _first_feature_images, 
			                         Entry<Integer, int[]>[] _second_feature_images,
			                         ImageSimilarity _similarity) {
		first_feature_images_entries = _first_feature_images;
		second_feature_images_entries = _second_feature_images;
		first_query_feature_array = _first_query_feature_array;
		second_query_feature_array = _second_query_feature_array;
		start = _start;
		end = _end;
		similarity = _similarity;
	}
	
	@Override
	public ImageScore[] call() throws Exception {
		int id;
		int[] first_feature_array, second_feature_array;
		float score, first_score, second_score;
		final HitQueue pq;
		if (similarity.getComparisonMeasure() == ImageSimilarity.DISTANCE_COMPARISON) 
			pq = new DistanceHitQueue(Processor.TOP_K);
		else
			pq = new SimilarityHitQueue(Processor.TOP_K);
		
        ImageScore pqTop = pq.top();
        boolean should_insert_in_pq = false;
		for (int i = start; i < end; i++) {
			id = first_feature_images_entries[i].getKey();
			first_feature_array = first_feature_images_entries[i].getValue();
			second_feature_array = second_feature_images_entries[i].getValue();
			first_score = similarity.calculateSimilarity(first_feature_array, first_query_feature_array);
			second_score = similarity.calculateSimilarity(second_feature_array, second_query_feature_array);
			//COMBINACAO DE SCORES
			score = FIRST_FEATURE_WEIGHT*first_score + SECOND_FEATURE_WEIGHT*second_score;
			if (similarity.isDistanceMeasure()) //distancia
				should_insert_in_pq = score < pqTop.score;
			else
				should_insert_in_pq = score > pqTop.score;
			if (should_insert_in_pq) {
        		pqTop.id = id;
                pqTop.score = score;
                pqTop = pq.updateTop();
			}
		}
		return pq.getResults(Processor.TOP_K);
	}
}
