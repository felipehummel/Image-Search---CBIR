package evaluation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import processing.Processor;
import similarities.DlogSimilarity;
import similarities.ImageSimilarity;

public class SpeedBenchmark {
	public static void main(String[] args) {
		Processor proc = new Processor();
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(args[0]));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		String output_binary_histograms = props.getProperty("binary_histograms_file", "/home/felipe/ufam/doutorado/ri/trab1_busca/output_binary_histograms");
		String image_id_lookup_file = props.getProperty("image_id_lookup_file", "/home/felipe/ufam/doutorado/ri/trab1_busca/file_lookup_file");
//		int parallel_rate = Integer.parseInt(props.getProperty("parallel_rate"));
		int num_queries = Integer.parseInt(props.getProperty("num_queries_speed_test", "50"));
		try {
			proc.readLCHData(output_binary_histograms, image_id_lookup_file);
			Random rand = new Random();
			ImageSimilarity similarity = new DlogSimilarity();
			int dataset_size = proc.size();
			long accumulated_time = 0;
			long before, after;
			for (int parallel_rate = 1; parallel_rate <= 24; parallel_rate++) {
				for (int i = 0; i < num_queries; i++) {
					Entry<Integer, int[]> entry = proc.getLCHImageEntry(rand.nextInt(dataset_size));
					int query_id = entry.getKey();
					before = System.currentTimeMillis();
					proc.parallelProcessQueryImage(query_id, similarity, parallel_rate);
					after = System.currentTimeMillis();
					accumulated_time += (after-before);
				}
				System.out.println("["+parallel_rate+" threads] Média de tempo por consulta: "+(accumulated_time/num_queries));
				accumulated_time = 0;
			}
			proc.shutDown();
		} catch (IOException e) {
			System.exit(0);
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (ExecutionException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		
	}
}
