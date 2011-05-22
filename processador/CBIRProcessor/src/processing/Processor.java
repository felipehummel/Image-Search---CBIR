package processing;

import static java.lang.System.out;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
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
import evaluation.Precision;

public class Processor {
	public static final int LCH_HISTOGRAM_SIZE = 64*5*5;
	public static final int EDGES_HISTOGRAM_SIZE = 4*5*5;
	public static final int TOP_K = 30;
	private final HashMap<Integer, int[]> lch_images = new HashMap<Integer, int[]>(104000);
	private Entry<Integer, int[]>[] lch_image_entries;
	private final HashMap<Integer, int[]> edges_images = new HashMap<Integer, int[]>(104000);
	private Entry<Integer, int[]>[] edges_image_entries;
	
	private final ExecutorService executor = Executors.newFixedThreadPool(24);
	
	public Entry<Integer, int[]> getLCHImageEntry(int image_entries_positions) {
		return lch_image_entries[image_entries_positions];
	}
	
	public Entry<Integer, int[]> getEdgesImageEntry(int image_entries_positions) {
		return edges_image_entries[image_entries_positions];
	}
	
	public int size() {
		return lch_image_entries.length;
	}
	
	@SuppressWarnings("unchecked")
	public void readLCHData(String binary_histograms_file, String image_id_lookup_file) throws IOException {
        final FileInputStream inFile = new FileInputStream(binary_histograms_file);
        final TextFile names_file = new TextFile(image_id_lookup_file);
        Iterator<String> names_iterator = names_file.iterator();
        FileChannel inChannel = inFile.getChannel();
        ByteBuffer buf = ByteBuffer.allocate(LCH_HISTOGRAM_SIZE*4); //*4 pq cada int tem 4 bytes
        int[] ints;
        String name;
        int i = 0;
	    while (inChannel.read(buf) != -1) {
	    	ints = new int[LCH_HISTOGRAM_SIZE];
	    	((ByteBuffer) (buf.flip())).asIntBuffer().get(ints);
	        buf.clear();
	        name = names_iterator.next();
	        lch_images.put(Integer.parseInt(name), ints);
	        if (i++%10000==0)
	        	System.out.println(i);
        }
        inFile.close();
        lch_image_entries = (Entry<Integer, int[]>[]) new Entry[lch_images.size()]; 
    	i = 0;
    	for (Entry<Integer, int[]> entry : lch_images.entrySet()) {
    		lch_image_entries[i] = entry;
			i++;
		}
        out.println(lch_images.size()+  " imagens");
	}
	
	@SuppressWarnings("unchecked")
	public void readEdgesData(String binary_histograms_file, String image_id_lookup_file) throws IOException {
        final FileInputStream inFile = new FileInputStream(binary_histograms_file);
        final TextFile names_file = new TextFile(image_id_lookup_file);
        Iterator<String> names_iterator = names_file.iterator();
        FileChannel inChannel = inFile.getChannel();
        ByteBuffer buf = ByteBuffer.allocate(EDGES_HISTOGRAM_SIZE*4); //*4 pq cada int tem 4 bytes
        int[] ints;
        String name;
        int i = 0;
	    while (inChannel.read(buf) != -1) {
	    	ints = new int[EDGES_HISTOGRAM_SIZE];
	    	((ByteBuffer) (buf.flip())).asIntBuffer().get(ints);
	        buf.clear();
	        name = names_iterator.next();
	        edges_images.put(Integer.parseInt(name), ints);
	        if (i++%10000==0)
	        	System.out.println(i);
        }
        inFile.close();
        edges_image_entries = (Entry<Integer, int[]>[]) new Entry[edges_images.size()]; 
    	i = 0;
    	for (Entry<Integer, int[]> entry : edges_images.entrySet()) {
    		edges_image_entries[i] = entry;
			i++;
		}
        out.println(edges_images.size()+  " imagens");
	}
	
	public ImageScore[] parallelProcessQueryImageRoupas(int query_image, ImageSimilarity similarity, int parallel_rate) throws InterruptedException, ExecutionException {
		Future<ImageScore[]>[] futures = (Future<ImageScore[]>[]) new Future[parallel_rate];
		int num_images = edges_images.size();
		int shard_size = (int)Math.ceil((double)num_images / (double)parallel_rate);
		int lower = 0;
		int upper;
		ShardQueryProcessor shard;
		for (int i = 0; i < parallel_rate; i++) {
			if (shard_size + lower >= num_images)
				upper = num_images;
			else
				upper = shard_size + lower;
			shard = new ShardQueryProcessor(edges_images.get(query_image), lower, upper, edges_image_entries, similarity);
			futures[i] = executor.submit(shard);
			lower += shard_size;
		}
		if (parallel_rate == 1)
			return futures[0].get();
		
		ArrayList<ImageScore> scores = new ArrayList<ImageScore>(100);
		for (int i = 0; i < parallel_rate; i++) {
			ImageScore[] x = futures[i].get();
			for (int j = 0; j < x.length; j++) 
				scores.add(x[j]);
		}
		ImageScore.sort(scores, similarity);
		return scores.toArray(new ImageScore[scores.size()]);	
	}
	
