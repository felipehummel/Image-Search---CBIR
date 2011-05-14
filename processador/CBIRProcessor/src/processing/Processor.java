package processing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import similarities.DlogSimilarity;
import similarities.EuclidianDistanceSimilarity;
import similarities.ImageSimilarity;
import similarities.IntersectionSimilarity;
import similarities.VectorSpaceSimilarity;
import util.TextFile;
import evaluation.Metric;
import evaluation.Precision;

public class Processor {
	public static final int HISTOGRAM_SIZE = 64*5*5;
	private final HashMap<Integer, int[]> images = new HashMap<Integer, int[]>(104000);
	private Entry<Integer, int[]>[] image_entries;
	public static final int TOP_K = 20;
	private final ExecutorService executor = Executors.newFixedThreadPool(3);
	
	@SuppressWarnings("unchecked")
	public void readData() throws IOException {
        FileInputStream inFile = null;
        inFile = new FileInputStream("/home/felipe/ufam/doutorado/ri/trab1_busca/output_binary_histograms");
        TextFile names_file = new TextFile("/home/felipe/ufam/doutorado/ri/trab1_busca/file_lookup_file");
        Iterator<String> names_iterator = names_file.iterator();
        FileChannel inChannel = inFile.getChannel();
        int[] ints;
        ByteBuffer buf = ByteBuffer.allocate(HISTOGRAM_SIZE*4);
        String name;
        int i = 0;
	    while (inChannel.read(buf) != -1) {
	    	ints = new int[HISTOGRAM_SIZE];
	    	((ByteBuffer) (buf.flip())).asIntBuffer().get(ints);
	        buf.clear();
	        name = names_iterator.next();
	        images.put(Integer.parseInt(name), ints);
	        if (i++%10000==0)
	        	System.out.println(i);
        }
        inFile.close();
        image_entries = (Entry<Integer, int[]>[]) new Entry[images.size()]; 
    	i = 0;
    	for (Entry<Integer, int[]> entry : images.entrySet()) {
    		image_entries[i] = entry;
			i++;
		}
        System.out.println(images.size()+  " imagens");
	}
	public ImageScore[] paralellProcessQueryImage(int query_image, ImageSimilarity similarity, int parallel_rate) throws InterruptedException, ExecutionException {
		ArrayList<Future<ImageScore[]>> futures = new ArrayList<Future<ImageScore[]>>(parallel_rate);
		int num_images = images.size();
		int shard_size = (int)Math.ceil((double)num_images / (double)parallel_rate);
		int lower = 0;
		int upper; 
		for (int i = 0; i < parallel_rate; i++) {
			if (shard_size + lower >= num_images)
				upper = num_images;
			else
				upper = shard_size + lower;
//			System.out.println(i+ " = ["+lower+", "+upper+"]");
			ShardProcessor shard = new ShardProcessor(images.get(query_image), lower, upper, image_entries, similarity);
			Future<ImageScore[]> result = executor.submit(shard);
			futures.add(result);
			lower += shard_size;
		}
		if (parallel_rate == 1)
			return futures.get(0).get();
		ArrayList<ImageScore> scores = new ArrayList<ImageScore>(100);
		for (int i = 0; i < parallel_rate; i++) 
			scores.addAll(Arrays.asList(futures.get(i).get()));
		
		similarity.sortResults(scores);
//		Collections.sort(scores);
		return scores.toArray(new ImageScore[scores.size()]);
	}
	
