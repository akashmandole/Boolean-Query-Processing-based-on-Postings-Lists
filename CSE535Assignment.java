import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class CSE535Assignment {
	
	
	private static File inputFile; // Input file
	private static File outputLogFile; // Output File 
	private static File queryFile; // Query File

	int K = -1; // Top K terms intializing it to -1
	
	// Index posting ordered by increasing document IDs
	private HashMap<DictionaryTerms, PostingList> indexPostingIncreasingDocID = new HashMap<DictionaryTerms, PostingList>();
	// Index posting ordered by decreasing term frequencies
	private HashMap<DictionaryTerms, PostingList> indexDecreasingTermFrequencies = new HashMap<DictionaryTerms, PostingList>();
	
	int comparisionMade = 0;
	
	// Function to create both index
	public void createIndex() throws FileNotFoundException, IOException {
		
		try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
			
			for (String line; (line = br.readLine()) != null;) { // read input file line by line
				
				DictionaryTerms dictionaryTerms = new DictionaryTerms();
				PostingList postinglst = new PostingList();

				String[] index = line.trim().split("\\\\"); // split on '\' character
				
				String term = index[0];
				String docfreq = index[1].replace("c", ""); // calculate doc frequency for each term
				
				dictionaryTerms.setTerm(term); // set dictionary term
				dictionaryTerms.setDocumentFrequency(Integer.parseInt(docfreq)); // set corresponding document frequency with term
				
				String postingList = index[2].replace("m", "").replace("[", "").replace("]", ""); // calculate posting list for each term
				String[] postingArr = postingList.split(",");

				for (String posting : postingArr) {

					Posting postingElement = new Posting();
					
					String[] postingArrElements = posting.split("/");
					String docId = postingArrElements[0];  
					String termFreq = postingArrElements[1]; 
					
					postingElement.setDocId(Integer.parseInt(docId.trim())); // set document Id
					postingElement.setTermFrequency(Integer.parseInt(termFreq.trim())); // set corresponding term frequency
					
					postinglst.getPostinglist().add(postingElement); // add posting element to the posting list
				}
				
				PostingList sortedDocId = sortIncreasingDocId(postinglst); // Sort posting list in increasing doc ID
				PostingList sortedTermFreq = sortDecreasingTermFrequencies(postinglst); // Sort posting list in decreasing term frequency

				indexPostingIncreasingDocID.put(dictionaryTerms, sortedDocId); // Creating index posting ordered by increasing document IDs
				indexDecreasingTermFrequencies.put(dictionaryTerms, sortedTermFreq); // Creating index posting ordered by decreasing term frequencies
				
			}

		}
		
	}
	
	// This function emulates an evaluation of a multi-term Boolean AND query on the index with term-at-a-time query
	private void termAtATimeQueryAnd(String[] queryterms) throws FileNotFoundException, IOException {
			
		FileOutputStream fos = new FileOutputStream(outputLogFile,true); 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		bw.newLine();
		String query = "";
		for (int i=0;i<queryterms.length;i++) {
			query = query.concat(queryterms[i]+ ", ");
		}
		
		bw.write("FUNCTION: termAtATimeQueryAnd " + query.substring(0,query.lastIndexOf(",")));
		
		if (queryterms != null) {
			
				long startTime = System.currentTimeMillis();
				comparisionMade = 0;
				
				List<PostingList> postinglist = new ArrayList<PostingList>();
				List<PostingList> postinglistOptimized = new ArrayList<PostingList>();
				
				for (int i=0;i<queryterms.length;i++) {
					PostingList postinglistDecreasingTermFrequencies = getPostingsDecreasingTermFrequencies(queryterms[i]);
					if (postinglistDecreasingTermFrequencies != null) {
						postinglist.add(postinglistDecreasingTermFrequencies);
					}
				}
				
				postinglistOptimized = sortForOptimizationIncreasing(postinglist);
				
				PostingList and = postinglist.get(0);
				String documentsFound = String.valueOf(0);
				
				if (postinglist.size() == queryterms.length) {					
					for (int i=1;i<postinglist.size();i++) {
						and = ANDTermAtATime(and,postinglist.get(i));
					}					
					documentsFound =  String.valueOf(and.getPostinglist().size());
				} 
				
				
				long stopTime = System.currentTimeMillis();
				long millisecondsused = stopTime - startTime;
				double secondsUsed = millisecondsused / 1000.0;
				
				String comparisionMadeStr = String.valueOf(comparisionMade);
				String secondsUsedStr = String.valueOf(secondsUsed);;
				
				// Optimization
				PostingList andPtimized = postinglist.get(0);
				comparisionMade =0;
				if (postinglist.size() == queryterms.length) {
					for (int i=1;i<postinglistOptimized.size();i++) {
						andPtimized = ANDTermAtATime(andPtimized,postinglistOptimized.get(i));
					}
				}
				String comparisionMadeOptimizedStr = String.valueOf(comparisionMade);
				
				
				bw.newLine();
				bw.write(documentsFound +" documents are found");
				bw.newLine();
				bw.write(comparisionMadeStr +" comparisions are made");
				bw.newLine();
				bw.write(secondsUsedStr + " seconds are used");
				bw.newLine();
				bw.write(comparisionMadeOptimizedStr +" comparisons are made with optimization");
				bw.newLine();
				
				and = sortIncreasingDocId(and);
				Iterator<Posting> anditr = and.getPostinglist().iterator();
				String andstr = "";
				while (anditr.hasNext()) {
					andstr = andstr.concat(anditr.next().getDocId() + ", ");
				 }
				if (documentsFound.equals("0")) {
					bw.write("Result: terms not found");
				} else if (andstr.contains(",")) {					
					bw.write("Result: " + andstr.substring(0,andstr.lastIndexOf(",")));
				} else if (!andstr.isEmpty()) {
					bw.write("Result: " + andstr);
				}
				
			}
		bw.close();
	}
	
	// This function emulates an evaluation of a multi-term Boolean OR query on the index with term-at-a-time query.
	private void termAtATimeQueryOr(String[] queryterms) throws FileNotFoundException, IOException {
		
		FileOutputStream fos = new FileOutputStream(outputLogFile,true); 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		bw.newLine();
		String query = "";
		for (int i=0;i<queryterms.length;i++) {
			query = query.concat(queryterms[i]+ ", ");
		}
		
		bw.write("FUNCTION: termAtATimeQueryOr " + query.substring(0,query.lastIndexOf(",")));
		
			if (queryterms != null) {
			
				long startTime = System.currentTimeMillis();
				comparisionMade = 0;
				
				List<PostingList> postinglist = new ArrayList<PostingList>();
				List<PostingList> postinglistOptimized = new ArrayList<PostingList>();
						
				for (int i=0;i<queryterms.length;i++) {
					PostingList postinglistDecreasingTermFrequencies = getPostingsDecreasingTermFrequencies(queryterms[i]);
					if (postinglistDecreasingTermFrequencies != null) {
						postinglist.add(postinglistDecreasingTermFrequencies);
					}
				}
				
				postinglistOptimized = sortForOptimizationDecreasing(postinglist);
				
				PostingList or = postinglist.get(0);
				String documentsFound = String.valueOf(0);
				
				for (int i=1;i<postinglist.size();i++) {
					or = ORTermAtATime(or,postinglist.get(i));
				}
				
				
				documentsFound =  String.valueOf(or.getPostinglist().size());

				long stopTime = System.currentTimeMillis();
				long millisecondsused = stopTime - startTime;
				double secondsUsed = millisecondsused / 1000.0;
				
				String comparisionMadeStr = String.valueOf(comparisionMade);
				String secondsUsedStr = String.valueOf(secondsUsed);;
				
				// Optimization
				PostingList orPtimized = postinglist.get(0);
				comparisionMade =0;
				
				for (int i=1;i<postinglistOptimized.size();i++) {
					orPtimized = ORTermAtATime(orPtimized,postinglistOptimized.get(i));
				}
				
				String comparisionMadeOptimizedStr = String.valueOf(comparisionMade);
				
				bw.newLine();
				bw.write(documentsFound +" documents are found");
				bw.newLine();
				bw.write(comparisionMadeStr +" comparisions are made");
				bw.newLine();
				bw.write(secondsUsedStr + " seconds are used");
				bw.newLine();
				bw.write(comparisionMadeOptimizedStr +" comparisons are made with optimization");
				bw.newLine();
				
				or = sortIncreasingDocId(or);
				Iterator<Posting> oritr = or.getPostinglist().iterator();
				String orstr = "";
				while (oritr.hasNext()) {
					orstr = orstr.concat(oritr.next().getDocId() + ", ");
				 }
				if (documentsFound.equals("0")) {
					bw.write("Result: terms not found");
				} else if (orstr.contains(",")) {					
					bw.write("Result: " + orstr.substring(0,orstr.lastIndexOf(",")));
				} else if (!orstr.isEmpty()) {
					bw.write("Result: " + orstr);
				}
				
			}
			bw.close();
	}
	
	// Sort posting list in decreasing order of size
	private List<PostingList> sortForOptimizationDecreasing(List<PostingList> postinglist) {

		List<PostingList> result =   new ArrayList<PostingList>();
		for (int i=0;i<postinglist.size();i++) {
			result.add(new PostingList(postinglist.get(i)));
		}
		Collections.sort(result,new PostingListDecreasingSizeComparator());
		return result;
	}

	// Sort posting list in increasing order of size
	private List<PostingList> sortForOptimizationIncreasing(List<PostingList> postinglist) {

		List<PostingList> result =   new ArrayList<PostingList>();
		for (int i=0;i<postinglist.size();i++) {
			result.add(new PostingList(postinglist.get(i)));
		}
		Collections.sort(result,new PostingListIncreasingSizeComparator());
		return result;
	}

	
	// This function calculates OR of two posting list
	private PostingList ORTermAtATime(PostingList p1, PostingList p2) {
		 PostingList result = new PostingList();
		 Iterator<Posting> p1itr = p1.getPostinglist().iterator();
		 Iterator<Posting> p2itr = p2.getPostinglist().iterator();
		 
		 while (p1itr.hasNext()) {
			 Posting p1post = p1itr.next();
			 result.getPostinglist().add(p1post);
		 }
		 while (p2itr.hasNext()) {
			 p1itr = p1.getPostinglist().iterator();
			 Posting p2post = p2itr.next();
			 boolean found = false;
			 while (p1itr.hasNext()) {
				 Posting p1post = p1itr.next();
				 comparisionMade++;
				 if (p1post.getDocId() == p2post.getDocId()){
					 found = true;
					 break;
				 }
			 }
			 if (found == false) {
				 result.getPostinglist().add(p2post);
			 }
		 }
		return result;	
	}

	// This function calculates OR of two posting list
	private PostingList ANDTermAtATime(PostingList p1, PostingList p2) {
		PostingList result = new PostingList();
		 Iterator<Posting> p1itr = p1.getPostinglist().iterator();
		 
		 while (p1itr.hasNext()) {
			 Posting p1post = p1itr.next();
			 Iterator<Posting> p2itr = p2.getPostinglist().iterator();
			 while (p2itr.hasNext()) {
				 comparisionMade++;
				 if (p1post.getDocId() == p2itr.next().getDocId()) {
					 result.getPostinglist().add(p1post);
				 }
			 }
		 }
		
		return result;
	}
	
	// This function emulates an evaluation of a multi-term Boolean OR query on the index with document-at-a-time query
	private void docAtATimeQueryOR(String [] queryterms) throws FileNotFoundException, IOException {
		
		FileOutputStream fos = new FileOutputStream(outputLogFile,true); 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		bw.newLine();
		String query = "";
		for (int i=0;i<queryterms.length;i++) {
			query = query.concat(queryterms[i]+ ", ");
		}
		
		bw.write("FUNCTION: docAtATimeQueryOr " + query.substring(0,query.lastIndexOf(",")));
		
			if (queryterms != null) {
			
				long startTime = System.currentTimeMillis();
				comparisionMade = 0;
				
				List<PostingList> postinglist = new ArrayList<PostingList>();
				String documentsFound = String.valueOf(0);
						
				for (int i=0;i<queryterms.length;i++) {
					PostingList postinglistIncreasingDocID = getPostingsIncreasingDocID(queryterms[i]);
					if (postinglistIncreasingDocID != null) {
						postinglist.add(postinglistIncreasingDocID);
					}
				}
				
				List<ListIterator<Posting>> postinglistitr = new ArrayList<ListIterator<Posting>>();
				for(int i=0;i<postinglist.size();i++){
					postinglistitr.add(postinglist.get(i).getPostinglist().listIterator());
				}
						
				PostingList result = new PostingList();
				while(anyPointerHasNext(postinglistitr)) {
					
					int mindocid = findMinValueDocID(postinglistitr);
					
					Posting post = traverseAllPostingTillMinDocID(postinglistitr,mindocid);
					if (post != null) {						
						result.getPostinglist().add(post);
					}
				}
				
				documentsFound =  String.valueOf(result.getPostinglist().size());

				long stopTime = System.currentTimeMillis();
				long millisecondsused = stopTime - startTime;
				double secondsUsed = millisecondsused / 1000.0;
				
				String comparisionMadeStr = String.valueOf(comparisionMade);
				String secondsUsedStr = String.valueOf(secondsUsed);;
				
				bw.newLine();
				bw.write(documentsFound +" documents are found");
				bw.newLine();
				bw.write(comparisionMadeStr +" comparisions are made");
				bw.newLine();
				bw.write(secondsUsedStr + " seconds are used");
				bw.newLine();
				
				Iterator<Posting> oritr = result.getPostinglist().iterator();
				String orstr = "";
				while (oritr.hasNext()) {
					orstr = orstr.concat(oritr.next().getDocId() + ", ");
				 }
				if (documentsFound.equals("0")) {
					bw.write("Result: terms not found");
				} else if (orstr.contains(",")) {					
					bw.write("Result: " + orstr.substring(0,orstr.lastIndexOf(",")));
				} else if (!orstr.isEmpty()) {
					bw.write("Result: " + orstr);
				}

			}
			bw.close();
	}
	
	// Traverse all posting lists till minimum doc id
	private Posting traverseAllPostingTillMinDocID(List<ListIterator<Posting>> postinglistitr, int mindocid) {
		Posting result=null;
		for (int i=0;i<postinglistitr.size();i++) {
			if (postinglistitr.get(i).hasNext()) {				
				Posting post = postinglistitr.get(i).next();
				comparisionMade++;
				if (post.getDocId() != mindocid) {
					postinglistitr.get(i).previous();
				}
				else {
					result = post;
				}
			}
		}
		return result;
	}

	// Find the minimum value of all posting iterators
	private int findMinValueDocID(List<ListIterator<Posting>> postinglistitr) {
		int mindocid=9999999; // Assume a very large value (-ve infinity)
		for (int i=0;i<postinglistitr.size();i++) {
		  if (postinglistitr.get(i).hasNext()) {
			int docid = postinglistitr.get(i).next().getDocId();
			comparisionMade++;
			if (docid<mindocid) {
				mindocid = docid;
			}
			postinglistitr.get(i).previous();
		  }
		}
		return mindocid;
	}

	// Check that if any pointer has next value. Use for termination of while loop 
	private boolean anyPointerHasNext(List<ListIterator<Posting>> postinglistitr) {
		for (int i=0;i<postinglistitr.size();i++) {
			if(postinglistitr.get(i).hasNext() == true) {
				return true;
			}
		}
		return false;
	}

	// This function emulates an evaluation of a multi-term Boolean AND query on the index with document-at-a-time query.
	private void docAtATimeQueryAnd(String [] queryterms) throws FileNotFoundException, IOException {
		
		FileOutputStream fos = new FileOutputStream(outputLogFile,true); 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		bw.newLine();
		String query = "";
		for (int i=0;i<queryterms.length;i++) {
			query = query.concat(queryterms[i]+ ", ");
		}
		
		bw.write("FUNCTION: docAtATimeQueryAnd " + query.substring(0,query.lastIndexOf(",")));
			
			if (queryterms != null) {
			
				long startTime = System.currentTimeMillis();
				comparisionMade = 0;
				
				List<PostingList> postinglist = new ArrayList<PostingList>();
				String documentsFound = String.valueOf(0);
						
				for (int i=0;i<queryterms.length;i++) {
					PostingList postinglistIncreasingDocID = getPostingsIncreasingDocID(queryterms[i]);
					if (postinglistIncreasingDocID != null) {
						postinglist.add(postinglistIncreasingDocID);
					}
				}
				
				PostingList result = new PostingList();
				if (postinglist.size() == queryterms.length) {
				 List<ListIterator<Posting>> postinglistitr = new ArrayList<ListIterator<Posting>>();
				 for(int i=0;i<postinglist.size();i++){
					postinglistitr.add(postinglist.get(i).getPostinglist().listIterator());
				 }
						
				 while(anyPointerDoesNotHaveNext(postinglistitr)) {
				
					Posting post = allValuesEqual(postinglistitr);
					if( post != null){
						result.getPostinglist().add(post);
						incrementAll(postinglistitr);
					}
					else {
						int maxdocid = findMaxValueDocID(postinglistitr); 
						traverseAllPostingTillMaxDocID(postinglistitr,maxdocid);
					}
				 }
				 documentsFound =  String.valueOf(result.getPostinglist().size());
				}

				long stopTime = System.currentTimeMillis();
				long millisecondsused = stopTime - startTime;
				double secondsUsed = millisecondsused / 1000.0;
				
				String comparisionMadeStr = String.valueOf(comparisionMade);
				String secondsUsedStr = String.valueOf(secondsUsed);;
				
				bw.newLine();
				bw.write(documentsFound +" documents are found");
				bw.newLine();
				bw.write(comparisionMadeStr +" comparisions are made");
				bw.newLine();
				bw.write(secondsUsedStr + " seconds are used");
				bw.newLine();
				
				Iterator<Posting> anditr = result.getPostinglist().iterator();
				String orstr = "";
				while (anditr.hasNext()) {
					orstr = orstr.concat(anditr.next().getDocId() + ", ");
				 }
				if (documentsFound.equals("0")) {
					bw.write("Result: terms not found");
				} else if (orstr.contains(",")) {					
					bw.write("Result: " + orstr.substring(0,orstr.lastIndexOf(",")));
				} else if (!orstr.isEmpty()) {
					bw.write("Result: " + orstr);
				}
				
			}
		bw.close();
	}
	
	// Increment all values of posting list iterator
	private void incrementAll(List<ListIterator<Posting>> postinglistitr) {
		for (int i=0;i<postinglistitr.size();i++) {
			if (postinglistitr.get(i).hasNext()) {				
				postinglistitr.get(i).next();	
			}
		}
	}

	// Traverse all posting list to maximum document id
	private void traverseAllPostingTillMaxDocID(List<ListIterator<Posting>> postinglistitr, int maxdocid) {
		for (int i=0;i<postinglistitr.size();i++) {
			int temp=0;
			while(temp<maxdocid && postinglistitr.get(i).hasNext()){
				comparisionMade++;
				temp=postinglistitr.get(i).next().getDocId();
			}
			if(postinglistitr.get(i).hasNext()!=false) {
				postinglistitr.get(i).previous();
			}
		}
	}

	// Find maximum value of doc id in the current posting iterators
	private int findMaxValueDocID(List<ListIterator<Posting>> postinglistitr) {
		int maxdocid=0;
		for (int i=0;i<postinglistitr.size();i++) {
			int docid = postinglistitr.get(i).next().getDocId();
			comparisionMade++;
			if (docid>maxdocid) {
				maxdocid = docid;
			}
			postinglistitr.get(i).previous();
		}
		return maxdocid;
	}

	// Check if all values iterators point are equal
	private Posting allValuesEqual(List<ListIterator<Posting>> postinglistitr) {
		Posting post;
		boolean allvaluesequal = true;
		post = postinglistitr.get(0).next();
		postinglistitr.get(0).previous();
		for (int i=1;i<postinglistitr.size();i++) {
			comparisionMade++;
			if (post.getDocId() !=  postinglistitr.get(i).next().getDocId()) {
				allvaluesequal=false;
			}
			postinglistitr.get(i).previous();
		}
		if (allvaluesequal == true){			
			return post;
		}else {
			return null;
		}
	}

	// Check if any pointer does not have next use to terminate the for loop
	private boolean anyPointerDoesNotHaveNext(List<ListIterator<Posting>> postinglistitr) {
		for (int i=0;i<postinglistitr.size();i++) {
			if(postinglistitr.get(i).hasNext() == false) {
				return false;
			}
		}
		return true;
	}

	// Retrieve the postings list for array of query terms 
	private void getPostingsFromArray(String [] queryterms) throws FileNotFoundException, IOException {
			
		// Write output to lof file 
		FileOutputStream fos = new FileOutputStream(outputLogFile,true); 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		bw.newLine();		

			if (queryterms != null) {				
				for (int i=0;i<queryterms.length;i++) {
					
					bw.write("FUNCTION: getPostings "+queryterms[i]);
					bw.newLine();
					PostingList postinglistIncreasingDocID = getPostingsIncreasingDocID(queryterms[i]);
					PostingList postinglistDecreasingTermFrequencies = getPostingsDecreasingTermFrequencies(queryterms[i]);
					if (postinglistIncreasingDocID != null && postinglistDecreasingTermFrequencies != null) {	
						bw.write("Ordered by doc IDs: ");
						String postinglistIncreasingDocIDString = "";
						for(Posting p : postinglistIncreasingDocID.getPostinglist()) {
							//System.out.print(p.getDocId() + ",");
							postinglistIncreasingDocIDString = postinglistIncreasingDocIDString.concat(p.getDocId() + ", ");
						}		
						bw.write(postinglistIncreasingDocIDString.substring(0,postinglistIncreasingDocIDString.lastIndexOf(",")));
						bw.newLine();
						bw.write("Ordered by TF: ");
						String postinglistDecreasingTermFrequenciesString = "";
						for(Posting p : postinglistDecreasingTermFrequencies.getPostinglist()) {
							postinglistDecreasingTermFrequenciesString = postinglistDecreasingTermFrequenciesString.concat(p.getDocId() + ", ");
						}		
						bw.write(postinglistDecreasingTermFrequenciesString.substring(0, postinglistDecreasingTermFrequenciesString.lastIndexOf(",")));
					} else {
						bw.write("term not found");
					}
					if(i!=queryterms.length-1) {						
						bw.newLine();
					}
				}
			}		
			bw.close();
	}
	
	// Get postings in increasing doc id
	private PostingList getPostingsIncreasingDocID(String query) {

		Set<DictionaryTerms> keyterms = indexPostingIncreasingDocID.keySet();

		   for (Iterator<DictionaryTerms> i = keyterms.iterator(); i.hasNext();) {
			   
			   DictionaryTerms key = i.next();
			   if (key.getTerm().equals(query)) {				   
				   PostingList value = indexPostingIncreasingDocID.get(key);
				   return value;
			   }
		   }
		
		return null;
	}
	
	// Get posting in decreasing term frequency
	private PostingList getPostingsDecreasingTermFrequencies(String query) {
		
		Set<DictionaryTerms> keyterms = indexDecreasingTermFrequencies.keySet();

		   for (Iterator<DictionaryTerms> i = keyterms.iterator(); i.hasNext();) {
			   
			   DictionaryTerms key = i.next();
			   if (key.getTerm().equals(query)) {				   
				   PostingList value = indexDecreasingTermFrequencies.get(key);
				   return value;
			   }
		   }
		
		return null;
	}

	// This returns the key dictionary terms that have the K largest postings lists
	private void getTopK(int k) throws IOException {
		
		Set<DictionaryTerms> keyterms = indexPostingIncreasingDocID.keySet();
		List<DictionaryTerms> list = new ArrayList<DictionaryTerms>(keyterms);
		Collections.sort(list,Collections.reverseOrder());
		
		// Write output to log file 
		FileOutputStream fos = new FileOutputStream(outputLogFile,true); 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		bw.write("FUNCTION: getTopK " + k);
		bw.newLine();
		bw.write("Result: ");
		
		int j;
		for (j = 0; j < k-1; j++) {
			bw.write(list.get(j).getTerm()+ ", ");
		}
		bw.write(list.get(j).getTerm());
		bw.close();
		
	}
	
	// Sort posting list in decreasing term frequencies
	private PostingList sortDecreasingTermFrequencies(PostingList postinglst) {
		PostingList sortedList = new PostingList(postinglst);
		Collections.sort((List<Posting>) sortedList.getPostinglist(),new DecreasingTermFreqComparator());
		return sortedList;
	}

	// Sort posting list in increasing doc id
	private PostingList sortIncreasingDocId(PostingList postinglst) {
		PostingList sortedList = new PostingList(postinglst);
		Collections.sort((List<Posting>) sortedList.getPostinglist(),new IncreasingDocIDComparator());
		return sortedList;
	}
	
	public static void main(String[] args) throws FileNotFoundException,
			IOException {

		CSE535Assignment cse535Assignment = new CSE535Assignment();
		
		// Read input file
		if (args.length > 0) { 
			
			String inputFileStr = args[0];
			String outputLogFileStr = args[1]; 
			int K = Integer.parseInt(args[2]);
			String queryFileStr = args[3];
			
			long startTime = System.currentTimeMillis();
			 
			if (inputFileStr != null && outputLogFileStr != null && K != -1 && queryFileStr !=null) {				
				
				inputFile = new File(inputFileStr); // initialize input file
				outputLogFile = new File(outputLogFileStr); // initialize output file
				queryFile = new File(queryFileStr); // initialize query file
				
				cse535Assignment.createIndex();
				cse535Assignment.getTopK(K);
				try (BufferedReader br = new BufferedReader(new FileReader(queryFile))) {
					String [] queryterms = null;
					for (String line; (line = br.readLine()) != null;) {
						queryterms = line.trim().split(" ");
						cse535Assignment.getPostingsFromArray(queryterms);
						cse535Assignment.termAtATimeQueryAnd(queryterms);
						cse535Assignment.termAtATimeQueryOr(queryterms);
						cse535Assignment.docAtATimeQueryAnd(queryterms);
						cse535Assignment.docAtATimeQueryOR(queryterms);
					}
				}
				long stopTime = System.currentTimeMillis();
				long millisecondsused = stopTime - startTime;
				double secondsUsed = millisecondsused / 1000.0;
	
				System.out.println("Total time taken(seconds).......   " + secondsUsed);
			}
			else {
				System.out.println("Invalid arguments..");
			}
		}
	}

}
