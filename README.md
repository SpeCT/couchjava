### Overview

couchjava is the interface for the java language view server for CouchDb that runs on [Cloudant's BigCouch][1].  CouchDb views are normally written in Javascript, but Java language views have several applications and advantages:
 * Syntax checking at compile time.
 * Allows for re-use of existing Java language libaries and functions.
 * Wide knowledge of Java language
The java view server is enabled by default for all users of Cloudant's hosted CouchDb service.  To sign up for an account, visit the [Cloudant Home Page][1].

### Writing a view

First, you should clone this repository:

	git clone git@github.com:cloudant/couchjava.git

There is an example view in the directory [com/cloudant/javaviews/SplitText.java][3].  This example splits text at white space during map and counts the terms during reduce.   

A user defined view can be created by creating a java class that implements the methods of the [com.cloudant.couchdbjavaserver.JavaView interface][2].

### Compiling your view

Once you have written your class, you need to make it into a jar archive that can be uploaded to cloudant.  The jar archive should contain all of your user code and any non-standard libraries that you call.  

For our example, we use the [ant build system][4].  The build steps are contained in the build.xml file.

To compile the SplitText example, do "ant jars" or simply "ant" ("jars" is the default target).  This will create a jar archive dist/javaviews.jar.

By default, all classes contained in the src directory are compiled and added to the jar.  Additionally, all jars in the lib directory are in the default build path.  To package a jar contained in the lib directory with your javaviews.jar, modify the "views.jar" target, adding:

	<zipfileset excludes="META-INF/*.SF" src="${lib.dir}/org.json.jar"/>

Where "org.json.jar" is replaced with the name of the jar file you reference.

### Uploading your view:

You now need to load the view into CouchDb.  Cloudant has implemented Java lanuage views such that the Java code and libraries are uploaded as binary attachments to a design document.  The design document is written in JSON and specifies which class to call and any necessary configuration information.   


For the following example, my database is named "wikipedia".  The design document looks like:


    {"_id":"_design/splittext",
    "language":"java",
    "views" :
	{
		"title" : {"map":"{\"classname\":\"com.cloudant.javaviews.SplitText\",\"configure\":\"title\"}","reduce":"com.cloudant.javaviews.SplitText"},
		"text" : {"map":"{\"classname\":\"com.cloudant.javaviews.SplitText\",\"configure\":\"text\"}","reduce":"com.cloudant.javaviews.SplitText"}
	}
    }

Note that you need to pass a single variable to "map", the full class name of your JavaView implmentation.  The "configure" field is optional and allows you to configure your class at run time.  Also note that by CouchDb design, the values pointed to by the "map" and "reduce" keys are strings.  In the case of "map", we stringify the JSON object that is used to configure "map" (that's why we need to escape the quotes).

Upload this design doc, either using the command line interface or by saving the file on disk as splittext.json:

	curl -X PUT http://localhost:5984/wikipedia/_design/splittext -d @splittext.json

Now you need to grab the revision id (using GET) in order to attach the jar file that contains your class.  Here is the command to upload the binary attachment.

    	curl -X PUT http://localhost:5984/wikipedia/_design/splittext/javaviews.jar?rev=1-d58671fd0844f4466f498f22575ec308 --data-binary @dist/javaviews.jar

You are done -- you can run your view like you normally would:

	curl -X GET http://localhost:5984/wikipedia/_design/splittext/_view/text?key="an"&reduce=false

gets all documents with "an" in field "text".

	curl -X GET http://localhost:5984/wikipedia/_design/splittext/_view/text?key="an"

counts documents with "an" in field "text"

### Contact

Cloudant folks are usually hanging out in IRC.  Freenode, channel #cloudant.  Wemay also be reached:

 * [http://cloudant.com][1]
 * [info@cloudant.com][5]

----

[1]: http://www.cloudant.com
[2]: https://cloudant.com/doc/javaviews/com/cloudant/couchdbjavaserver/JavaView.html
[3]: https://cloudant.com/doc/javaviews/com/cloudant/javaviews/SplitText.html
[4]: http://ant.apache.org/
[5]: mailto:info@cloudant.com