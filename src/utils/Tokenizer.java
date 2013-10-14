package utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.da.DanishAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.pt.PortugueseAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.fa.PersianAnalyzer;
import org.apache.lucene.analysis.ar.ArabicAnalyzer;
import org.apache.lucene.analysis.ro.RomanianAnalyzer;
import org.apache.lucene.analysis.id.IndonesianAnalyzer;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.sv.SwedishAnalyzer;
import org.apache.lucene.analysis.no.NorwegianAnalyzer;
import org.apache.lucene.analysis.fi.FinnishAnalyzer;
import org.apache.lucene.analysis.tr.TurkishAnalyzer;
import org.apache.lucene.analysis.hu.HungarianAnalyzer;
import org.apache.lucene.analysis.bg.BulgarianAnalyzer;
import org.apache.lucene.analysis.morfologik.MorfologikAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

public class Tokenizer {

    public static String DELIMITERS = "\\s";
    public static String PUNCTUATION = "[,.:;?!'\"()]";
    public static String DELIMITERS_AND_PUNCTUATION = "[,.:;?!'\"()\\s]";
	
	Map<String, Analyzer> analyzers = new HashMap<String, Analyzer>();
	Analyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_44);
	
	public Tokenizer() {
		
		Analyzer en_analyzer = new EnglishAnalyzer(Version.LUCENE_44);
		Analyzer es_analyzer = new SpanishAnalyzer(Version.LUCENE_44);
		Analyzer de_analyzer = new GermanAnalyzer(Version.LUCENE_44);
		Analyzer da_analyzer = new DanishAnalyzer(Version.LUCENE_44);
		Analyzer el_analyzer = new GreekAnalyzer(Version.LUCENE_44);
		Analyzer fr_analyzer = new FrenchAnalyzer(Version.LUCENE_44);
		Analyzer it_analyzer = new ItalianAnalyzer(Version.LUCENE_44);
		Analyzer pt_analyzer = new PortugueseAnalyzer(Version.LUCENE_44);
		Analyzer ru_analyzer = new RussianAnalyzer(Version.LUCENE_44);
		Analyzer fa_analyzer = new PersianAnalyzer(Version.LUCENE_44);	
		Analyzer ar_analyzer = new ArabicAnalyzer(Version.LUCENE_44);
		Analyzer id_analyzer = new IndonesianAnalyzer(Version.LUCENE_44);
		Analyzer pl_analyzer = new MorfologikAnalyzer(Version.LUCENE_44);
		Analyzer nl_analyzer = new DutchAnalyzer(Version.LUCENE_44);
		Analyzer no_analyzer = new NorwegianAnalyzer(Version.LUCENE_44);
		Analyzer ro_analyzer = new RomanianAnalyzer(Version.LUCENE_44);
		Analyzer sv_analyzer = new SwedishAnalyzer(Version.LUCENE_44);
		Analyzer fi_analyzer = new FinnishAnalyzer(Version.LUCENE_44);
		Analyzer tr_analyzer = new TurkishAnalyzer(Version.LUCENE_44);
		Analyzer hu_analyzer = new HungarianAnalyzer(Version.LUCENE_44);
		Analyzer bg_analyzer = new BulgarianAnalyzer(Version.LUCENE_44);
		
		analyzers.put("en", en_analyzer);
		analyzers.put("es", es_analyzer);
		analyzers.put("de", de_analyzer);
		analyzers.put("da", da_analyzer);
		analyzers.put("el", el_analyzer);
		analyzers.put("fr", fr_analyzer);
		analyzers.put("it", it_analyzer);
		analyzers.put("pt", pt_analyzer);
		analyzers.put("ru", ru_analyzer);
		analyzers.put("fa", fa_analyzer);
		analyzers.put("ar", ar_analyzer);
		analyzers.put("id", id_analyzer);
		analyzers.put("pl", pl_analyzer);
		analyzers.put("nl", nl_analyzer);
		analyzers.put("no", no_analyzer);
		analyzers.put("ro", ro_analyzer);
		analyzers.put("sv", sv_analyzer);
		analyzers.put("fi", fi_analyzer);
		analyzers.put("tr", tr_analyzer);
		analyzers.put("hu", hu_analyzer);
		analyzers.put("bg", bg_analyzer);
	}
	
    public List<String> parseKeywords(String keywords, String lang) {
    	keywords.replaceAll(DELIMITERS_AND_PUNCTUATION, " ");
    	keywords.replaceAll("_", " ");
    	keywords.replaceAll("-", " ");
    	
    	Analyzer analyzer = getAnalyzer(lang);
        List<String> result = new ArrayList<String>();
        try {
        	TokenStream stream  = analyzer.tokenStream(null, new StringReader(keywords));
        	stream.reset();
            while(stream.incrementToken()) {
                result.add(stream.getAttribute(CharTermAttribute.class).toString());
            }
        }
        catch(IOException e) {
          
        }
        return result;
    }  
    
    private Analyzer getAnalyzer(String lang) {
    	if(lang == null)
    		return standardAnalyzer;
    	
    	Analyzer langAnalyzer = analyzers.get(lang);
    	
    	return langAnalyzer==null ? standardAnalyzer : langAnalyzer;
    	
    }
}