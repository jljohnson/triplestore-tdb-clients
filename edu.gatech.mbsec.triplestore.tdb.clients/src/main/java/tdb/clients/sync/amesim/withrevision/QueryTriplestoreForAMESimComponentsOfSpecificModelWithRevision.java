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

public class QueryTriplestoreForAMESimComponentsOfSpecificModelWithRevision {

	public static void main(String[] args) {
		
		// load model from triplestore
		String directory = TriplestoreUtil.getTriplestoreLocation();
		Dataset dataset = TDBFactory.createDataset(directory);
		Model model = dataset.getDefaultModel();
		
		
		
		// Create a new query
		String queryString = 							
			"SELECT DISTINCT ?amesimResource " +
			"WHERE {" +
			"    ?amesimResource  ?p ?o. " +	
			"FILTER ( regex(str(?amesimResource), \"/services/httpswdxcnd519309s.repos.comsvnamesimrepository---FlatTwin/components\") ) " +
			"FILTER ( regex(str(?amesimResource), \"---revision4\") ) " +
//			"FILTER ( regex(str(?simulinkResource), \"/services/httpskoneksys118080svnrepository3test1test2---model4\") ) " +
//			"    ?simulinkResource  simulink_parameter:value ?value . " +
//			"FILTER ( regex(str(?simulinkResource), \"/services/httpskoneksys118080svnrepository3test1test2test3---sldemo_househeat/parameters/House::R-value::Value\") ) " +
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
