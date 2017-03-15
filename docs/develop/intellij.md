# IntelliJ

A rough guide on how to develop molgenis in IntelliJ IDEA.
This was created using IntelliJ IDEA 2016.1 Ultimate, courteously provided to us by
JetBrains on an open source license.

## Middleware
Before you start, make sure the following middleware is installed on your system.
    * Create a database called omx with a user molgenis that has full permissions on the database.
* Java 8 JDK (latest version available from Oracle)
* Maven >= 3.1.0 (The built-in maven version of IntelliJ is too old to compile molgenis.)
* Apache Tomcat 7 or 8
* MySQL server.
* MySQL JDBC driver. Put it in the lib/ext dir of your Tomcat installation.

## Molgenis settings
You'll need a molgenis-server.properties file. In your home dir, create a folder
`.molgenis`. In it create a folder called `omx`.
Put a file in there called `molgenis-server.properties` and fill it with this information:

```
	db_user=molgenis  
	db_password=molgenis  
	db_uri=jdbc:mysql://localhost/molgenis  
	admin.password=admin  
	user.password=admin  
```

## Set the maven home dir in IntelliJ
* Open Preferences, Build Execution Deployment, Maven
* Set the home dir to where your >= 3.1.0 maven installation lives.

## Creating the molgenis project
* File, New, Project from version control, Git (or Github)
* Pick `https://github.com/molgenis/molgenis.git` for the repository URL.
* In the Event log: Non-managed pom.xml file found. Choose "Add as Maven Project". (You'll also see 'JPA framework is detected'. Ignore that.)

You now should have a Project called molgenis with a module for each maven module.

## Set the project JDK
Open File, Project Structure. For the entire project:
* (Add and) select Java 8 JDK.
* Set language level to Java 8.

## Create maven run configuration
For normal operation, IntelliJ can compile the sources for you. But some classes get
generated so you'll need to run the maven build at least once.
* Run, Edit Configurations...
* Press `+`, add new maven run config, call it `molgenis [clean install]`
* Set the project location as run directory
* Command line is `clean install`
* In the tool bar, select the `molgenis[clean install]` configuration and press the play button.
Molgenis now should compile and test. In particular the molgenis-core-ui module may take a while the first time it is run.

## Build in IntelliJ
Build, Make project should work fine now and spew out no errors, only warnings.

## Java Code Style settings
* In the root folder of the molgenis project, you'll find eclipse-java-molgenis-style.xml.
Open Settings, Editor, Code Style, and import it as the molgenis profile.
Select it for the molgenis project.
* TODO: Other settings (Dennis)

## Deploy / Run in Tomcat server
* Run, Edit configurations..., `+`, Tomcat, Local.
* Call it `molgenis-app [exploded]`
* (Add and) select your Tomcat installation
* VM options: `-Dmolgenis.home=<path to dir containing molgenis-server.properties> -Xmx4g -Des.discovery.zen.ping.multicast.enabled=false -Des.network.host=localhost`
* Deployment: Select `+` -> `artifact` -> `molgenis-app:war exploded`
* Application context: Select `/`
* Go back to the first tab, you should now have more options in the update and frame deactivation pulldowns.
* Select On Update: Redeploy, don't ask (This is the action that will triggered when you press the blue reload button in the run view)
* Select On frame deactivation: Update resources (This is the action that will be triggered whenever you tab out of IntelliJ)
(If you get update classes and resources to work, let us know!!)
* Select your favorite browser in the pulldown.
* Save the configuration
* In the tool bar, select the `molgenis-app [exploded]` configuration and press the play button.

This'll build and deploy molgenis to tomcat and open it in the browser.
Whenever you tab from molgenis to the browser, all modified resources will be copied to the deployed exploded war.
A browser reload should display the changes.

## Security
See also the [MOLGENIS Security settings](./security)

## Webpack watch run configuration
(TODO: I just noticed that IntelliJ has webpack support so I suspect there's a better way to do this.)
* Open molgenis-core-ui/package.json
* Select the scripts / watch key in the json configuration
* Right-click it and select the NPM `create watch` option from the menu.
* SaveIn the tool bar, select the npm `watch` configuration and press the play button to start webpack watch.

Somehow, getting your javascript/webpack changes to show in the browser requires tabbing from IntelliJ to the browser
and back again twice. But then it works!
Let us know if you find out how to improve this.
