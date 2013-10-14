package utils;

import java.util.List;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

public class StringUtils {

	public static String concat(List<String> tags) {
		StringBuffer strb = new StringBuffer();
		for(String tag : tags) {
			strb.append(tag+" ");
		}
		return strb.toString();
	}
	
	public static String concat(String...strs) {
		StringBuffer strb = new StringBuffer();
		for(String str : strs) {
			strb.append(str+" ");
		}
		return strb.toString();
	}
	
	public static String landDetect(String text) {
		Detector detector;
		try {
			detector = DetectorFactory.create();
			detector.append(text);
			String lang = detector.detect();
			return lang;
		} catch (LangDetectException e) {
			return null;
		}
		
	}
}
