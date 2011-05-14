package evaluation;

public interface Metric {
	float calculate(boolean[][] evaluations);
	String resultToString(float result);
}
