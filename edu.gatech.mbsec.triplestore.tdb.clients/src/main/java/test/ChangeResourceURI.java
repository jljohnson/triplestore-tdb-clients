package test;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.ResourceUtils;

public class ChangeResourceURI {

	public static void main(String[] args) {
		// load model from triplestore
		String directory = "C:\\Users\\Axel\\git\\triplestore\\triplestore\\mytriplestore4";
		Dataset dataset = TDBFactory.createDataset(directory);
		Model model = dataset.getDefaultModel();

		// get all resources of triplestore

		Map<Resource, String> resourcesToBeRenamed = new HashMap<Resource, String>();

		// first traversal of graph to replace resource/property/object URIs
		StmtIterator statementsIT = model.listStatements();
		while (statementsIT.hasNext()) {
			Statement statement = statementsIT.next();
			// if(!statement.isReified()){ // does not work
			Resource subject = statement.getSubject();
			Property property = statement.getPredicate();
			RDFNode object = statement.getObject();

			if (subject.getURI() != null) {

				// change url of subject to url dereferenceable by
				// triplestore adapter
				String oldURI = subject.getURI();
				// if resource URI is specific to an oslc adapter,
				// change the url to be specific to the triplestore
				// adapter
				if (oldURI.contains("oslc4j")) {
					String newURI = oldURI.replaceAll("8.8./oslc4j.+/services", "8686" + "/oslc4jtdb/services");
					int separatorIndex = newURI.lastIndexOf("services/");
					String oldURIID1 = newURI.substring(0, separatorIndex + 9);
					String oldURIID2 = newURI.substring(separatorIndex + 9, newURI.length());
					System.out.println(oldURIID2);

					// replace slashes by dash
					String uriWithNewID = oldURIID1 + "resources/" + oldURIID2.replace("/", "-");
					System.out.println(uriWithNewID);

					// change uri
					// resourcesToBeRenamed.put(subject, uriWithNewID);

					// add old uri as property to graph
					Property oldURIProperty = ResourceFactory
							.createProperty("http://localhost:" + "8686" + "/oslc4jtdb/", "tdb#oldURI");
					RDFNode oldURIObject = ResourceFactory.createTypedLiteral(subject.getURI());

					// renaming resoource
					Resource renamedResource = ResourceUtils.renameResource(subject, uriWithNewID);

					// add to model for rdf serialization
					model.add(renamedResource, oldURIProperty, oldURIObject);

					// updating property uri
					if (property.getURI() != null) {
						String oldPropertyURI = property.getURI();
						// if resource URI is specific to an oslc adapter,
						// change the url to be specific to the triplestore
						// adapter
						if (oldPropertyURI.contains("oslc4j")) {

							System.out.println("updating property uri");
							String newPropertyURI = oldPropertyURI.replaceAll("8.8./oslc4j.+/services",
									"8686" + "/oslc4jtdb/services");
							int propertySeparatorIndex = newPropertyURI.lastIndexOf("services/");
							String oldPropertyURIID1 = newPropertyURI.substring(0, propertySeparatorIndex + 9);
							String oldPropertyURIID2 = newPropertyURI.substring(propertySeparatorIndex + 9,
									newPropertyURI.length());
							System.out.println(oldPropertyURIID2);

							// replace slashes by dash
							String propertyUriWithNewID = oldPropertyURIID1 + "resources/"
									+ oldPropertyURIID2.replace("/", "-");
							System.out.println(propertyUriWithNewID);
							
							// get property name
							int propertyNameIndex = propertyUriWithNewID.lastIndexOf("#");
							String propertyName = propertyUriWithNewID.substring(propertyNameIndex, propertyUriWithNewID.length());
							
							// create new statement
							Property newProperty = ResourceFactory
									.createProperty(propertyUriWithNewID, propertyName);
							model.add(renamedResource, newProperty, oldURIObject);
							
							// remove old statement
//							statementsIT.remove();
							
							
						}

					}

				}

			}

		}

		// // change uri
		// for (Resource resource : resourcesToBeRenamed.keySet()) {
		//
		// // add old uri as property to graph
		// Property oldURIProperty = ResourceFactory.createProperty(
		// "http://localhost:" + "8585" + "/oslc4jtdb/", "tdbrdf");
		// RDFNode oldURIObject =
		// ResourceFactory.createTypedLiteral(resource.getURI());
		//
		// Resource renamedResource = ResourceUtils.renameResource(resource,
		// resourcesToBeRenamed.get(resource));
		//
		// // add to model for rdf serialization
		// model.add(renamedResource, oldURIProperty, oldURIObject);
		// }

		model.close();
		dataset.close();

	}

}
