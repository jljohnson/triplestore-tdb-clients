package threads;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.xml.datatype.DatatypeConfigurationException;



import org.eclipse.lyo.adapter.amesim.resources.AMESimComponent;
import org.eclipse.lyo.adapter.amesim.resources.AMESimParameter;
import org.eclipse.lyo.oslc4j.core.exception.OslcCoreApplicationException;
import org.eclipse.lyo.oslc4j.core.model.AbstractResource;
import org.eclipse.lyo.oslc4j.core.model.QueryCapability;
import org.eclipse.lyo.oslc4j.core.model.Service;
import org.eclipse.lyo.oslc4j.core.model.ServiceProvider;
import org.eclipse.lyo.oslc4j.core.model.ServiceProviderCatalog;
import org.eclipse.lyo.oslc4j.provider.jena.JenaModelHelper;
import org.eclipse.lyo.oslc4j.provider.jena.JenaProvidersRegistry;
import org.glassfish.jersey.client.ClientConfig;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

import tdb.clients.AMESimAdapterAndTDBSubversionSyncClient;
import util.TriplestoreUtil;

public class AddAllAMESimParametersToTriplestoreThread extends Thread{

	String fileName;
	
	public AddAllAMESimParametersToTriplestoreThread(String fileName){
		this.fileName = fileName;
	}
	
	public static void main(String[] args){
		String fileName = "SUV_Example";
		Thread thread = new AddAllAMESimParametersToTriplestoreThread(fileName);
		thread.start();
		try {
			thread.join();
			System.out.println("All triples of file " + fileName + " added to triplestore");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void start() {
		String directory = TriplestoreUtil.getTriplestoreLocation();
		Dataset dataset = TDBFactory.createDataset(directory);
		
		ClientConfig clientConfig = new ClientConfig();
		for (Class providerClass : JenaProvidersRegistry.getProviders()) {
			clientConfig.register(providerClass);
		}
		Client rdfclient = ClientBuilder.newClient(clientConfig);
		System.out.println(AMESimAdapterAndTDBSubversionSyncClient.oslcServiceProviderCatalogURI);
		Response response = rdfclient.target(AMESimAdapterAndTDBSubversionSyncClient.oslcServiceProviderCatalogURI).request("application/rdf+xml").get();
		System.out.println(response.getStatus());
		ServiceProviderCatalog serviceProviderCatalog = response.readEntity(ServiceProviderCatalog.class);			
		
		// list to collect all resources
		ArrayList<AbstractResource> oslcResourcesArrayList = new ArrayList<AbstractResource>();
		
		for (ServiceProvider serviceProvider : serviceProviderCatalog.getServiceProviders()) {
//			System.out.println("serviceProvider " + serviceProvider.getAbout());
			if(serviceProvider.getAbout().toString().endsWith("/serviceProviders/" + fileName)){
				for (Service service : serviceProvider.getServices()) {
					for (QueryCapability queryCapability : service.getQueryCapabilities()) {					
						if(queryCapability.getQueryBase().toString().endsWith("/parameters")){	//association blocks can be retrieved if just "blocks"
							Response queryCapabilityResponse = rdfclient.target(queryCapability.getQueryBase()).request("application/rdf+xml").get();
							System.out.println(queryCapability.getQueryBase());
							System.out.println(queryCapabilityResponse.getStatus());
							AMESimParameter[] oslcResources = queryCapabilityResponse.readEntity(AMESimParameter[].class);
							oslcResourcesArrayList.addAll(Arrays.asList(oslcResources));
						}
						
						
					}
				}
			}
			
		}
		
		
		
		Object[] objects = oslcResourcesArrayList.toArray();

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

}