	public ImageScore[] processQueryImage(int query_image, ImageSimilarity similarity) {
		int id;
		int[] feature_array;
		int[] query_feature_array = images.get(query_image);
		float score;
		final HitQueue pq = new HitQueue(TOP_K, true, similarity.getComparisonMeasure());
        ImageScore pqTop = pq.top();
		for (int i = 0; i < image_entries.length; i++) {
			id = image_entries[i].getKey();
			feature_array = image_entries[i].getValue();
			score = similarity.calculateSimilarity(feature_array, query_feature_array);
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
	
	
	public static ImageScore[] getResults(HitQueue x) {
		// In case pq was populated with sentinel values, there might be less
        // results than pq.size(). Therefore return all results until either
        // pq.size() or totalHits.
        //totalHits é igual a top_k pq a gente tem certeza que sempre vai ter top_K resultados no heap, ou seja não tem como sobrer objeto sentinel
        int totalHits = TOP_K; 
        int size = totalHits < x.size() ? totalHits : x.size();
        
        // We know that start < pqsize, so just fix howMany. 
        int howMany = Math.min(size, TOP_K);
        ImageScore[] results = new ImageScore[howMany];
        for (int i = howMany - 1; i >= 0; i--) {
            results[i] = x.pop();
        }
        return results;
	}
	
	private void shutDown() {
		executor.shutdownNow();
	}
	
	private void evaluateQueries(String relevants_dir, ImageSimilarity similarity, Metric metric, int parallel_rate) throws InterruptedException, ExecutionException {
		File relDir = new File(relevants_dir);
		boolean[][] evaluations = new boolean[relDir.list().length][];
		int i = 0;
		for (String query_file : relDir.list()) {
			EvaluatedQuery evaluated_query = parseEvaluatedQuery(relevants_dir+"/"+query_file);
			System.out.println("Querying image_id: "+evaluated_query.query_id);
			ImageScore[] results = this.paralellProcessQueryImage(evaluated_query.query_id, similarity, parallel_rate);
			System.out.println(evaluated_query.relevants);
			System.out.println(Arrays.toString(results));
			boolean[] relevance_array = getRelevanceArray(evaluated_query, results);
			evaluations[i] = relevance_array;
			i++;
		}
		float metric_result = metric.calculate(evaluations);
		System.out.println(metric.resultToString(metric_result));
	}
	
	private boolean[] getRelevanceArray(EvaluatedQuery evaluated_query,	ImageScore[] results) {
		boolean[] relevance_array = new boolean[results.length];
		for (int i = 0; i < results.length; i++) {
			if (evaluated_query.isImageRelevant(results[i].id)) 
				relevance_array[i] = true;
			else 
				relevance_array[i] = false;	
		}
		return relevance_array;
	}
	private EvaluatedQuery parseEvaluatedQuery(String query_file_path) {
		TextFile query_text_file = new TextFile(query_file_path);
		Iterator<String> file_it = query_text_file.iterator();
		int query_id = Integer.parseInt(file_it.next());
		Set<Integer> relevants = new HashSet<Integer>();
		while(file_it.hasNext()) 
			relevants.add(Integer.parseInt(file_it.next()));
		return new EvaluatedQuery(query_id, relevants);
	}

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		Processor proc = new Processor();
		proc.readData();
		final int parallel_rate = 2;
		ImageSimilarity similarity = new VectorSpaceSimilarity();
		Metric metric = new Precision(10);
		long before = System.currentTimeMillis();
		proc.evaluateQueries("relevantes", similarity, metric, parallel_rate);
		long after = System.currentTimeMillis();
		System.out.println("Avaliação demorou: "+(after-before)+ " ms");
		
		proc.shutDown();
//			long before = System.currentTimeMillis();
//			ImageScore[] results = proc.paralellProcessQueryImage(1234, new EuclidianDistanceSimilarity(), 2);
//			results = proc.paralellProcessQueryImage(1234, new IntersectionSimilarity(), 2);
//			results = proc.paralellProcessQueryImage(1234, new VectorSpaceSimilarity(), 2);
//			results = proc.paralellProcessQueryImage(1234, new DlogSimilarity(), 2);
//			long after = System.currentTimeMillis();
//			System.out.println((after-before)+ " ms");
//			System.out.println(Arrays.toString(results));
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} catch (ExecutionException e) {
//			e.printStackTrace();
//		}
//		finally {
			
//		}
	}
	
	private static class EvaluatedQuery {
		public final int query_id;
		public final Set<Integer> relevants;
		
		public EvaluatedQuery(int _query_id, Set<Integer> _relevants) {
			relevants = _relevants;
			query_id = _query_id;
		}

		public boolean isImageRelevant(int id) {
			return relevants.contains(id);
		}
		
	}
	
}