package processing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import processing.priorityqueue.DistanceHitQueue;
import processing.priorityqueue.HitQueue;
import processing.priorityqueue.SimilarityHitQueue;
import similarities.DlogSimilarity;
import similarities.EuclidianDistanceSimilarity;
import similarities.ImageSimilarity;
import similarities.IntersectionSimilarity;
import similarities.VectorSpaceSimilarity;
import util.TextFile;
import evaluation.EvaluatedQuery;
import evaluation.MeanAveragePrecision;
import evaluation.Metric;

public class Processor {
	public static final int HISTOGRAM_SIZE = 64*5*5;
	public static final int TOP_K = 30;
	private final HashMap<Integer, int[]> images = new HashMap<Integer, int[]>(104000);
	private Entry<Integer, int[]>[] image_entries;
	private final ExecutorService executor = Executors.newFixedThreadPool(3);
	
	@SuppressWarnings("unchecked")
	public void readData(String binary_histograms_file, String image_id_lookup_file) throws IOException {
        final FileInputStream inFile = new FileInputStream(binary_histograms_file);
        final TextFile names_file = new TextFile(image_id_lookup_file);
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
			ShardQueryProcessor shard = new ShardQueryProcessor(images.get(query_image), lower, upper, image_entries, similarity);
			Future<ImageScore[]> result = executor.submit(shard);
			futures.add(result);
			lower += shard_size;
		}
		if (parallel_rate == 1)
			return futures.get(0).get();
		ArrayList<ImageScore> scores = new ArrayList<ImageScore>(100);
		for (int i = 0; i < parallel_rate; i++) 
			scores.addAll(Arrays.asList(futures.get(i).get()));
		
		ImageScore.sort(scores, similarity);
		return scores.toArray(new ImageScore[scores.size()]);
	}
	
	public ImageScore[] processQueryImage(int query_image, ImageSimilarity similarity) {
		int id;
		int[] feature_array;
		int[] query_feature_array = images.get(query_image);
		float score;
		final HitQueue pq;
		if (similarity.getComparisonMeasure() == ImageSimilarity.DISTANCE_COMPARISON) 
			pq = new DistanceHitQueue(Processor.TOP_K);
		else
			pq = new SimilarityHitQueue(Processor.TOP_K);
        ImageScore pqTop = pq.top();
        boolean should_insert_in_pq;
		for (int i = 0; i < image_entries.length; i++) {
			id = image_entries[i].getKey();
			feature_array = image_entries[i].getValue();
			score = similarity.calculateSimilarity(feature_array, query_feature_array);
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
		return pq.getResults(TOP_K);
	}
	
	public void shutDown() {
		executor.shutdownNow();
	}
	
	private void evaluateQueries(String relevants_dir, ImageSimilarity similarity, Metric metric, int parallel_rate) throws InterruptedException, ExecutionException {
		File relDir = new File(relevants_dir);
		boolean[][] evaluations = new boolean[relDir.list().length][];
		int[] num_relevants = new int[relDir.list().length];
		int i = 0;
		for (String query_file : relDir.list()) {
			EvaluatedQuery evaluated_query = parseEvaluatedQuery(relevants_dir+"/"+query_file);
			num_relevants[i] = evaluated_query.numberOfRelevants();
			ImageScore[] results = this.paralellProcessQueryImage(evaluated_query.query_id, similarity, parallel_rate);
//			System.out.println("Querying image_id: "+evaluated_query.query_id);
//			System.out.println(evaluated_query.relevants);
//			System.out.println(Arrays.toString(results));
			evaluations[i] = getRelevanceArray(evaluated_query, results);;
			i++;
		}
		float metric_result = metric.calculate(evaluations, num_relevants);
		System.out.println("["+similarity.toString()+"]"+metric.resultToString(metric_result));
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
		proc.readData("/home/felipe/ufam/doutorado/ri/trab1_busca/output_binary_histograms", 
				      "/home/felipe/ufam/doutorado/ri/trab1_busca/file_lookup_file");
		final int parallel_rate = 2;
		Metric metric = new MeanAveragePrecision();
		long before = System.currentTimeMillis();
		proc.evaluateQueries("relevantes", new VectorSpaceSimilarity(), metric, parallel_rate);
		proc.evaluateQueries("relevantes", new DlogSimilarity(), metric, parallel_rate);
		proc.evaluateQueries("relevantes", new EuclidianDistanceSimilarity(), metric, parallel_rate);
		proc.evaluateQueries("relevantes", new IntersectionSimilarity(), metric, parallel_rate);
		
		long after = System.currentTimeMillis();
		System.out.println("Avaliação demorou: "+(after-before)+ " ms");
		proc.shutDown();
	}
}
