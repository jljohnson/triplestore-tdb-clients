package tdb.clients.sync.amesim.withrevision;

import java.io.InputStream;

import util.TriplestoreUtil;

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

public class QueryTriplestoreForAMESimParameterOfSpecificModelWithRevision {

	public static void main(String[] args) {
		
		// load model from triplestore
		String directory = TriplestoreUtil.getTriplestoreLocation();
		Dataset dataset = TDBFactory.createDataset(directory);
		Model model = dataset.getDefaultModel();
		
		
		
		// Create a new query
		String queryString = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX amesim: <http://www.lmsintl.com/LMS-Imagine-Lab-AMESim/> " +
			"PREFIX amesim_parameter: <http://www.lmsintl.com/LMS-Imagine-Lab-AMESim/Parameter/> " +
//			"SELECT ?amesimResource " +
			"SELECT ?amesimResource ?value " +
			"WHERE {" +
//			"    ?amesimResource  ?p ?o. " +	
//			"FILTER ( regex(str(?amesimResource), \"/services/httpskoneksys118080svnrepository3test1test2test3---sldemo_househeat\") ) " +
//			"FILTER ( regex(str(?amesimResource), \"---revision7\") ) " +
//			"FILTER ( regex(str(?amesimResource), \"/services/httpskoneksys118080svnrepository3test1test2test3---sldemo_househeat\") ) " +
			"    ?amesimResource  amesim_parameter:value ?value . " +
			"FILTER ( regex(str(?amesimResource), \"FlatTwin/parameters/length::simple_crank\") ) " +
			"      }";
		Query query = QueryFactory.create(queryString);

		
		
		
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();

		// Output query results	
		ResultSetFormatter.out(System.out, results, query);

		// Important - free up resources used running the query
		qe.close();		
	}
}
