package collections;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import models.MultimodalItem;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@SuppressWarnings("serial")
public class MultimediaCollection extends HashMap<String, MultimodalItem> {

	public static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	
	public MultimediaCollection() {
    	super();     
    }
	
    public MultimediaCollection(String metadataFilename) {
    	super();     
        loadBasicData(metadataFilename, null);
    }
    
    public MultimediaCollection(String metadataFilename, Set<String> subset) {
    	super();     
        loadBasicData(metadataFilename, subset);
    }
    
    private void loadBasicData(String filename, Set<String> subset) {
        File xmlFile = new File(filename);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            NodeList photoNodes = doc.getElementsByTagName("photo");
            int n_items = photoNodes.getLength();
            for(int i=0; i<n_items; i++) {  
            	
            	Node node = photoNodes.item(i);
            	NamedNodeMap nodeAttributes = node.getAttributes();
                MultimodalItem item = new MultimodalItem();
                
                item.id = nodeAttributes.getNamedItem("id").getNodeValue().trim();
                
                if(subset!=null && !subset.contains(item.id))
                	continue;
                	
                item.username = nodeAttributes.getNamedItem("username").getNodeValue().trim();
                
                String dateTaken = nodeAttributes.getNamedItem("dateTaken").getNodeValue();
                item.date_taken = formatter.parse(dateTaken);
                item.timestamp_taken = item.date_taken.getTime();
                
                String dateUploaded = nodeAttributes.getNamedItem("dateUploaded").getNodeValue();
                item.date_uploaded = formatter.parse(dateUploaded);
                item.timestamp_uploaded = item.date_uploaded.getTime();

                NodeList childNodes = node.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                	Node childNode = childNodes.item(j);
                	String nodeName = childNode.getNodeName();
                	if (nodeName.equals("title")) {
                		item.title = childNode.getTextContent().trim();
                	} else if (nodeName.equals("description")){
                		item.description = childNode.getTextContent().trim();                    
                	} else if (nodeName.equals("tags")) {
                		NodeList tagNodes = childNode.getChildNodes();
                		List<String> tags = new ArrayList<String>(tagNodes.getLength());
                		for (int p = 0; p < tagNodes.getLength(); p++)
                			if(!tagNodes.item(p).getTextContent().trim().equals(""))
                				tags.add(tagNodes.item(p).getTextContent().trim().toLowerCase());
                		item.tags = tags;
                	} else if (nodeName.equals("location")) {
                		NamedNodeMap locationAttributes = childNode.getAttributes();
                		item.latitude=Double.parseDouble(locationAttributes.getNamedItem("latitude").getNodeValue());
                		item.longitude=Double.parseDouble(locationAttributes.getNamedItem("longitude").getNodeValue());
                	}
                }
                
                this.put(item.id, item);
                
            }
        } catch (IOException e) {
        	e.printStackTrace();
        } catch (ParserConfigurationException e) {
        	e.printStackTrace();
        } catch (SAXException e) {
        	e.printStackTrace();
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
    }  

    
//    private void processTitleFeature(MultimodalItem item) {
//    	String title = item.title;
//        if(title.trim().length()>0){
//            Scanner tokenize = new Scanner(title);
//            tokenize.useDelimiter(Constants.DELIMITERS);
//            
//            while (tokenize.hasNext()) {
//                String term = tokenize.next().toLowerCase();
//                while((term.length()>0) && ((term.charAt(0)+"").matches(Constants.PUNCTUATION)))
//                    term = term.substring(1);
//                while((term.length()>0)&&((term.charAt(term.length()-1)+"").matches(Constants.PUNCTUATION)))
//                    term = term.substring(0,term.length()-1);
//                if(term.length()>0) {
//                    Word word = textualCodebook.getWord(term);
//                    if(word == null) {
//                    	word = new Word(term);
//                        textualCodebook.addWord(word);
//                    }
//                    //item.titleTFIDF.wordOccurrence(tmp_word_voc);
//                }
//            }   
//            tokenize.close();
//        }
//    }
    
    
//    private void processDescriptionFeature(MultimodalItem item,String feature){
//        if(feature.trim().length()>0){
//            Scanner tokenize;
//            String tmp_word;
//            tokenize = new Scanner(feature);
//            tokenize.useDelimiter(Constants.DELIMITERS);
//            while (tokenize.hasNext()) {
//                tmp_word=tokenize.next().toLowerCase();
//                String[] tmp_str = tmp_word.split(Constants.PUNCTUATION);
//                while((tmp_word.length()>0)&&((tmp_word.charAt(0)+"").matches(Constants.PUNCTUATION)))
//                    tmp_word=tmp_word.substring(1);
//                while((tmp_word.length()>0)&&((tmp_word.charAt(tmp_word.length()-1)+"").matches(Constants.PUNCTUATION)))
//                    tmp_word=tmp_word.substring(0,tmp_word.length()-1);
//                if(tmp_word.length()>0){
//                    Word tmp_word_voc=textualCodebook.getWord(tmp_word);
//                    if(tmp_word_voc==null){
//                    	tmp_word_voc=new Word(tmp_word);
//                        textualCodebook.addWord( tmp_word_voc);
//                    }
//                    //item.descriptionTFIDF.wordOccurrence(tmp_word_voc);
//                }
//            }            
//        }
//    }
    
 }
