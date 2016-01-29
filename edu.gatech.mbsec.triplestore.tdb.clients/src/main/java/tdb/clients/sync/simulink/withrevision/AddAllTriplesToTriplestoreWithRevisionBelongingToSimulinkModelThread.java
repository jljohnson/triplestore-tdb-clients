package tdb.clients.sync.simulink.withrevision;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.xml.datatype.DatatypeConfigurationException;

import org.eclipse.lyo.adapter.simulink.resources.SimulinkBlock;
import org.eclipse.lyo.adapter.simulink.resources.SimulinkInputPort;
import org.eclipse.lyo.adapter.simulink.resources.SimulinkLine;
import org.eclipse.lyo.adapter.simulink.resources.SimulinkModel;
import org.eclipse.lyo.adapter.simulink.resources.SimulinkOutputPort;
import org.eclipse.lyo.adapter.simulink.resources.SimulinkParameter;
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

import tdb.clients.SimulinkAdapterAndTDBSubversionSyncClient;
import util.TriplestoreUtil;

public class AddAllTriplesToTriplestoreWithRevisionBelongingToSimulinkModelThread extends Thread{

	String fileName;
	String revision;
	
	public AddAllTriplesToTriplestoreWithRevisionBelongingToSimulinkModelThread(String fileName, String revision){
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
		System.out.println(SimulinkAdapterAndTDBSubversionSyncClientWithRevision.oslcServiceProviderCatalogURI);
		Response response = rdfclient.target(SimulinkAdapterAndTDBSubversionSyncClientWithRevision.oslcServiceProviderCatalogURI).request("application/rdf+xml").get();
		System.out.println(response.getStatus());
		ServiceProviderCatalog serviceProviderCatalog = response.readEntity(ServiceProviderCatalog.class);			
		
		
		
		
		
		// list to collect all Simulink resources
		ArrayList<AbstractResource> oslcResourcesArrayList = new ArrayList<AbstractResource>();
		
		for (ServiceProvider serviceProvider : serviceProviderCatalog.getServiceProviders()) {
			System.out.println("serviceProvider " + serviceProvider.getAbout());
			if(serviceProvider.getAbout().toString().endsWith("/serviceProviders/" + fileName)){
								
				
				
				
				for (Service service : serviceProvider.getServices()) {
					for (QueryCapability queryCapability : service.getQueryCapabilities()) {					
						System.out.println(queryCapability.getQueryBase());
						Response queryCapabilityResponse = rdfclient.target(queryCapability.getQueryBase()).request("application/rdf+xml").get();
						System.out.println(queryCapabilityResponse.getStatus());
						if(queryCapability.getQueryBase().toString().endsWith("blocks")){
							SimulinkBlock[] oslcResources = queryCapabilityResponse.readEntity(SimulinkBlock[].class);
							oslcResourcesArrayList.addAll(Arrays.asList(getResourcesWithVersion(oslcResources, revision)));
						}
						else if(queryCapability.getQueryBase().toString().endsWith("lines")){
							SimulinkLine[] oslcResources = queryCapabilityResponse.readEntity(SimulinkLine[].class);
							oslcResourcesArrayList.addAll(Arrays.asList(getResourcesWithVersion(oslcResources, revision)));
						}
						else if(queryCapability.getQueryBase().toString().endsWith("parameters")){
							SimulinkParameter[] oslcResources = queryCapabilityResponse.readEntity(SimulinkParameter[].class);
							oslcResourcesArrayList.addAll(Arrays.asList(getResourcesWithVersion(oslcResources, revision)));
						}
						else if(queryCapability.getQueryBase().toString().endsWith("outputports")){
							SimulinkOutputPort[] oslcResources = queryCapabilityResponse.readEntity(SimulinkOutputPort[].class);
							oslcResourcesArrayList.addAll(Arrays.asList(getResourcesWithVersion(oslcResources, revision)));
						}
						else if(queryCapability.getQueryBase().toString().endsWith("inputports")){
							SimulinkInputPort[] oslcResources = queryCapabilityResponse.readEntity(SimulinkInputPort[].class);
							oslcResourcesArrayList.addAll(Arrays.asList(getResourcesWithVersion(oslcResources, revision)));
						}
						else if(queryCapability.getQueryBase().toString().endsWith("model")){
							SimulinkModel oslcResource = queryCapabilityResponse.readEntity(SimulinkModel.class);
							oslcResourcesArrayList.add(getResourceWithVersion(oslcResource,revision));
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
	
	protected static AbstractResource[] getResourcesWithVersion(AbstractResource[] oslcResources, String revision) {
		AbstractResource[] oslcResourcesWithVersion = new AbstractResource[oslcResources.length];
		int i = 0;
		for (AbstractResource simulinkBlock : oslcResources) {
			simulinkBlock.setAbout(URI.create(simulinkBlock.getAbout().toString() + "---revision" + revision));
			oslcResourcesWithVersion[i] = simulinkBlock;
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