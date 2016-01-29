package test;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.hp.hpl.jena.assembler.assemblers.FileModelAssembler;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;

public class CreateAndPopulateTriplestore {

	public static void main(String[] args) {
		// create TDB dataset
		String directory = "c:\\Users\\Axel\\git\\oslc4jintegrity\\oslc4jintegrity\\triplestore\\tdb";
		Dataset dataset = TDBFactory.createDataset(directory);

		// populate model of TDB dataset with PTC Integrity requirements of RDF
		// file
		Model tdbModel = dataset.getDefaultModel();
		String source = "file:c:\\Users\\Axel\\git\\oslc4jintegrity\\oslc4jintegrity\\integrity_requirements.rdf";
		FileManager.get().readModel(tdbModel, source);
		tdbModel.close();
		dataset.close();
	}
}
