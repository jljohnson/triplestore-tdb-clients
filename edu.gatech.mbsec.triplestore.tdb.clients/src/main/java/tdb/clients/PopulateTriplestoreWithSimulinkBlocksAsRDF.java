package tdb.clients;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import util.TriplestoreUtil;

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

public class PopulateTriplestoreWithSimulinkBlocksAsRDF {

	public static void main(String[] args) {		
		Thread thread = new Thread(){
			public void start(){
				// create TDB dataset
				String directory = TriplestoreUtil.getTriplestoreLocation();
				Dataset dataset = TDBFactory.createDataset(directory);

				// populate model of TDB dataset with PTC Integrity requirements of RDF
				// file
				Model tdbModel = dataset.getDefaultModel();		
				File file = new File("sample rdf/simulink_blocks.rdf");		
				String source = "file:" + file.getAbsolutePath();
				
				FileManager.get().readModel(tdbModel, source);
				tdbModel.close();
				dataset.close();
			}
		};
		thread.start();		
		 try {
			thread.join();
			System.out.println("Simulink Blocks added to triplestore");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
