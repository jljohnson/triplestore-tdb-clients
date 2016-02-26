#Automatic syncing between Subversion, adapters and triplestore





##Instructions to demo syncing scenario (for Matlab R2013b, 2014a, 2014b, 2015)

Last updated by Axel Reichwein (axel.reichwein@koneksys.com) 				February 26, 2016



### 1. Before the demo

1. In Windows Explorer, perform a checkout operation of the MagicDraw repository using Tortoise.
2. In MagicDraw: Open MagicDraw model named Wired_Camera_Example  from the local working directory which was just checked out
3. In Eclipse: Launch MagicDraw adapter (Maven configuration named oslc adapter for magicDraw tomcat run)
4. In Eclipse: Launch Simulink adapter (Maven configuration named oslc adapter for simulink)
5. In Eclipse: Launch AMESim adapter (Maven configuration named oslc4jamesim-wink tomcat run)
6. In Eclipse: In project triplestore in package tdb.clients.sync.magicdraw.withrevision, run Java application MagicDrawAdapterAndTDBSubversionSyncClientWithRevision.java
7. In Browser: have tabs already open with following URLs

-	[http://localhost:8080/oslc4jmagicdraw/services/svnfilepublisher](http://localhost:8080/oslc4jmagicdraw/services/svnfilepublisher)
-	[http://localhost:8181/oslc4jsimulink/services/svnfilepublisher](http://localhost:8181/oslc4jsimulink/services/svnfilepublisher)
-	[http://localhost:8282/oslc4jamesim/services/svnfilepublisher](http://localhost:8282/oslc4jamesim/services/svnfilepublisher)
-	[http://localhost:8080/oslc4jmagicdraw/services/catalog/singleton](http://localhost:8080/oslc4jmagicdraw/services/catalog/singleton) 
-	[http://localhost:8181/oslc4jsimulink/services/catalog/singleton](http://localhost:8181/oslc4jsimulink/services/catalog/singleton) 
-	[http://localhost:8282/oslc4jamesim/services/catalog/singleton](http://localhost:8282/oslc4jamesim/services/catalog/singleton) 
-	postman client already with prerecorded request (in the history tab) for MagicDraw value property resource with Accept header equal to application/rdf+xmlRDF/XML and same request but with Accept header equal to application/json
