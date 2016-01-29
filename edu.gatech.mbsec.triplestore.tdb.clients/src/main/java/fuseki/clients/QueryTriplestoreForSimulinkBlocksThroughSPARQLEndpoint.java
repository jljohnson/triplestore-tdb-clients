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
import com.hp.hpl.jena.util.FileManager;

public class QueryTriplestoreForSimulinkBlocksThroughSPARQLEndpoint {

	public static void main(String[] args) {
		
		// Create a new query
		String queryString = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"PREFIX simulink: <http://www.mathworks.com/products/simulink/> " +
				"SELECT ?simulinkBlock " +
				"WHERE {" +
				"    ?simulinkBlock  rdf:type simulink:Block . " +			
				"      }";

		Query query = QueryFactory.create(queryString);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.sparqlService(
				"http://localhost:3030/mydataset/query", query);
		ResultSet results = qe.execSelect();

		// Output query results
		ResultSetFormatter.out(System.out, results, query);

		// Important - free up resources used running the query
		qe.close();
	}
}
