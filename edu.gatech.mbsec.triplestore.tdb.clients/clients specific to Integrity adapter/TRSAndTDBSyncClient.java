package fuseki.clients;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.datatype.DatatypeConfigurationException;

import edu.gatech.mbsec.adapter.integrity.generated.resources.IntegrityItem;
import edu.gatech.mbsec.adapter.integrity.generated.resources.IntegrityProductRequirement;
import edu.gatech.mbsec.adapter.integrity.generated.resources.IntegrityProductRequirementDocument;
import edu.gatech.mbsec.adapter.integrity.resources.trs.Base;
import edu.gatech.mbsec.adapter.integrity.resources.trs.ChangeLog;
import edu.gatech.mbsec.adapter.integrity.resources.trs.ChangeEvent;
import edu.gatech.mbsec.adapter.integrity.resources.trs.TrackedResourceSet;

import org.eclipse.lyo.oslc4j.core.annotation.OslcPropertyDefinition;
import org.eclipse.lyo.oslc4j.core.exception.OslcCoreApplicationException;
import org.eclipse.lyo.oslc4j.core.model.AbstractResource;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.eclipse.lyo.oslc4j.provider.jena.ErrorHandler;
import org.eclipse.lyo.oslc4j.provider.jena.JenaModelHelper;
import org.eclipse.lyo.oslc4j.provider.jena.JenaProvidersRegistry;
import org.glassfish.jersey.client.ClientConfig;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import com.mks.api.response.Field;
import com.mks.api.response.Item;
import com.mks.api.response.ItemList;

public class TRSAndTDBSyncClient {

	static String fusekiServerURI = "http://localhost:3030/";
	static String triplestoreDatasetName = "mydataset";
	static String fusekiEndpointURI = fusekiServerURI + triplestoreDatasetName;
	static int delayInSecondsBetweenChangeChecks = 10;
	static int mostRecentlyProcessedChangeEventOrder = 0;
	static URI lastKnownBaseCutOffEvent = null;
	static String baseHTTPURI = "http://localhost:8484/oslc4jintegrity";

	// processedChangeEvents

	public static void main(String[] args) {
		checkIntegrityTRSAndUpdateTDBPeriodically();

		// check order of last processed event

		// check if basecutoff date has not changed!!
	}

