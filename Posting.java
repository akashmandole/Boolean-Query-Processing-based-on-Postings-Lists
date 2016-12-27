public class Posting implements Comparable<Posting>{
	private int docId;
	private int termFrequency;

	public int getDocId() {
		return docId;
	}

	public void setDocId(int docId) {
		this.docId = docId;
	}

	public int getTermFrequency() {
		return termFrequency;
	}

	public void setTermFrequency(int termFrequency) {
		this.termFrequency = termFrequency;
	}

	@Override
	public int compareTo(Posting o) {
		int comparedTermFrequency = o.termFrequency;
		if (this.termFrequency > comparedTermFrequency) {
			return 1;
		} else if (this.termFrequency == comparedTermFrequency) {
			return 0;
		} else {
			return -1;
		}
	}

}
