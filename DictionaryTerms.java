
public class DictionaryTerms implements Comparable<Object>{
	
	private String term;
	
	private int documentFrequency;

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public int getDocumentFrequency() {
		return documentFrequency;
	}

	public void setDocumentFrequency(int documentFrequency) {
		this.documentFrequency = documentFrequency;
	}

	@Override
	public int compareTo(Object o) {
		int comparedDocumentFrequency = ((DictionaryTerms)o).documentFrequency;
		if (this.documentFrequency > comparedDocumentFrequency) {
			return 1;
		} else if (this.documentFrequency == comparedDocumentFrequency) {
			return 0;
		} else {
			return -1;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		String term = ((DictionaryTerms) obj).getTerm();
		return term.equals(this.getTerm());
	}

	
}
