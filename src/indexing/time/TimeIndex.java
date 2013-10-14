package indexing.time;

import collections.MultimediaCollection;
import models.MultimodalItem;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Time index based.
 */
public class TimeIndex {

	private MultimediaCollection collection;

	public TimeIndex(MultimediaCollection collection) {
		this.collection = collection;
	}

	public Set<String> search(MultimodalItem item, int k) throws IOException {
		TreeSet<TimeDiff> set = new TreeSet<TimeDiff>();
		for(MultimodalItem other : collection.values()) {
			if(item.id.equals(other.id))
				continue;
			
			long t1 = Math.abs(item.timestamp_taken - other.timestamp_taken);
			long t2 = Math.abs(item.timestamp_uploaded - other.timestamp_uploaded);
			
			long t = Math.min(t1, t2);
			if(item.username.equals(other.username))
				t=0;
			
			if(t > 1000 * 3600 * 48)
				continue;
			
			TimeDiff timeDiff = new TimeDiff(other.id, t);
			set.add(timeDiff);
		}
		Set<String> ret = new HashSet<String>();
		for(int i=0; i<k; i++) {
			TimeDiff r = set.pollFirst();
			ret.add(r.id);
		}
		return ret;
	}

	public int size() {
		return collection.size();
	}
	
	private static class TimeDiff implements Comparable<TimeDiff> {
		public String id;
		public long time;

		public TimeDiff(String id, long time) {
			this.id = id;
			this.time = time;
		}
		
		@Override
		public String toString() {
			return id+" @ "+time;
		}

		@Override
		public int compareTo(TimeDiff other) {
			return other.time > time ? -1 : 1;
		}
	}
	
}