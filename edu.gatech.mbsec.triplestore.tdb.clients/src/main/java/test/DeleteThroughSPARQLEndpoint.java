package test;


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

public class DeleteThroughSPARQLEndpoint {

	public static void main(String[] args) {

		// Create a new update
		String deleteString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX integrity: <http://www.ptc.com/solutions/application-lifecycle-management/> "
				+ "PREFIX project102_requirement: <http://localhost:8383/oslc4jintegrity/services/project102/requirements/> "
				+ "DELETE"
				+ "{project102_requirement:NewReq2 ?p ?o .}" 
				+ "WHERE"
				+ "{project102_requirement:NewReq2 ?p ?o .}";

		UpdateRequest updateRequest = UpdateFactory.create(deleteString);

		// Execute the query and obtain results
		UpdateProcessor updateProcessor = UpdateExecutionFactory.createRemote(
				updateRequest, "http://localhost:3030/requirements/update");
		updateProcessor.execute();				
	}
}
