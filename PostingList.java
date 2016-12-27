import java.util.LinkedList;

public class PostingList {

	private LinkedList<Posting> postinglist = new LinkedList<Posting>();

	@SuppressWarnings("unchecked")
	public PostingList(PostingList postinglst) {
		this.postinglist = (LinkedList<Posting>) postinglst.getPostinglist().clone();
	}

	public PostingList() {
	}

	public LinkedList<Posting> getPostinglist() {
		return postinglist;
	}

	public void setPostinglist(LinkedList<Posting> postinglist) {
		this.postinglist = postinglist;
	}

}
