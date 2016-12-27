import java.util.Comparator;


public class PostingListIncreasingSizeComparator implements Comparator<PostingList>{

	@Override
	public int compare(PostingList o1, PostingList o2) {
		if (o1.getPostinglist().size() > o2.getPostinglist().size()) {
			return 1;
		} else if (o1.getPostinglist().size() == o2.getPostinglist().size()) {
			return 0;
		} else {
			return -1;
		}
	}

}
