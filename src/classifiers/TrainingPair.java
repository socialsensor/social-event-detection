package classifiers;

import models.MultimodalItem;

public class TrainingPair {
		
		public MultimodalItem item1;
		public MultimodalItem item2;
	
		public TrainingPair(MultimodalItem item1, MultimodalItem item2) {
			this.item1 = item1;
			this.item2 = item2;
		}
	}