#Automatic syncing of data between Subversion, OSLC adapters and RDF triplestore





##Instructions to demo syncing scenario 

Last updated by Axel Reichwein (axel.reichwein@koneksys.com) 				February 26, 2016



### 1. Before the demo

1. In Windows Explorer, perform a checkout operation of the MagicDraw repository using Tortoise.
2. In MagicDraw: Open MagicDraw model named Wired_Camera_Example  from the local working directory which was just checked out
3. In Eclipse: Launch MagicDraw adapter (Maven configuration named oslc adapter for magicDraw tomcat run)
4. In Eclipse: Launch Simulink adapter (Maven configuration named oslc adapter for simulink)
5. In Eclipse: Launch AMESim adapter (Maven configuration named oslc4jamesim-wink tomcat run)
6. In Eclipse: In project triplestore in package tdb.clients.sync.magicdraw.withrevision, run Java application **MagicDrawAdapterAndTDBSubversionSyncClientWithRevision.java**
7. In Browser: have tabs already open with following URLs

-	[http://localhost:8080/oslc4jmagicdraw/services/svnfilepublisher](http://localhost:8080/oslc4jmagicdraw/services/svnfilepublisher)
-	[http://localhost:8181/oslc4jsimulink/services/svnfilepublisher](http://localhost:8181/oslc4jsimulink/services/svnfilepublisher)
-	[http://localhost:8282/oslc4jamesim/services/svnfilepublisher](http://localhost:8282/oslc4jamesim/services/svnfilepublisher)
-	[http://localhost:8080/oslc4jmagicdraw/services/catalog/singleton](http://localhost:8080/oslc4jmagicdraw/services/catalog/singleton) 
-	[http://localhost:8181/oslc4jsimulink/services/catalog/singleton](http://localhost:8181/oslc4jsimulink/services/catalog/singleton) 
-	[http://localhost:8282/oslc4jamesim/services/catalog/singleton](http://localhost:8282/oslc4jamesim/services/catalog/singleton) 
-	postman client already with prerecorded request (in the history tab) for MagicDraw value property resource with Accept header equal to application/rdf+xmlRDF/XML and same request but with Accept header equal to application/json


### 2. Start the demo

####Demo OSLC adapters supporting Subversion

1. In Browser: Show Subversion repository with MagicDraw files. Example: https://koneksys1:18080/svn/repository3/
2. In Browser: Show revision of Subversion repository containing Magicdraw files
3. In Browser: Show MagicDraw files published by MagicDraw adapter [http://localhost:8080/oslc4jmagicdraw/services/subversionfiles](http://localhost:8080/oslc4jmagicdraw/services/subversionfiles) 
4. In Browser: Show revision of a specific MagicDraw file published by MagicDraw adapter. Example:  http://localhost:8080/oslc4jmagicdraw/services/subversionfiles/httpskoneksys118080svnmagicdrawrepository---Wired_Camera_Example.mdzip 
5. In MagicDraw: show value of a MagicDraw value property
6. In Browser: show how you browse from OSLC Service Provider Catalog to a specific MagicDraw value property 
7. In Browser: show value of MagicDraw value property as OSLC resource in HTML format 
8. In Browser: show with postman rest client value of MagicDraw value property as OSLC resource in RDF/XML and JSON
9. In Eclipse: In project triplestore, explain that your local triplestore (RDF database) is saved in the folder **mytriplestore** and that it can be queried using SPARQL, which is similar to SQL. Explain that the triplestore has been populated with the resources from the running OSLC adapter (step #6 in last section). Explain that you will perform a SPARQL query to retrieve the MagicDraw value property saved in the triplestore
10.	In Eclipse: In project triplestore in package tdb.clients.sync.magicdraw.withrevision, run Java application **QueryTriplestoreForMagicDrawValuePropertyOfSpecificModelWithRevision.java**
11.	and show value of MagicDraw value property saved in the triplestore in the Eclipse console window. 


####Demo automatic syncing between Subversion, adapters and triplestore

1. In MagicDraw: change value of MagicDraw value property
2. With your SVN client, perform a commit operation. Wait for logging statement indicating correct commit operation. If you do not have a SVN commit client, update and run the Java application **MagicDrawCommitClient.java** located in the edu.gatech.mbsec.subversion.client project of the subversion-client repository.
3. In Browser: Show Subversion repository with MagicDraw files. Example: https://koneksys1:18080/svn/repository3/
4. In Browser: Show revision of Subversion repository containing Magicdraw files. Revision number should be +1
5. Wait for MagicDraw adapter to finish reloading files from Subversion server
6. In Browser: Show revision of specific MagicDraw file published by MagicDraw adapter. Example:  http://localhost:8080/oslc4jmagicdraw/services/subversionfiles/httpskoneksys118080svnmagicdrawrepository---Wired_Camera_Example.mdzip 
7. In Browser: show updated value of MagicDraw value property as OSLC resource in HTML format 
8. In Browser: show with postman rest client value of MagicDraw value property as OSLC resource in RDF/XML and JSON
9. In Eclipse: In project triplestore in package tdb.clients.sync.magicdraw.withrevision, run Java application **QueryTriplestoreForMagicDrawValuePropertyOfSpecificModelWithRevision.java**
and show value of MagicDraw value properties saved in the triplestore. 
10.	In Eclipse: In project triplestore, run batch file named run **fuseki.bat**
11.	In browser, go to page [http://localhost:3030/](http://localhost:3030/) 
12.	In Eclipse: In project triplestore, copy SPARQL query located in file get SysML value properties.txt in the folder example sparql queries
13.	In browser, at page [http://localhost:3030/](http://localhost:3030/), go to Query and enter SPARQL query text, and click on play. Query results will display MagicDraw SysML value properties saved in triplestore having different revision numbers and values
14.	In Eclipse: In project triplestore, copy SPARQL query located in file get all sysml blocks.txt in the folder example sparql queries
15.	In browser, at page [http://localhost:3030/](http://localhost:3030/), go to Query and enter SPARQL query text, and click on play. Query results will display all MagicDraw SysML blocks saved in triplestore having different revision numbers 


Note: after launching Fuseki, you can no longer perform SPARQL queries against the triplestore using the Java API. Every interaction with the triplestore has to go through the SPARQL HTTP endpoint of Fuseki. 


