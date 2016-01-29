package adapter.clients;

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

import org.eclipse.lyo.adapter.magicdraw.resources.SysMLBlock;
import org.eclipse.lyo.adapter.simulink.resources.SimulinkBlock;
import org.eclipse.lyo.oslc4j.core.exception.OslcCoreApplicationException;
import org.eclipse.lyo.oslc4j.core.model.AbstractResource;
import org.eclipse.lyo.oslc4j.core.model.AnyResource;
import org.eclipse.lyo.oslc4j.provider.jena.ErrorHandler;
import org.eclipse.lyo.oslc4j.provider.jena.JenaModelHelper;
import org.eclipse.lyo.oslc4j.provider.jena.JenaProvidersRegistry;
import org.eclipse.lyo.oslc4j.provider.jena.OslcRdfXmlCollectionProvider;
import org.eclipse.lyo.oslc4j.provider.jena.OslcRdfXmlProvider;
import org.glassfish.jersey.client.ClientConfig;

import edu.gatech.mbsec.adapter.integrity.generated.resources.IntegrityProductRequirementDocument;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;




public class GETAbstractResourceTest {

	public static void main(String[] args) {
		Thread thread = new Thread() {
			public void start() {
				String baseHTTPURI = "http://localhost:8484/oslc4jintegrity";
				String projectId = "project2883__xxxxx__Mannheim_POC_Sample_Content";

				String requirementsURI = baseHTTPURI + "/services/"
						+ projectId + "/productrequirementdocuments/12188";

				// get requirements as POJOs
				ClientConfig clientConfig = new ClientConfig();
				for (Class providerClass : JenaProvidersRegistry.getProviders()) {
					clientConfig.register(providerClass);
				}
				Client rdfclient = ClientBuilder.newClient(clientConfig);
				Response response = rdfclient.target(requirementsURI)
						.request("application/rdf+xml").get();
				System.out.println(response.getStatus());
//				IntegrityProductRequirementDocument[] oslcResources = response
//						.readEntity(IntegrityProductRequirementDocument[].class);
				
				Object integrityResource = response
						.getEntity();
				
				
				Object[] objects = new Object[1] ;
				objects[0] = integrityResource;
				
				// print requirements in RDF file
				try {
					Model model = JenaModelHelper.createJenaModel(objects);
					RDFWriter writer = writer = model.getWriter("RDF/XML");
			        writer.setProperty("showXmlDeclaration",
			                           "false");
			        writer.setErrorHandler(new ErrorHandler());    
			        OutputStream outputStream = new FileOutputStream("sample rdf/abstract_resource.rdf");	      	       	        
			        writer.write(model,
			                     outputStream,
			                     null);
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | DatatypeConfigurationException
						| OslcCoreApplicationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		thread.start();
		try {
			thread.join();
			System.out.println("Integrity Product Requirement Documents from adapter saved as RDF");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}	
}
