package tdb.clients.metadata;

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

public class QueryTriplestoreForResourceShapeProperties {

	public static void main(String[] args) {
		
		// load model from triplestore
		String directory = TriplestoreUtil.getTriplestoreLocation();
		Dataset dataset = TDBFactory.createDataset(directory);
		Model model = dataset.getDefaultModel();
		

	    
		
		// Create a new query
		String queryString = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX oslc: <http://open-services.net/ns/core#> " +
			"PREFIX sysml_namedelement: <http://www.omg.org/sysml/NamedElement/> " +
			"SELECT DISTINCT ?resourceShape ?propertyDefinition ?propertyOccurs " +
			"WHERE {" +
			"    ?resourceShape  rdf:type oslc:ResourceShape . " +
			"    ?resourceShape  oslc:property ?propertyOfResourceShape . " +
			"    ?propertyOfResourceShape  oslc:propertyDefinition ?propertyDefinition . " +
			"    ?propertyOfResourceShape  oslc:occurs ?propertyOccurs . " +
//			"FILTER ( regex(str(?resourceShape), \"resourceShapes/block\") ) " +
//			"FILTER ( regex(str(?propertyDefinition), \"sysml/Block\") ) " +
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
