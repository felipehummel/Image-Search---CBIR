package processing;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import similarities.DlogSimilarity;
import similarities.ImageSimilarity;
import util.TextFile;

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
		TextFile queries_file = new TextFile("queries.txt");
		ArrayList<Integer> queries_ids = new ArrayList<Integer>();
		for (String str : queries_file) {
			queries_ids.add(Integer.parseInt(str));
		}
		for (int i = 0; i < queries_ids.size(); i++) {
			ImageScore[] results = proc.parallelProcessQueryImageRoupas(queries_ids.size(), similarity, 1);
			Arrays.toString(results);	
		}
		
		proc.shutDown();
	}
}
