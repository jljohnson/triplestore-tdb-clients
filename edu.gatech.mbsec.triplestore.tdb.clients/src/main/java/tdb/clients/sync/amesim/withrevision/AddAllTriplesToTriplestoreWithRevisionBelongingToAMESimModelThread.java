package tdb.clients.sync.amesim.withrevision;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.xml.datatype.DatatypeConfigurationException;

import edu.gatech.mbsec.adapter.amesim.resources.AMESimComponent;
import edu.gatech.mbsec.adapter.amesim.resources.AMESimLine;
import edu.gatech.mbsec.adapter.amesim.resources.AMESimParameter;
import org.eclipse.lyo.adapter.subversion.SubversionFile;
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

public class AddAllTriplesToTriplestoreWithRevisionBelongingToAMESimModelThread extends Thread{

	String fileName;
	String revision;
	
	public AddAllTriplesToTriplestoreWithRevisionBelongingToAMESimModelThread(String fileName, String revision){
		this.fileName = fileName;
		this.revision = revision;
	}
	
	public void start() {
		String directory = TriplestoreUtil.getTriplestoreLocation();
		Dataset dataset = TDBFactory.createDataset(directory);
		
		ClientConfig clientConfig = new ClientConfig();
		for (Class providerClass : JenaProvidersRegistry.getProviders()) {
			clientConfig.register(providerClass);
		}
		Client rdfclient = ClientBuilder.newClient(clientConfig);
		System.out.println(AMESimAdapterAndTDBSubversionSyncClientWithRevision.oslcServiceProviderCatalogURI);
		Response response = rdfclient.target(AMESimAdapterAndTDBSubversionSyncClientWithRevision.oslcServiceProviderCatalogURI).request("application/rdf+xml").get();
		System.out.println(response.getStatus());
		ServiceProviderCatalog serviceProviderCatalog = response.readEntity(ServiceProviderCatalog.class);			
		
		
		
		
		
		// list to collect all AMESim resources
		ArrayList<AbstractResource> oslcResourcesArrayList = new ArrayList<AbstractResource>();
		
		for (ServiceProvider serviceProvider : serviceProviderCatalog.getServiceProviders()) {
			System.out.println("serviceProvider " + serviceProvider.getAbout());
			if(serviceProvider.getAbout().toString().endsWith("/serviceProviders/" + fileName)){
								
				
				
				
				for (Service service : serviceProvider.getServices()) {
					for (QueryCapability queryCapability : service.getQueryCapabilities()) {					
						System.out.println(queryCapability.getQueryBase());
						Response queryCapabilityResponse = rdfclient.target(queryCapability.getQueryBase()).request("application/rdf+xml").get();
						System.out.println(queryCapabilityResponse.getStatus());
						if(queryCapability.getQueryBase().toString().endsWith("components")){
							AMESimComponent[] oslcResources = queryCapabilityResponse.readEntity(AMESimComponent[].class);
							oslcResourcesArrayList.addAll(Arrays.asList(getResourcesWithVersion(oslcResources, revision)));
						}
//						else if(queryCapability.getQueryBase().toString().endsWith("lines")){
//							AMESimLine[] oslcResources = queryCapabilityResponse.readEntity(AMESimLine[].class);
//							oslcResourcesArrayList.addAll(Arrays.asList(getResourcesWithVersion(oslcResources, revision)));
//						}
						else if(queryCapability.getQueryBase().toString().endsWith("parameters")){
							AMESimParameter[] oslcResources = queryCapabilityResponse.readEntity(AMESimParameter[].class);
							oslcResourcesArrayList.addAll(Arrays.asList(getResourcesWithVersion(oslcResources, revision)));
						}
//						else if(queryCapability.getQueryBase().toString().endsWith("outputports")){
//							AMESimOutputPort[] oslcResources = queryCapabilityResponse.readEntity(AMESimOutputPort[].class);
//							oslcResourcesArrayList.addAll(Arrays.asList(getResourcesWithVersion(oslcResources, revision)));
//						}
//						else if(queryCapability.getQueryBase().toString().endsWith("inputports")){
//							AMESimInputPort[] oslcResources = queryCapabilityResponse.readEntity(AMESimInputPort[].class);
//							oslcResourcesArrayList.addAll(Arrays.asList(getResourcesWithVersion(oslcResources, revision)));
//						}
//						else if(queryCapability.getQueryBase().toString().endsWith("model")){
//							AMESimModel oslcResource = queryCapabilityResponse.readEntity(AMESimModel.class);
//							oslcResourcesArrayList.add(getResourceWithVersion(oslcResource,revision));
//						}
						
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
	
	protected static AbstractResource[] getResourcesWithVersion(AbstractResource[] oslcResources, String revision) {
		AbstractResource[] oslcResourcesWithVersion = new AbstractResource[oslcResources.length];
		int i = 0;
		for (AbstractResource amesimBlock : oslcResources) {
			amesimBlock.setAbout(URI.create(amesimBlock.getAbout().toString() + "---revision" + revision));
			oslcResourcesWithVersion[i] = amesimBlock;
			i++;
		}
		return oslcResourcesWithVersion;
	}

	protected static AbstractResource getResourceWithVersion(AbstractResource oslcResource, String revision) {
		AbstractResource oslcResourceWithVersion = oslcResource;
		oslcResourceWithVersion.setAbout(URI.create(oslcResource.getAbout().toString() + "---revision" + revision));		
		return oslcResourceWithVersion;
	}
}