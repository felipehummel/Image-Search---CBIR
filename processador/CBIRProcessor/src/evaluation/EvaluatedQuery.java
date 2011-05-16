package evaluation;

import java.util.Set;

public class EvaluatedQuery {
	public final int query_id;
	public final Set<Integer> relevants;
	
	public EvaluatedQuery(int _query_id, Set<Integer> _relevants) {
		relevants = _relevants;
		query_id = _query_id;
	}

	public int numberOfRelevants() {
		return relevants.size();
	}

	public boolean isImageRelevant(int id) {
		return relevants.contains(id);
	}
	
}