	public ImageScore[] parallelProcessQueryImage(int query_image, ImageSimilarity similarity, int parallel_rate) throws InterruptedException, ExecutionException {
		@SuppressWarnings("unchecked")
		Future<ImageScore[]>[] futures = (Future<ImageScore[]>[]) new Future[parallel_rate];
		int num_images = lch_images.size();
		int shard_size = (int)Math.ceil((double)num_images / (double)parallel_rate);
		int lower = 0;
		int upper;
		ShardQueryProcessor shard;
		for (int i = 0; i < parallel_rate; i++) {
			if (shard_size + lower >= num_images)
				upper = num_images;
			else
				upper = shard_size + lower;
//			TwoEvidenceShardProcessor shard = new TwoEvidenceShardProcessor(lch_images.get(query_image), edges_images.get(query_image), lower, upper, 
//					                                                        lch_image_entries, edges_image_entries, similarity);
			shard = new ShardQueryProcessor(lch_images.get(query_image), lower, upper, lch_image_entries, similarity);
			futures[i] = executor.submit(shard);
			lower += shard_size;
		}
		if (parallel_rate == 1)
			return futures[0].get();
		
		ArrayList<ImageScore> scores = new ArrayList<ImageScore>(100);
		for (int i = 0; i < parallel_rate; i++) {
			ImageScore[] x = futures[i].get();
			for (int j = 0; j < x.length; j++) 
				scores.add(x[j]);
		}
		ImageScore.sort(scores, similarity);
		return scores.toArray(new ImageScore[scores.size()]);
	}
	
	public ImageScore[] processQueryImage(int query_image, ImageSimilarity similarity) {
		int id;
		int[] feature_array;
		int[] query_feature_array = lch_images.get(query_image);
		float score;
		final HitQueue pq;
		if (similarity.getComparisonMeasure() == ImageSimilarity.DISTANCE_COMPARISON) 
			pq = new DistanceHitQueue(Processor.TOP_K);
		else
			pq = new SimilarityHitQueue(Processor.TOP_K);
        ImageScore pqTop = pq.top();	
        boolean should_insert_in_pq;
		for (int i = 0; i < lch_image_entries.length; i++) {
			id = lch_image_entries[i].getKey();
			feature_array = lch_image_entries[i].getValue();
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
			ImageScore[] results = this.parallelProcessQueryImage(evaluated_query.query_id, similarity, parallel_rate);
			evaluations[i] = getRelevanceArray(evaluated_query, results);
			i++;
		}
		float metric_result = metric.calculate(evaluations, num_relevants);
		out.println("["+similarity.toString()+"]"+metric.resultToString(metric_result));
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
		Properties props = new Properties();
		props.load(new FileInputStream(args[0]));
		String output_binary_histograms = props.getProperty("binary_histograms_file", "/home/felipe/ufam/doutorado/ri/trab1_busca/output_binary_histograms");
		String image_id_lookup_file = props.getProperty("image_id_lookup_file", "/home/felipe/ufam/doutorado/ri/trab1_busca/file_lookup_file");
		String edges_output_binary_histograms = props.getProperty("edges_binary_histograms_file", "/home/felipe/ufam/doutorado/ri/trab1_busca/arestas/output_binary_histograms");
		String edges_image_id_lookup_file = props.getProperty("edges_image_id_lookup_file", "/home/felipe/ufam/doutorado/ri/trab1_busca/arestas/file_lookup_file");
		proc.readLCHData(output_binary_histograms, image_id_lookup_file);
		proc.readEdgesData(edges_output_binary_histograms, edges_image_id_lookup_file);
		
		final int parallel_rate = 2;
		TwoEvidenceShardProcessor.FIRST_FEATURE_WEIGHT = 0F;
		TwoEvidenceShardProcessor.SECOND_FEATURE_WEIGHT = 1F;
		long before = System.currentTimeMillis();
		
		
		long after = System.currentTimeMillis();
		System.out.println("Avaliação demorou: "+(after-before)+ " ms");
		proc.shutDown();
	}
}

