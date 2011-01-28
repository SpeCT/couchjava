### Overview

couchjava is the interface for the java language view server for CouchDB that runs on [Cloudant's BigCouch][1].  CouchDB views are normally written in Javascript, but Java language views have several applications and advantages:

 * Syntax checking at compile time.
 * Allows for re-use of existing Java language libraries and functions.
 * Wide knowledge of Java language

The java view server is enabled by default for all users of Cloudant's hosted CouchDB service.  To sign up for an account, visit the [Cloudant Home Page][1].

### Writing a view

First, you should clone this repository:

	git clone git@github.com:cloudant/couchjava.git

There is an example view in the directory [com/cloudant/javaviews/SplitText.java][3].  This example splits text at white space during map and counts the terms during reduce.   

There is also an example of a custom Lucene indexer [com/cloudant/indexers/MyCustomSearch.java][6].  This shows how to write a user defined indexer for use with Cloudant's search.

A user defined view can be created by creating a java class that implements the methods of the [com.cloudant.couchdbjavaserver.JavaView interface][2].  For a search indexing class, you'll want to implement [com.cloudant.couchdbjavaserver.SearchView interface][7]

### Compiling your view

Once you have written your class, you need to make it into a jar archive that can be uploaded to cloudant.  The jar archive should contain all of your user code and any non-standard libraries that you call.  

For our example, we use the [ant build system][4].  The build steps are contained in the build.xml file.

To compile the SplitText example, do "ant jars" or simply "ant" ("jars" is the default target).  This will create a jar archive dist/javaviews.jar.

By default, all classes contained in the src directory are compiled and added to the jar.  Additionally, all jars in the lib directory are in the default build path.  To package a jar contained in the lib directory with your javaviews.jar, modify the "views.jar" target, adding:

	<zipfileset excludes="META-INF/*.SF" src="${lib.dir}/org.json.jar"/>

Where "org.json.jar" is replaced with the name of the jar file you reference.

### Uploading your view:

You now need to load the view into CouchDB.  Cloudant has implemented Java language views such that the Java code and libraries are uploaded as binary attachments to a design document.  The design document is written in JSON and specifies which class to call and any necessary configuration information.   


For the following example, my database is named "wikipedia".  The design document looks like:


    {"_id":"_design/splittext",
    "language":"java",
    "views" :
	{
		"title" : {"map":{"classname":"com.cloudant.javaviews.SplitText","configure":"title"},"reduce":"com.cloudant.javaviews.SplitText"},
		"text" : {"map":{"classname":"com.cloudant.javaviews.SplitText","configure":"text"},"reduce":"com.cloudant.javaviews.SplitText"}
	}
    }

Note that you required to pass an JSON Object to "map" containing the full class name of your JavaView implementation.  The "configure" field is optional and allows you to configure your class at run time.  Reduce takes a single string with the name of the class containing Java reduce function (or a CouchDB builtin like _count, _sum, etc.)

Upload this design doc, either using the command line interface or by saving the file on disk as splittext.json:

	curl -X PUT http://localhost:5984/wikipedia/_design/splittext -d @splittext.json

Now you need to grab the revision id (using GET) in order to attach the jar file that contains your class.  Here is the command to upload the binary attachment.

    	curl -X PUT http://localhost:5984/wikipedia/_design/splittext/javaviews.jar?rev=1-d58671fd0844f4466f498f22575ec308 --data-binary @dist/javaviews.jar

You are done -- you can run your view like you normally would:

	curl -X GET http://localhost:5984/wikipedia/_design/splittext/_view/text?key="an"&reduce=false

gets all documents with "an" in field "text".

	curl -X GET http://localhost:5984/wikipedia/_design/splittext/_view/text?key="an"

counts documents with "an" in field "text"

### Cloudant Search View

To use [com.cloudant.indexers.MyCustomSearch][6] for indexing your database, you'll need to upload the following design document in the standard location (_design/lucene):

    {
        "language":"java",
        "views" : 
	{
	    "index" : {"map":{"classname":"com.cloudant.indexers.MyCustomSearch","configure":{"analyzer":"org.apache.lucene.analysis.WhitespaceAnalyzer","fields[{"name":".*","lucenename":"all","type":"string","regexp":true}]}},"reduce":"_count"}
	    }    
    }

MyCustomSearch is identical to the standard Cloudant Search class com.cloudant.indexers.CustomSearch.  Full configuration instructions can be found on [Cloudant Support][8].


### Contact

Cloudant folks are usually hanging out in IRC.  Freenode, channel #cloudant.  We may also be reached:

 * [http://cloudant.com][1]
 * [info@cloudant.com][5]

----

[1]: http://www.cloudant.com
[2]: https://cloudant.com/doc/javaviews/com/cloudant/couchdbjavaserver/JavaView.html
[3]: https://cloudant.com/doc/javaviews/com/cloudant/javaviews/SplitText.html
[4]: http://ant.apache.org/
[5]: mailto:info@cloudant.com
[6]: https://cloudant.com/doc/javaviews/com/cloudant/indexers/MyCustomSearch.html
[7]: https://cloudant.com/doc/javaviews/com/cloudant/couchdbjavaserver/SearchView.html
[8]: http://support.cloudant.com/faqs/search/search-indexing