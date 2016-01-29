package test;


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

import org.eclipse.lyo.oslc4j.core.exception.OslcCoreApplicationException;
import org.eclipse.lyo.oslc4j.provider.jena.ErrorHandler;
import org.eclipse.lyo.oslc4j.provider.jena.JenaModelHelper;
import org.eclipse.lyo.oslc4j.provider.jena.JenaProvidersRegistry;
import org.eclipse.lyo.oslc4j.provider.jena.OslcRdfXmlCollectionProvider;
import org.eclipse.lyo.oslc4j.provider.jena.OslcRdfXmlProvider;
import org.glassfish.jersey.client.ClientConfig;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;

//import edu.gatech.mbse.oslc4jsimulink.resources.SimulinkParameter;




public class GETSimulinkParameter {

	

	public static void main(String[] args) {

		String baseHTTPURI = "http://localhost:8181/oslc4jsimulink";
		String projectId = "model11";

		String requirementURI = baseHTTPURI + "/services/"
				+ projectId + "/parameters/Constant::Value";

		// get the parameter / perform a GET
		System.out.println("Performing HTTP GET on resource "
				+ requirementURI);

		// get parameter in HTML
		Client client = ClientBuilder.newClient();
		String requirementHTML = client
				.target(requirementURI)
				.request(MediaType.TEXT_HTML).get(String.class);
		client.close();
		System.out.println("Requirement resource in HTML"
				+ requirementURI + ": "
				+ requirementHTML);
		
		// get parameter as POJO
		ClientConfig clientConfig = new ClientConfig();
		for (Class providerClass : JenaProvidersRegistry.getProviders()) {
			clientConfig.register(providerClass);
		}
		Client rdfclient = ClientBuilder.newClient(clientConfig);
		Response response = rdfclient.target(requirementURI)
				.request("application/rdf+xml").get();
		System.out.println(response.getStatus());
		edu.gatech.mbsec.adapter.simulink.resources.SimulinkParameter integrityRequirement = response
				.readEntity(edu.gatech.mbsec.adapter.simulink.resources.SimulinkParameter.class);
		System.out.println(integrityRequirement.getName());
		
		Object[] objects = new Object[] { integrityRequirement };
		
		
//		Model model;
//		try {
//			model = JenaModelHelper.createJenaModel(objects);
//			RDFWriter writer = writer = model.getWriter("RDF/XML");
//	        writer.setProperty("showXmlDeclaration",
//	                           "false");
//	        writer.setErrorHandler(new ErrorHandler());    
//	        OutputStream outputStream = new FileOutputStream("integrity_requirement.rdf");
//	      
////	        	String xmlDeclaration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
////	        	outputStream.write(xmlDeclaration.getBytes());
//	       
//	        
//	        writer.write(model,
//	                     outputStream,
//	                     null);
//		} catch (IllegalAccessException | IllegalArgumentException
//				| InvocationTargetException | DatatypeConfigurationException
//				| OslcCoreApplicationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		
	}
	
}
