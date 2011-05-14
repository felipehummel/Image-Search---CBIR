package processing;

import java.util.Comparator;

public class ImageScore {
		public int id;
		public float score;
		
		
		public ImageScore(int _id, float _score) {
			id = _id;
			score = _score;
		}
		
		@Override
		public String toString() {
			return id + " : "+score;
		}

		public static final Comparator<ImageScore> DISTANCE_COMPARATOR = new Comparator<ImageScore>() {
			public int compare(ImageScore o1, ImageScore o2) {
				if (o1.score > o2.score) return +1;
				if (o1.score < o2.score) return -1;
				return 0;
		}};
		public static final Comparator<ImageScore> SIMILARITY_COMPARATOR = new Comparator<ImageScore>() {
				public int compare(ImageScore o1, ImageScore o2) {
					if (o1.score > o2.score) return -1;
					if (o1.score < o2.score) return +1;
					return 0;					
		}};
		
//		@Override
//		public int compareTo(ImageScore o) {
//			if (this.score > o.score) return -1;
//			if (this.score < o.score) return +1;
//			return 0;
//		}
}