package indexing.textual;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import models.MultimodalItem;
import models.TFIDFVector;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import collections.MultimediaCollection;

import utils.StringUtils;


public class TextualIndex {

	private FSDirectory directory;
	private Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_44);
	private DirectoryReader ireader = null;
	private IndexSearcher isearcher = null;
	
	public TextualIndex(String folder, boolean writeMode) throws IOException {
		directory = FSDirectory.open(new File(folder));
		if(!writeMode) {
			ireader = DirectoryReader.open(directory);
			isearcher = new IndexSearcher(ireader);
		}
	}
	
	public TextualIndex(String folder) throws IOException {
		
		directory = FSDirectory.open(new File(folder));
		ireader = DirectoryReader.open(directory);
		isearcher = new IndexSearcher(ireader);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	public int size() {
		try {
			return ireader.getDocCount("id");
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public Set<String> search(MultimodalItem item, int k) throws Exception {
		Set<String> set = new HashSet<String>();
		if(ireader == null) {
			ireader = DirectoryReader.open(directory);
		}
		if(isearcher == null) {
			isearcher = new IndexSearcher(ireader);
		}
		
		QueryParser parser = new QueryParser(Version.LUCENE_44, "title", analyzer);
		String text = item.title.replaceAll("\n", " ").replaceAll("\r", " ");
	    Query query = parser.parse(text);
	    ScoreDoc[] hits = isearcher.search(query, k).scoreDocs;
	    for (ScoreDoc hit : hits) {
	    	Document hitDoc = isearcher.doc(hit.doc);
	    	String otherId = hitDoc.getField("id").stringValue();
	    	if(item.id.equals(otherId))
				continue;
	    	set.add(otherId);
	    }
	    
	    parser = new QueryParser(Version.LUCENE_44, "description", analyzer);
		text = item.description.replaceAll("\n", " ").replaceAll("\r", " ");
	    query = parser.parse(text);
	    hits = isearcher.search(query, k).scoreDocs;
	    for (ScoreDoc hit : hits) {
	    	Document hitDoc = isearcher.doc(hit.doc);
	    	String otherId = hitDoc.getField("id").stringValue();
	    	if(item.id.equals(otherId))
				continue;
	    	set.add(otherId);
	    }
	    
	    parser = new QueryParser(Version.LUCENE_44, "tags", analyzer);
		text = StringUtils.concat(item.tags);
	    query = parser.parse(text);
	    hits = isearcher.search(query, k).scoreDocs;
	    for (ScoreDoc hit : hits) {
	    	Document hitDoc = isearcher.doc(hit.doc);
	    	String otherId = hitDoc.getField("id").stringValue();
	    	if(item.id.equals(otherId))
				continue;
	    	set.add(otherId);
	    }
	    return set;
	}
	
	public TFIDFVector getTFIDFVector(String id, String field) {
		TFIDFVector vector = new TFIDFVector();
		Query query = new TermQuery(new Term("id", id));
		try {
			int docs = ireader.numDocs();
			ScoreDoc[] hits = isearcher.search(query, 1).scoreDocs;
			if(hits.length > 0) {
				int docId = hits[0].doc;
				try {
					
					Terms terms = ireader.getTermVector(docId, field);
					if(terms == null) {
						return vector;
					}
					
					TermsEnum termsEnum = null;
					termsEnum = terms.iterator(termsEnum);
					
					BytesRef term = null;
					while ((term = termsEnum.next()) != null) {
						BytesRef termName = termsEnum.term();
						String termText = term.utf8ToString();
						int tf = (int) termsEnum.totalTermFreq();
						
						double df = ((double)ireader.docFreq(new Term(field, termName)));
						double idf = Math.log(docs / (df+1));
						
						vector.addTerm(termText, tf, idf);
					}
					return vector;
				} catch (Exception e) {
					e.printStackTrace();
					return vector;
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return vector;
	}
	
//	public Map<String, Integer> getTFVector(IndexReader reader, String field, int docID) {
//		Map<String, Integer> vector = new HashMap<String, Integer>();
//		try {
//			Fields fields = reader.getTermVectors(docID);
//			Terms titleTerms = fields.terms(field);
//			TermsEnum termsEnum = titleTerms.iterator(null);
//			
//			BytesRef text = null;
//			while ((text = termsEnum.next()) != null) {
//				String temp = text.utf8ToString();
//				int freq = (int) termsEnum.totalTermFreq();
//				vector.put(temp, freq);
//			}
//			return vector;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//		
//	}
//	
//	public Map<String, Double> getIDFVector(IndexReader reader, String field, int docID) {
//		Map<String, Double> vector = new HashMap<String, Double>();
//		
//		try {
//			Fields fields = reader.getTermVectors(docID);
//			Terms titleTerms = fields.terms(field);
//			TermsEnum termsEnum = titleTerms.iterator(null);
//			int docs = reader.numDocs();
//			//BytesRef text = null;
//			while (termsEnum.next() != null) {
//				BytesRef termName = termsEnum.term();
//				double df = ((double)reader.docFreq(new Term(field, termName)));
//				double idf = Math.log(docs / (df+1));
//				
//				String temp = termName.utf8ToString();
//				vector.put(temp, idf);
//			}
//			return vector;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
	
	public void createIndex(MultimediaCollection collection) throws IOException {
		
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_44, analyzer);
		IndexWriter iwriter = new IndexWriter(directory, config);
		
		FieldType type = new FieldType();
		type.setIndexed(true);
		type.setStored(true);
		type.setStoreTermVectors(true);
		
		for(MultimodalItem item : collection.values()) {
			Document doc = new Document();
			doc.add(new Field("id", item.id, TextField.TYPE_STORED));
			doc.add(new Field("title", item.title, type));
		    doc.add(new Field("description", item.description, type));
		    doc.add(new Field("tags", StringUtils.concat(item.tags), type));
		    try {
				iwriter.addDocument(doc);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
    	}
		iwriter.close();
	}
}
