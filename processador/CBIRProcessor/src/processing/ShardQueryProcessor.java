package processing;
import java.util.Map.Entry;
import java.util.concurrent.Callable;


import similarities.ImageSimilarity;


public class ShardQueryProcessor implements Callable<ImageScore[]>{
	private final int[] query_feature_array;
	private final Entry<Integer, int[]>[] images_entries;
	private final int start;
	private final int end;
	private final ImageSimilarity similarity;
	/**
	 * start is inclusive and end is exclusive 
	 * @param _query_feature_array
	 * @param _keys
	 * @param _start
	 * @param _end
	 */
	public ShardQueryProcessor(int[] _query_feature_array, int _start, int _end, Entry<Integer, int[]>[] _images, ImageSimilarity _similarity) {
		images_entries = _images;
		query_feature_array = _query_feature_array;
		start = _start;
		end = _end;
		similarity = _similarity;
	}
	
	@Override
	public ImageScore[] call() throws Exception {
		int id;
		int[] feature_array;
		float score;
		final HitQueue pq = new HitQueue(Processor.TOP_K, true, similarity.getComparisonMeasure());
        ImageScore pqTop = pq.top();
		for (int i = start; i < end; i++) {
			id = images_entries[i].getKey();
			feature_array = images_entries[i].getValue();
			score = similarity.calculateSimilarity(feature_array, query_feature_array);
//			System.out.println(id+ ":"+score);
			if (similarity.getComparisonMeasure()) {//distancia
				if (score < pqTop.score) {
	        		pqTop.id = id;
	                pqTop.score = score;
	                pqTop = pq.updateTop();
				}
			}
			else {
				if (score > pqTop.score) {
	        		pqTop.id = id;
	                pqTop.score = score;
	                pqTop = pq.updateTop();
				}
			}
		}
		return Processor.getResults(pq);
	}

}
