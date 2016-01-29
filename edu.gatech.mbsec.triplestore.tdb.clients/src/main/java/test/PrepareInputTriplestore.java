package test;

import tdb.clients.CreateTriplestore;
import tdb.clients.GETAllMagicDrawBlocksAndPopulateTriplestoreWithPOJOs;
import tdb.clients.metadata.GetResourceShapesOfMagicDrawAdapterAndPushToStore;
import tdb.clients.metadata.GetVocabularyOfMagicDrawAdapterAndPushToStore;

public class PrepareInputTriplestore {

	public static void main(String[] args) {
		CreateTriplestore.main(null);
		GETAllMagicDrawBlocksAndPopulateTriplestoreWithPOJOs.main(null);
		GetResourceShapesOfMagicDrawAdapterAndPushToStore.main(null);
		GetVocabularyOfMagicDrawAdapterAndPushToStore.main(null);

		
	}

}
