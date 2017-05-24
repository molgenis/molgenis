# Deploy MOLGENIS with apache-tomcat

The three components needed to run MOLGENIS locally or on a server are:

**
You can download, install and use MOLGENIS for free under license [LGPLv3]().
**

* [apache-tomcat, v7 (latest release)](http://tomcat.apache.org/)
* [PostGreSQL, v9.6](https://www.postgresql.org/download/)
* The WAR for the [latest molgenis-app release](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.molgenis%22%20AND%20a%3A%22molgenis-app%22) from maven central.
* [Java, v8 (latest release)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

Deploy apache-tomcat, and place the molgenis-app WAR as the ROOT.war in your apache-tomcat/webapps folder. If you are unfamiliar with apache-tomcat, follow one of their [apache-tomcat installation guides](https://tomcat.apache.org/tomcat-7.0-doc/deployer-howto.html).

Now that your apache-tomcat is running and MOLGENIS is deployed, you will notice it will not work yet. This is because your database needs to be configured, and a single properties file needs to be set.

**Setting your molgenis-server.properties**   
The properties file supplies information to the application regarding the database URL, and the initial administrator password. To make it clear to tomcat where to find your properties file, you have to edit the setenv.sh file in the apache-tomcat folder.

```
echo 'CATALINA_OPTS="-Xmx2g -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -Dmolgenis.home=${molgenis_home_folder}"' > ${apache-tomcat_folder}/bin/setenv.sh
```

The **-Dmolgenis.home** property tells tomcat where to find your properties file. Replace the ${molgenis_home_folder} with the location of your molgenis home. Note that you should **NOT** use relative paths in your apache-tomcat configuration. Always use absolute paths to point to your molgenis-server.properties.

Inside the specified molgenis home folder, create a file called *molgenis-server.properties*, and depending on the version of your MOLGENIS, write the following:

```
	db_user=molgenis
	db_password=molgenis
	db_uri=jdbc\:postgresql\://localhost/molgenis?reWriteBatchedInserts=true&autosave=CONSERVATIVE
	admin.password=admin
	user.password=admin
```

Remember the *molgenis* specified in your db_uri, because this will be the name of the database you will create later on in PostGreSQL. This effectively means that whatever you call your database, your db_uri should point to it.

**Setting up your PostGreSQL**  
If you are unfamiliar with PostGreSQL, follow one of their [PostGreSQL installation guides](https://www.postgresql.org/docs/9.6/static/index.html). Once you have a PostGreSQL server running, open up the included pgAdmin application that is supplied with most PostGreSQL installations, and perform the following actions:

- Add a database 'molgenis'
- Add a user 'molgenis' (password 'molgenis') under Login Roles
- Add 'can create databases' privilege to user 'molgenis'

Now that your database server and properties file have been configured, restart the apache tomcat server.
If you open up a web browser and navigate to where your apache-tomcat applications are deployed (often this is localhost:8080) you should see the following:  

![](../images//molgenis_home_logged_out.png?raw=true, "molgenis home page")  

Congratulations! You now have your MOLGENIS application up and running. Remember the admin.password you set in the molgenis-server.properties file? Use that password to login as the admin user. The next section will take you through the different modules MOLGENIS has to offer.
