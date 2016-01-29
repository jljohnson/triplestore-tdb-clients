package tdb.clients.sync.simulink.withrevision;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.datatype.DatatypeConfigurationException;

import edu.gatech.mbsec.adapter.simulink.resources.SimulinkBlock;
import org.eclipse.lyo.adapter.subversion.SubversionFile;
import org.eclipse.lyo.oslc4j.core.exception.OslcCoreApplicationException;
import org.eclipse.lyo.oslc4j.provider.jena.ErrorHandler;
import org.eclipse.lyo.oslc4j.provider.jena.JenaModelHelper;
import org.eclipse.lyo.oslc4j.provider.jena.JenaProvidersRegistry;
import org.eclipse.lyo.oslc4j.provider.jena.OslcRdfXmlCollectionProvider;
import org.eclipse.lyo.oslc4j.provider.jena.OslcRdfXmlProvider;
import org.glassfish.jersey.client.ClientConfig;

import util.TriplestoreUtil;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;

public class GETSimulinkBlocksAndPopulateTriplestoreWithRevision {

	public static void main(String[] args) {
		Thread thread = new Thread() {
			public void start() {
				// create TDB dataset
				String directory = TriplestoreUtil.getTriplestoreLocation();
				Dataset dataset = TDBFactory.createDataset(directory);

				String baseHTTPURI = "http://localhost:" + SimulinkAdapterAndTDBSubversionSyncClientWithRevision.port + "/oslc4jsimulink";
				String projectId = "httpskoneksys118080svnrepository2---sldemo_househeat";

				String blocksURI = baseHTTPURI + "/services/" + projectId + "/blocks";

				
				
				
				// get requirements as POJOs
				ClientConfig clientConfig = new ClientConfig();
				for (Class providerClass : JenaProvidersRegistry.getProviders()) {
					clientConfig.register(providerClass);
				}
				
				Client rdfclient = ClientBuilder.newClient(clientConfig);
				
				
				// get model version
				String projectName = projectId.split("---")[1];
				String subversionFileURI = baseHTTPURI + "/services/subversionfiles/" + projectName + ".slx";
				Response subversionFileResponse = rdfclient.target(subversionFileURI).request("application/rdf+xml").get();
				SubversionFile subversionFileResource = subversionFileResponse.readEntity(SubversionFile.class);
				String revision = subversionFileResource.getRevision();
				System.out.println("revision: " + revision);
				
				// get blocks
				Response response = rdfclient.target(blocksURI).request("application/rdf+xml").get();
				System.out.println(response.getStatus());
				SimulinkBlock[] oslcResources = response.readEntity(SimulinkBlock[].class);
				
				SimulinkBlock[] oslcResourcesWithVersion = new SimulinkBlock[oslcResources.length];
				int i = 0;
				for (SimulinkBlock simulinkBlock : oslcResources) {
					simulinkBlock.setAbout(URI.create(simulinkBlock.getAbout().toString() + "---revision" + revision));
					oslcResourcesWithVersion[i] = simulinkBlock;
					i++;
				}
				
				
				Object[] objects = oslcResources;

				Model model;
				Model tdbModel = dataset.getDefaultModel();
				try {
					model = JenaModelHelper.createJenaModel(objects);
					tdbModel.add(model);
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (DatatypeConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OslcCoreApplicationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
