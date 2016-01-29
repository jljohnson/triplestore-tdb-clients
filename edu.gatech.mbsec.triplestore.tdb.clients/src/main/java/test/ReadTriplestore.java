package test;


import java.io.InputStream;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;

public class ReadTriplestore {

	public static void main(String[] args) {
		String directory = "c:\\Users\\Axel\\git\\oslc4jintegrity\\oslc4jintegrity\\triplestore\\tdb";
		Dataset dataset = TDBFactory.createDataset(directory);
		Model model = dataset.getDefaultModel();

		// write it to standard out
		model.write(System.out, "RDF/XML-ABBREV");

	}

}
