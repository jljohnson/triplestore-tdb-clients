package threads;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

import util.TriplestoreUtil;

public class DeleteAllTriplesInTriplestoreBelongingToSimulinkModelThread extends Thread{

	String fileName;
	
	public DeleteAllTriplesInTriplestoreBelongingToSimulinkModelThread(String fileName){
		this.fileName = fileName;
	}
	
	public void start() {
		// load model from triplestore
		String directory = TriplestoreUtil.getTriplestoreLocation();
		Dataset dataset = TDBFactory.createDataset(directory);

		// delete all triples where the resource, or predicate, or object is owned by a specific model
		String queryString = 
			"" +
			"" +
			"DELETE{?s  ?p ?o .}" +
			"WHERE {" +
			"    ?s  ?p ?o . "
			+ "FILTER ( regex(str(?s), \"/services/" + fileName + "/\") || regex(str(?p), \"/services/" + fileName + "/\") || regex(str(?o), \"/services/" + fileName + "/\")) "
			+ "}";
		UpdateRequest query = UpdateFactory.create(queryString);

		// Execute the query and obtain results
		UpdateProcessor qe = UpdateExecutionFactory.create(query, (GraphStore) dataset.asDatasetGraph());
		qe.execute();
		
		
		dataset.close();
	}

}