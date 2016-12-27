import java.util.Comparator;


public class DecreasingTermFreqComparator implements Comparator<Posting>{

	@Override
	public int compare(Posting o1, Posting o2) {
		
		if (o1.getTermFrequency() > o2.getTermFrequency()) {
			return -1;
		} else if (o1.getTermFrequency() == o2.getTermFrequency()) {
			return 0;
		} else {
			return 1;
		}
	}

}
