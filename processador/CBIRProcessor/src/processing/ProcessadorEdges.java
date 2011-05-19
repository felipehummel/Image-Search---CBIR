package processing;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import similarities.DlogSimilarity;
import similarities.EuclidianDistanceSimilarity;
import similarities.ImageSimilarity;
import similarities.IntersectionSimilarity;
import similarities.VectorSpaceSimilarity;

public class ProcessadorEdges {
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException, ExecutionException {
		Processor proc = new Processor();
		Properties props = new Properties();
		props.load(new FileInputStream(args[0]));
		String edges_output_binary_histograms = props.getProperty("edges_binary_histograms_file", "/home/felipe/ufam/doutorado/ri/trab1_busca/arestas/output_binary_histograms");
		String edges_image_id_lookup_file = props.getProperty("edges_image_id_lookup_file", "/home/felipe/ufam/doutorado/ri/trab1_busca/arestas/file_lookup_file");
		
		proc.readEdgesData(edges_output_binary_histograms, edges_image_id_lookup_file);
		
		ImageSimilarity similarity = new DlogSimilarity();
//		similarity = new VectorSpaceSimilarity();
//		similarity = new EuclidianDistanceSimilarity();
//		similarity = new IntersectionSimilarity();
		
		int[] queries = new int[]{100, 200, 300, 5123, 1235};
		for (int i = 0; i < queries.length; i++) {
			ImageScore[] results = proc.parallelProcessQueryImageEdgesOnly(queries[i], similarity, 1);
			Arrays.toString(results);	
		}
		
		proc.shutDown();
	}
}