	private static void checkIntegrityTRSAndUpdateTDBPeriodically() {
		mostRecentlyProcessedChangeEventOrder = 0;
		lastKnownBaseCutOffEvent = URI
				.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil");
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				checkIntegrityTRSAndUpdateTDB();
			}
		}, 0, delayInSecondsBetweenChangeChecks * 1000);

	}

	public static void checkIntegrityTRSAndUpdateTDB() {

		String trsURI = baseHTTPURI + "/services/" + "trs";

		// get the trs / perform a GET
		String date = new Date().toString();
		System.out.println("");
		System.out.println("Checking the OSLC Integrity TRS at " + date);
		System.out.println("Performing HTTP GET on TRS resource " + trsURI);

		// get trs as POJO
		ClientConfig clientConfig = new ClientConfig();
		for (Class providerClass : JenaProvidersRegistry.getProviders()) {
			clientConfig.register(providerClass);
		}
		Client rdfclient = ClientBuilder.newClient(clientConfig);
		Response response = rdfclient.target(trsURI)
				.request("application/rdf+xml").get();
		// System.out.println(response.getStatus());
		TrackedResourceSet trackedResourceSet = response
				.readEntity(TrackedResourceSet.class);

		// check that there is no new base cutoff event
		URI baseURI = trackedResourceSet.getBase();
		Response baseResponse = rdfclient.target(baseURI)
				.request("application/rdf+xml").get();
		Base base = baseResponse.readEntity(Base.class);
		// Base base = (Base)baseResponse.getEntity();
		URI baseCutoffEventURI = base.getCutoffEvent();
		if (!lastKnownBaseCutOffEvent.toString().equals(
				baseCutoffEventURI.toString())) {
			lastKnownBaseCutOffEvent = baseCutoffEventURI;
			// initialize changeEvent order counter
			mostRecentlyProcessedChangeEventOrder = 0;
		}

		// get the changeLog resource
		ChangeLog changeLog = trackedResourceSet.getChangeLog();

		// collect all changeEvents
		List<ChangeEvent> changeEvents = new ArrayList<ChangeEvent>();
		getChangeEventsFromChangeLogs(changeEvents, changeLog, rdfclient);

		// process change events
		processChangeEvents(changeEvents, rdfclient);

	}

	private static void getChangeEventsFromChangeLogs(
			List<ChangeEvent> changeEvents, ChangeLog changeLog,
			Client rdfclient) {

		List<ChangeEvent> changeEventFromChangeLog = changeLog.getChanges();
		changeEvents.addAll(changeEventFromChangeLog);
		// handle subsequent changeLog resources if they exist (previous !=
		// null)
		if (changeLog.getPrevious() != null) {
			URI previousChangeLogURI = changeLog.getPrevious();
			Response previousChangeLogResponse = rdfclient
					.target(previousChangeLogURI)
					.request("application/rdf+xml").get();
			ChangeLog previousChangeLog = previousChangeLogResponse
					.readEntity(ChangeLog.class);
			// ChangeLog previousChangeLog =
			// (ChangeLog)previousChangeLogResponse
			// .getEntity();
			getChangeEventsFromChangeLogs(changeEvents, previousChangeLog,
					rdfclient);
		}

	}

	private static void processChangeEvents(List<ChangeEvent> changeEvents,
			Client rdfclient) {
		// check if there is any changeEvent
		if (changeEvents.size() == 0)
			return;

		// check if most recent change event has already been processed
		// get order of most recent change order
		Collections.sort(changeEvents, new Comparator<ChangeEvent>() {
			@Override
			public int compare(ChangeEvent o1, ChangeEvent o2) {
				int diff = o2.getOrder() - o1.getOrder();
				return diff;
			}
		});
		ChangeEvent mostRecentChangeEvent = changeEvents.get(0);
		int mostRecentChangeEventOrder = mostRecentChangeEvent.getOrder();
		if (mostRecentChangeEventOrder == mostRecentlyProcessedChangeEventOrder)
			return;

		// get reorder change events (most recent is last, first is oldest)
		Collections.sort(changeEvents, new Comparator<ChangeEvent>() {
			@Override
			public int compare(ChangeEvent o1, ChangeEvent o2) {
				int diff = o1.getOrder() - o2.getOrder();
				return diff;
			}
		});

		try {
			FileWriter logFile = new FileWriter("TRS-TDB-Sync-Log.txt");
			StringBuffer loggingBuffer = new StringBuffer();

			for (ChangeEvent changeEvent : changeEvents) {

				// skip to next change event if already processed
				if (changeEvent.getOrder() <= mostRecentlyProcessedChangeEventOrder) {
					continue;
				} else {
					mostRecentlyProcessedChangeEventOrder = changeEvent
							.getOrder();
				}

				// type of change event
				String changeEventType = null;
				for (URI changeEventTypeURI : changeEvent.getTypes()) {
					if (changeEventTypeURI.toString().startsWith(
							"http://open-services.net/ns/core/trs#")) {
						changeEventType = changeEventTypeURI
								.toString()
								.replace(
										"http://open-services.net/ns/core/trs#",
										"");
						break;
					}
				}
				if (changeEventType == null)
					continue;

				// changed resource of change event
				String changedResourceURI = changeEvent.getChanged().toString();

				// timestamp of change event
				String timestamp = changeEvent.getAbout().toString()
						.replace(baseHTTPURI + "/changeevents/", "");

				// logging
				System.out.println("\n*** CHANGE EVENT ***");
				System.out.println("\t" + changeEventType);
				System.out.println("\tChanged Resource: " + changedResourceURI);
				System.out.println("\tTimestamp: " + timestamp);
				loggingBuffer.append("*** CHANGE EVENT ***");
				loggingBuffer.append("\r\n\t" + changeEventType);
				loggingBuffer.append("\r\n\tChanged Resource: "
						+ changedResourceURI);
				loggingBuffer.append("\r\n\tTimestamp: " + timestamp);
				logFile.append(loggingBuffer);

				// processing of change event: update triplestore accordingly

				// GET the changed/created resource from OSLC Integrity Adapter
				Response changedResourceResponse = rdfclient
						.target(changedResourceURI)
						.request("application/rdf+xml").get();
				System.out.println(changedResourceResponse.getStatus());

				// temporary solution
				AbstractResource integrityResource;
				if (changedResourceURI.toString().contains(
						"productrequirementdocuments")) {
					integrityResource = changedResourceResponse
							.readEntity(IntegrityProductRequirementDocument.class);
				} else if (changedResourceURI.toString().contains(
						"productrequirements")) {
					integrityResource = changedResourceResponse
							.readEntity(IntegrityProductRequirement.class);
				} else if (changedResourceURI.toString().contains("items")) {
					integrityResource = changedResourceResponse
							.readEntity(IntegrityItem.class);
				} else {
					return;
				}

				if (changeEventType.equals("Creation")) {
					// create and execute SPARQL Update to Insert Data into
					// triplestore
					createAndExecuteSPARQLInsertDataUpdate(
							integrityResource.getAbout(), integrityResource,
							loggingBuffer);

				} else if (changeEventType.equals("Deletion")) {
					// create and execute SPARQL Update to Delete Data
					createAndExecuteSPARQLDeleteDataUpdate(changedResourceURI,
							loggingBuffer);
				} else if (changeEventType.equals("Modification")) {
					// create and execute SPARQL Update to Delete Data in
					// triplestore
					createAndExecuteSPARQLDeleteDataUpdate(changedResourceURI,
							loggingBuffer);
					// create and execute SPARQL Update to Insert Data into
					// triplestore
					createAndExecuteSPARQLInsertDataUpdate(
							integrityResource.getAbout(), integrityResource,
							loggingBuffer);
				}
			}
			logFile.append(loggingBuffer);
			logFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void createAndExecuteSPARQLDeleteDataUpdate(
			String changedResourceURI, StringBuffer loggingBuffer) {
		// Create a new update
		loggingBuffer.append("\n*** SPARQL DELETE DATA Update ***");
		String deleteString = "DELETE" + "{<" + changedResourceURI
				+ "> ?p ?o .}" + "WHERE" + "{<" + changedResourceURI
				+ "> ?p ?o .}";

		UpdateRequest updateRequest = UpdateFactory.create(deleteString);
		// Execute the query and obtain results
		UpdateProcessor updateProcessor = UpdateExecutionFactory.createRemote(
				updateRequest, fusekiEndpointURI + "/update");
		updateProcessor.execute();
	}

	private static void createAndExecuteSPARQLInsertDataUpdate(URI resourceURI,
			Object object, StringBuffer loggingBuffer) {
		loggingBuffer.append("\n*** SPARQL INSERT DATA Update ***");
		StringBuffer sparqlUpdateStringBuffer = new StringBuffer();

		// go through resource properties through reflection and create
		// corresponding triples in Turtle notation
		sparqlUpdateStringBuffer.append("\nINSERT DATA");
		sparqlUpdateStringBuffer.append("\n{");
		iterateThroughFields(resourceURI, object, sparqlUpdateStringBuffer);
		sparqlUpdateStringBuffer.append("\n}");

		// send a SPARQL query to the Fuseki server of the
		// triplestore
		// to add the new resource from PTC Integrity to the
		// triplestore
		UpdateRequest updateRequest = UpdateFactory
				.create(sparqlUpdateStringBuffer.toString());

		// Execute the query and obtain results
		UpdateProcessor updateProcessor = UpdateExecutionFactory.createRemote(
				updateRequest, fusekiEndpointURI + "/update");
		updateProcessor.execute();

	}

	private static void iterateThroughFields(URI resourceURI, Object object,
			StringBuffer updateQueryBuffer) {

		// add triple describing the rdf:type property of the requirement (Note:
		// integrity_requirement:type property of Integrity requirement is of
		// type string)

		// get resource types
		// getTypes() method has no OSLC annotations
		Collection<URI> resourceTypes = new ArrayList<URI>();
		Method getTypesMethod = null;
		for (Method method : object.getClass().getMethods()) {
			if (method.getName().equals("getTypes")) {
				getTypesMethod = method;
				break;
			}
		}
		if (getTypesMethod != null) {
			try {
				Object objectURI = getTypesMethod.invoke(object, null);
				resourceTypes = (Collection<URI>) objectURI;
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// getRdfTypes (common MBSE types)

		Method getRDFTypesMethod = null;
		for (Method method : object.getClass().getMethods()) {
			if (method.getName().equals("getRdfTypes")) {
				getRDFTypesMethod = method;
				break;
			}
		}
		if (getRDFTypesMethod != null) {
			try {
				Object objectURI = getRDFTypesMethod.invoke(object, null);
				URI[] resourceTypesArray = (URI[]) objectURI;
				resourceTypes.addAll(Arrays.asList(resourceTypesArray));
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		for (URI resourceTypeURI : resourceTypes) {
			updateQueryBuffer.append("\n<" + resourceURI + "> <"
					+ "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" + "> ");
			updateQueryBuffer.append("<" + resourceTypeURI.toString() + ">");
			updateQueryBuffer.append(" .");
		}

		for (java.lang.reflect.Field field : object.getClass()
				.getDeclaredFields()) {
			String fieldName = field.getName();
			String upperCaseFieldName = fieldName.substring(0, 1).toUpperCase()
					+ fieldName.substring(1, fieldName.length());

			String fieldURI = null;
			Method getterMethod = null;
			// try to find equivalent property
			for (Method method : object.getClass().getDeclaredMethods()) {
				if (method.getName().equals("get" + upperCaseFieldName)) {
					OslcPropertyDefinition propDefAnnotation = method
							.getAnnotation(OslcPropertyDefinition.class);
					fieldURI = propDefAnnotation.value();
					getterMethod = method;
					break;
				}
			}
			if (fieldURI == null)
				continue;
			try {

				// check that the field has a value
				if (field.getType() != null) {
					if (field.getType().getName().equals("java.util.Set")) {
						Object fieldValue;
						fieldValue = getterMethod.invoke(object, null);
						if (fieldValue instanceof Link[]) {
							Link[] links = (Link[]) fieldValue;
							if (links.length == 0) {
								continue;
							} else {
								updateQueryBuffer.append("\n<" + resourceURI
										+ "> <" + fieldURI + "> ");
								int index = 0;
								for (Link link : links) {

									URI uri = link.getValue();
									if (index > 0) {
										updateQueryBuffer.append(", ");
									}
									updateQueryBuffer.append("<" + uri + ">");
									index++;

								}
								updateQueryBuffer.append(" .");
							}
						}
					} else if (field.getType().getName().equals("java.net.URI")) {
						Object fieldValue = getterMethod.invoke(object, null);
						if (fieldValue instanceof URI) {
							URI uri = (URI) fieldValue;
							if (uri == null) {
								continue;
							} else {
								updateQueryBuffer.append("\n<" + resourceURI
										+ "> <" + fieldURI + "> ");
								updateQueryBuffer.append("<" + uri + ">");
								updateQueryBuffer.append(" .");
							}
						}
					} else if (field.getType().getName()
							.equals("java.lang.String")) {
						String fieldValue = (String) getterMethod.invoke(
								object, null);

						// convert multiline string fieldvalue into single line
						// string
						// if(fieldValue == null){
						// fieldValue = "null";
						// }
						// String[] lines =
						// fieldValue.split(System.getProperty("line.separator"));
						// StringBuilder builder = new StringBuilder();
						// builder.ensureCapacity(fieldValue.length()); //
						// prevent resizing
						// int i = 0;
						// for(String line : lines){
						// if(i > 0){
						// builder.append("\r\n");
						// }
						// builder.append(line);
						// i++;
						// }
						// fieldValue = builder.toString();

						if (fieldValue != null) {
							updateQueryBuffer.append("\n<" + resourceURI
									+ "> <" + fieldURI + "> ");
							updateQueryBuffer
									.append("\"\"\""
											+ fieldValue
											+ "\"\"\"^^<http://www.w3.org/2001/XMLSchema#string>");
							updateQueryBuffer.append(" .");
						}
					} else if (field.getType().getName().equals("int")) {
						int fieldValue = (int) getterMethod
								.invoke(object, null);
						updateQueryBuffer.append("\n<" + resourceURI + "> <"
								+ fieldURI + "> ");
						updateQueryBuffer.append("\"" + fieldValue
								+ "\"^^<http://www.w3.org/2001/XMLSchema#int>");
						updateQueryBuffer.append(" .");
					} else if (field.getType().getName().equals("float")) {
						float fieldValue = (float) getterMethod.invoke(object,
								null);
						updateQueryBuffer.append("\n<" + resourceURI + "> <"
								+ fieldURI + "> ");
						updateQueryBuffer
								.append("\""
										+ fieldValue
										+ "\"^^<http://www.w3.org/2001/XMLSchema#float>");
						updateQueryBuffer.append(" .");
					} else if (field.getType().getName().equals("boolean")) {
						boolean fieldValue = (boolean) getterMethod.invoke(
								object, null);
						updateQueryBuffer.append("\n<" + resourceURI + "> <"
								+ fieldURI + "> ");
						updateQueryBuffer
								.append("\""
										+ fieldValue
										+ "\"^^<http://www.w3.org/2001/XMLSchema#boolean>");
						updateQueryBuffer.append(" .");
					}
				} else {
					continue;
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}
