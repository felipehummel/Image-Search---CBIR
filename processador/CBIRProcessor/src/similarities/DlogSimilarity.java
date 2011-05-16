package similarities;

public class DlogSimilarity implements ImageSimilarity {

	private int computeLog(int value){
		//calcula o Log ja como manda o DLog
		int result = 0;
		if(value==0)       result=0;
	      else if(value<1)   result=1;
	      else if(value<2)   result=2;
	      else if(value<4)   result=3;
	      else if(value<8)   result=4;
	      else if(value<16)  result=5;
	      else if(value<32)  result=6;
	      else if(value<64)  result=7;
	      else if(value<128) result=8;
	      else                result=9;
		return result;
	}
	
	@Override
	public float calculateSimilarity(int[] a, int[] b) {
		int similarity= 0;
		int alog, blog;
		for (int i = 0; i < b.length; i++) {
			alog = computeLog(a[i]);
			blog = computeLog(b[i]);
			if (alog > blog)
				similarity = similarity + alog - blog;
			else
				similarity = similarity + blog - alog;
		}
		return (float)similarity;
	}

	@Override
	final public byte getComparisonMeasure() {
		return ImageSimilarity.DISTANCE_COMPARISON;
	}
	
	@Override
	public String toString() {
		return "Dlog";
	}

	@Override
	final public boolean isDistanceMeasure() {
		return true;
	}
}
