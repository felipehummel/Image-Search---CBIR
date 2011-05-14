package evaluation;

public class Precision implements Metric {
	public final int at;
	public Precision(int _at) {
		at = _at;
	}
	@Override
	public float calculate(boolean[][] evaluations) {
		float precision = 0F;
		for (int i = 0; i < evaluations.length; i++) {
			int corrects = countCorrects(evaluations[i], at);
			precision += (float)corrects/at;
		}
		
		return precision/(float)evaluations.length;
	}
	private int countCorrects(boolean[] bs, int until) {
		int count = 0;
		//começa de 1 por que a primeira resposta é sempre a propria imagem de consulta
		//por isso soma mais 1 no until pra ser P@AT+1
		for (int i = 1; i < bs.length && i < until+1; i++) 
			if (bs[i])
				count++;
		
		return count;
	}
	@Override
	public String resultToString(float result) {
		return "Precision@"+at+": "+(result*100)+"%";
	}

}
