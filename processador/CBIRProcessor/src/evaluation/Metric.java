package evaluation;

public interface Metric {
	float calculate(boolean[][] evaluations, int[] num_relevants);
	String resultToString(float result);
}
