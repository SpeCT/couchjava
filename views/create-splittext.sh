#!/bin/bash

echo '
{
    "_id":"_design/'$1'",
    "language":"java",
    "views" :
	{
	"title" : {"map":"{\"classname\":\"com.cloudant.javaviews.SplitText\",\"configure\":\"title\"}","reduce":"com.cloudant.javaviews.SplitText"},
	"text" : {"map":"{\"classname\":\"com.cloudant.javaviews.SplitText\",\"configure\":\"text\"}","reduce":"com.cloudant.javaviews.SplitText"}
	}
}
' > splittext.json

curl -X PUT http://localhost:5984/wikipedia/_design/$1 -d @splittext.json

curl -X PUT http://localhost:5984/wikipedia/_design/$1/javaviews.jar?rev=1-741547396ca4122a42cc05c7a2cec33e --data-binary @javaviews.jar
