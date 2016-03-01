package tdb.clients.sync.magicdraw.withrevision;

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

public class QueryTriplestoreForMagicDrawBlockOfSpecificModelWithRevision {

	public static void main(String[] args) {
		
		// load model from triplestore
		String directory = TriplestoreUtil.getTriplestoreLocation();
		Dataset dataset = TDBFactory.createDataset(directory);
		Model model = dataset.getDefaultModel();
		
		
		
		// Create a new query
		String queryString = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX sysml: <http://localhost:8080/oslc4jmagicdraw/services/sysml-rdfvocabulary#> " +
			"PREFIX sysml_namedelement: <http://localhost:8080/oslc4jmagicdraw/services/sysml-rdfvocabulary#NamedElement/> " +
//			"SELECT ?magicdrawResource " +
			"SELECT DISTINCT ?magicdrawResource ?name " +
			"WHERE {" +
//			"    ?magicdrawResource  ?p ?o. " +	
//			"FILTER ( regex(str(?magicdrawResource), \"/services/httpskoneksys118080svnrepository3test1test2test3---sldemo_househeat\") ) " +
//			"FILTER ( regex(str(?magicdrawResource), \"---revision8\") ) " +
//			"FILTER ( regex(str(?magicdrawResource), \"/services/httpskoneksys118080svnrepository3test1test2test3---sldemo_househeat\") ) " +
			"    ?magicdrawResource  sysml:NamedElement_name ?name . " +
			"FILTER ( regex(str(?magicdrawResource), \"---Wired_Camera_Example/blocks/Blocks::Camera\") ) " +
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
