### couchapp for Java language views

## Requirements

cloudant hosted account (sign up at <https://cloudant.com/#!/solutions/cloud>), couchapp (installation instructions at <http://couchapp.org/page/installing>).

## Usage

This is a simple example couchapp that uploads a java language view.  The is part of the [couchjava][1] project.

To specify that the language of the view is java, there is a file "language"
containing the term "java" in the top level directory.

As with all couchapps, the view is defined in the view folder.  In this case, there is a view call "title" with a map and reduces classes defined:

views/title/map.java
views/title/reduce.java

Any jar files that you want to upload should be place in the _attachments directory (note -- you can place multiple jar files there).  If you want to work with the deafult javaviews.jar file created by the couchjava project, create  a symoblic link to couchjava/dist/javaviews.jar

<pre><code>ln -s ../../dist/javaviews.jar _attachments/javaviews.jar</code></pre>

If you wish to specify a default database for this view, modify the .couchapprc file:

cat > .couchapprc
{"env":{"default":{"db":"http://&lt;user&gt;:&lt;pass&gt;@&lt;user&gt;.cloudant.com:5984/&lt;your_db&gt;"}}}
^C</code></pre>
*that last line means hit **CTRL-C***

the you can install the view with:

<pre><code>couchapp push</code></pre>

Otherwise, you can specify the database in the couchapp call:

<pre><code>couchapp push http://&lt;user&gt;:&lt;pass&gt;@&lt;user&gt;.cloudant.com:5984/&lt;your_db&gt;</code></pre>

### Contact

Cloudant folks are usually hanging out in IRC.  Freenode, channel #cloudant.  We may also be reached:

 * [http://cloudant.com][1]
 * [info@cloudant.com][3]

----

[1]: https://github.com/cloudant/couchjava 
[2]: http://www.cloudant.com
[3]: mailto:info@cloudant.com
