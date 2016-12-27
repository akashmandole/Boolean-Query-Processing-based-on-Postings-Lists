import java.util.Comparator;


public class IncreasingDocIDComparator implements Comparator<Posting>{

	@Override
	public int compare(Posting o1, Posting o2) {
	
		if (o1.getDocId() > o2.getDocId()) {
			return 1;
		} else if (o1.getDocId() == o2.getDocId()) {
			return 0;
		} else {
			return -1;
		}
	}

}
