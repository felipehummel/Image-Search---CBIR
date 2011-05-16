package evaluation;

public class MeanAveragePrecision implements Metric {
	public static final int MAP_N_LIMIT_RETURNED_DOCS = 30;
	@Override
	public float calculate(boolean[][] evaluations, int[] num_relevants) {
		float mean_precision = 0F;
		int j = 0;
		for (boolean[] query_relevants : evaluations) {
			float current_precision = 0F;
			int correct = 0;
			//Começa de 1 pra desconsiderar a primeira posicao que é a propria imagem
			for (int i = 1; i < query_relevants.length && i < MAP_N_LIMIT_RETURNED_DOCS; i++) {
				if (query_relevants[i]) {
					correct++;
					current_precision += ((float)correct/(float)i);
				}
			}
			mean_precision += (current_precision/(float)num_relevants[j]);
			j++;
		}
		return mean_precision / evaluations.length;
	}

	@Override
	public String resultToString(float result) {
		return "MAP:"+(result*100)+"%";
	}

}
