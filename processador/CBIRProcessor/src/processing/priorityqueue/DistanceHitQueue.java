package processing.priorityqueue;

/**
 * Priority Queue to use with distance-based metrics/measures/methods
 * @author felipe
 *
 */
public class DistanceHitQueue extends HitQueue {
	
	public DistanceHitQueue(int size) {
		super(size, true, true);
	}

}
