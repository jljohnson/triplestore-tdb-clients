package test;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;

public class QueryTriplestore {

	public static void main(String[] args) {
		
		// load model from triplestore
		String directory = "C:\\Users\\Axel\\git\\triplestore\\triplestore\\mytriplestore4";
		Dataset dataset = TDBFactory.createDataset(directory);
		Model model = dataset.getDefaultModel();
		
		Map<Resource, Collection<PropertyObjectPair>> resourceValueMap = new HashMap<Resource, Collection<PropertyObjectPair>>();
		
		
		StmtIterator statementsIT = model.listStatements();
		while(statementsIT.hasNext()){
			Statement statement = statementsIT.next();
//			if(!statement.isReified()){		// does not work
				Resource subject = statement.getSubject();
				Property property = statement.getPredicate();
				RDFNode object = statement.getObject();
				
//				if(!(property.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") & object.toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement"))){
				if(subject.getURI() != null){	
				
				
				PropertyObjectPair propertyObjectPair = new PropertyObjectPair(property, object);
				if(!resourceValueMap.containsKey(subject)){
					Collection<PropertyObjectPair> newPropertyObjectPairs = new ArrayList<PropertyObjectPair>();
					newPropertyObjectPairs.add(propertyObjectPair);
					resourceValueMap.put(subject, newPropertyObjectPairs);
				}
				else{
					Collection<PropertyObjectPair> propertyObjectPairs = resourceValueMap.get(subject);
					propertyObjectPairs.add(propertyObjectPair);
				}
			}
//			else{
//				
//			}
			
			// map between resoources and key-value pairs
		}
		
		// traverse all properties of resources
		for (Resource resource : resourceValueMap.keySet()) {
			System.out.println("");
			System.out.println("********************");
			System.out.println(resource.getURI());
			Collection<PropertyObjectPair> propertyObjectPairs = resourceValueMap.get(resource);
			
			
			// test RDF serialization
			Model resourceRDFModel = ModelFactory.createDefaultModel();
						
						
			for (PropertyObjectPair propertyObjectPair : propertyObjectPairs) {
				Property property = propertyObjectPair.getProperty();
				RDFNode object = propertyObjectPair.getObject();
				System.out.println(property.getURI() + " -> " + object.toString());
				
				resourceRDFModel.add(resource, property, object);
				
			}
			
			// write the RDF
			resourceRDFModel.setNsPrefix( "integrity_requirement", "http://www.ptc.com/solutions/application-lifecycle-management/Requirement/" );
			resourceRDFModel.write( System.out, null);
			
		}
		
		
		
		
		
		
	}
}
