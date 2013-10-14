package indexing.spatial;

import collections.MultimediaCollection;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Shape;

import models.MultimodalItem;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Geo-spatial index based on Lucene spatial module.
 */
public class SpatialIndex {


	public static void main(String[] args) throws IOException {
	
	}

	private SpatialContext ctx;
	private SpatialStrategy strategy;

	private Directory directory;

	public SpatialIndex(String folder) throws IOException {
		this.ctx = SpatialContext.GEO;
		int maxLevels = 11;
		SpatialPrefixTree grid = new GeohashPrefixTree(ctx, maxLevels);

		this.strategy = new RecursivePrefixTreeStrategy(grid, "myGeoField");
		directory = FSDirectory.open(new File(folder));
	}

	public int size() {
		int docs = 0;
		try {
			IndexReader indexReader = DirectoryReader.open(directory);
			docs = indexReader.getDocCount("id");
			indexReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return docs;
	}
	
	
	public void createIndex(MultimediaCollection items) throws IOException {
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_44);
		IndexWriterConfig iwConfig = new IndexWriterConfig(Version.LUCENE_44, analyzer);
		IndexWriter indexWriter = new IndexWriter(directory, iwConfig);

		for(MultimodalItem item : items.values()) {
			if(item.hasLocation()) {
				Document doc = getGeoDocument(item.id, item.latitude, item.longitude);
				indexWriter.addDocument(doc);
			}
		}
		indexWriter.close();
	}

	private Document getGeoDocument(String id, double latitude, double longitude) {
		Document doc = new Document();
		doc.add(new Field("id", id, TextField.TYPE_STORED));
		
		Shape shape = ctx.makePoint(longitude, latitude);
		for (IndexableField f : strategy.createIndexableFields(shape)) {
			doc.add(f);
		}
		doc.add(new StoredField(strategy.getFieldName(), shape.toString()));
		doc.add(new StoredField("lat", latitude));
		doc.add(new StoredField("lng", longitude));
		
		return doc;
	}

	public Set<String> search(MultimodalItem item, int k) throws IOException {
		Set<String> set = new HashSet<String>();
		
		IndexReader indexReader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		
		Point pt = ctx.makePoint(item.longitude, item.latitude);
		ValueSource valueSource = strategy.makeDistanceValueSource(pt);
		Sort distSort = new Sort(valueSource.getSortField(false)).rewrite(indexSearcher);
		TopFieldDocs docs = indexSearcher.search(new MatchAllDocsQuery(), k, distSort);
		for(ScoreDoc scoreDoc : docs.scoreDocs) {
			Document doc = indexSearcher.doc(scoreDoc.doc);
			String otherId = doc.getField("id").stringValue();
			if(item.id.equals(otherId))
				continue;
			
			set.add(otherId);
//			try {
//				Number longitude = doc.getField("lng").numericValue();
//				Number latitude = doc.getField("lat").numericValue();	
//				Double dist = GeodesicDistanceCalculator.vincentyDistance(latitude.doubleValue(), longitude.doubleValue(), item.latitude, item.longitude);
//			}
//			catch(Exception e){
//				e.printStackTrace();
//			}			
		}
		indexReader.close();
		return set;
	}
}