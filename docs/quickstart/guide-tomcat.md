# Deploy MOLGENIS with Apache Tomcat

The components needed to run MOLGENIS locally or on a server are:

**
You can download, install and use MOLGENIS for free under license [LGPLv3]().
**

* [Java Platform (JDK) 8u131](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Apache Tomcat v7.0.78](http://tomcat.apache.org/)
* [PostgreSQL v9.6.3](https://www.postgresql.org/download/)
* [Elasticsearch v5.4.1](https://www.elastic.co/downloads/elasticsearch)
* [MOLGENIS web application](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.molgenis%22%20AND%20a%3A%22molgenis-app%22) (Select 'war' in 'Download' column)


Deploy Apache Tomcat, and place the molgenis-app WAR as the ROOT.war in your apache-tomcat/webapps folder. If you are unfamiliar with Apache Tomcat, follow one of their [Apache Tomcat installation guides](https://tomcat.apache.org/tomcat-7.0-doc/deployer-howto.html).

Now that your Apache Tomcat is running and MOLGENIS is deployed, you will notice it will not work yet. This is because your database needs to be configured, and a single properties file needs to be set.

**Setting your molgenis-server.properties**   
The properties file supplies information to the application regarding the database URL, and the initial administrator password. To make it clear to Tomcat where to find your properties file, you have to edit the setenv.sh file in the apache-tomcat folder.

```
echo 'CATALINA_OPTS="-Xmx2g -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -Dmolgenis.home=${molgenis_home_folder}"' > ${apache-tomcat_folder}/bin/setenv.sh
```

The **-Dmolgenis.home** property tells tomcat where to find your properties file. Replace the ${molgenis_home_folder} with the location of your molgenis home. Note that you should **NOT** use relative paths in your apache-tomcat configuration. Always use absolute paths to point to your molgenis-server.properties.

Inside the specified molgenis home folder, create a file called *molgenis-server.properties*, and depending on the version of your MOLGENIS, write the following:

```
db_user=molgenis
db_password=molgenis
db_uri=jdbc\:postgresql\://localhost/molgenis
admin.password=admin
user.password=admin
```

Remember the *molgenis* specified in your db_uri, because this will be the name of the database you will create later on in PostgreSQL. This effectively means that whatever you call your database, your db_uri should point to it.

**Setting up your PostgreSQL**  
If you are unfamiliar with PostgreSQL, follow one of their [PostgreSQL installation guides](https://www.postgresql.org/docs/9.6/static/index.html). Once you have a PostgreSQL server running, open up the included pgAdmin application that is supplied with most PostgreSQL installations, and perform the following actions:

- Add a database 'molgenis'
- Add a user 'molgenis' (password 'molgenis') under Login Roles
- Add 'can create databases' privilege to user 'molgenis'

**Configuring Elasticsearch**  
Open elasticsearch.yml in the Elasticsearch config directory and set the following properties:
```
cluster.name: molgenis
node.name: node-1
indices.query.bool.max_clause_count: 131072
```
Start Elasticsearch from the command line:
```
./bin/elasticsearch
```

By default MOLGENIS will try to connect to a node within cluster 'molgenis' on address 127.0.0.1:9300 (not to be confused with port 9200 which is used for REST communication). If the cluster is named differently or is running on another address then you will have to specify these properties in *molgenis-server.properties*:
```
elasticsearch.cluster.name=my-cluster-name
elasticsearch.transport.addresses=127.1.2.3:9300,127.3.4.5:9301
```
**Launching MOLGENIS**  
Now that your database server, Elasticsearch and properties file have been configured, restart the Apache Tomcat server.
If you open up a web browser and navigate to where your apache-tomcat applications are deployed (often this is localhost:8080) you should see the following:  

![](../images//molgenis_home_logged_out.png?raw=true, "molgenis home page")  

Congratulations! You now have your MOLGENIS application up and running. Remember the admin.password you set in the molgenis-server.properties file? Use that password to login as the admin user. The next section will take you through the different modules MOLGENIS has to offer.
