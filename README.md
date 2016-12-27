# Boolean-Query-Processing-based-on-Postings-Lists
 
In this Assignment, posting lists are provided generated from the RCV1 news corpus (http://www.daviddlewis.com/resources/testcollections/rcv1/).   
 
Rebuild the index after reading the data. Linked List is used to store the index data in memory. Two index are created with two different ordering strategies:   
 1) The posting of each term should be ordered by increasing document IDs 
 2) The postings of each term should be ordered by decreasing term frequencies.   
 
Modules are created that return documents based on term-at-a-time with the postings list ordered by term frequencies, and document-at-a-time with the postings list ordered by doc IDs for a set of queries.
