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

public class AddSimulinkBlockToTriplestoreThroughSPARQLEndpoint {

	public static void main(String[] args) {
		Thread thread = new Thread() {
			public void start() {
				// Create a new update
				String updateString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
						"PREFIX simulink: <http://www.mathworks.com/products/simulink/> " 
						+ "INSERT DATA"
						+ "{<http://mydomain.org/myNewSimulink_Block> rdf:type simulink:Block .}";

				UpdateRequest updateRequest = UpdateFactory.create(updateString);

				// Execute the query and obtain results
				UpdateProcessor updateProcessor = UpdateExecutionFactory.createRemote(
						updateRequest, "http://localhost:3030/mydataset/update");
				updateProcessor.execute();
			}			
		};
		thread.start();
		try {
			thread.join();
			System.out.println("Triple added to triplestore");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
	}
}
