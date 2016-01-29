package fuseki.clients;

import java.io.InputStream;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.util.FileManager;

public class DeleteAllTriplesInTriplestoreThroughSPARQLEndpoint {

	public static void main(String[] args) {
		Thread thread = new Thread() {
			public void start() {
				// Create a new update
				String deleteString = 				
						 "DELETE" 
						+ "{?s ?p ?o .}" 
						+ "WHERE"
						+ "{?s ?p ?o .}";

				UpdateRequest updateRequest = UpdateFactory.create(deleteString);

				// Execute the query and obtain results
				UpdateProcessor updateProcessor = UpdateExecutionFactory.createRemote(
						updateRequest, "http://localhost:3030/mydataset/update");
				updateProcessor.execute();	
			}			
		};
		thread.start();
		try {
			thread.join();
			System.out.println("All triples deleted in triplestore");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
}
