# Using an IDE (Intellij) for development of MOLGENIS backend
A rough guide on how to develop MOLGENIS in IntelliJ IDEA is described below.
This was created using IntelliJ IDEA Ultimate. Most of the time we use the latest IntelliJ version.

## Set the maven home dir in IntelliJ
* Open Preferences, Build Execution Deployment, Maven
* Set the home dir to where your >= 3.6.0 maven installation lives.

## Get the MOLGENIS sourcecode
* File, New, Project from version control, Git (or Github)
* Pick `https://github.com/molgenis/molgenis.git` for the repository URL.
* In the Event log: Non-managed pom.xml file found. Choose "Add as Maven Project". (You'll also see 'JPA framework is detected'. Ignore that.)

You now should have a Project called 'molgenis' with a module for each maven module.

## Set the project JDK
Open File, Project Structure. For the entire project:
* (Add and) select Java 11 JDK.
* Set language level to Java 11.

## Create maven run configuration
For normal operation, IntelliJ can compile the sources for you. But some classes get
generated so you'll need to run the maven build at least once.
* Run, Edit Configurations...
* Press `+`, add new maven run config, call it `molgenis [clean install]`
* Set the project location as run directory
* Command line is `clean install`
* In the tool bar, select the `molgenis[clean install]` configuration and press the play button.
MOLGENIS now should compile and test.

## Build in IntelliJ
Build, Make project should work fine now and spew out no errors, only warnings.

## Java Code Style settings
We format our code according to the Google Java Format.
You can run the formatter in maven using target `fmt:format`.
Maven will invoke `fmt:check` at build time and fail if the code isn't formatted properly.

* Install and enable the [IntelliJ plugin which replaces the Reformat Code action](https://plugins.jetbrains.com/plugin/8527-google-java-format).
* Download and import the [IntelliJ Java Google Style file](https://github.com/molgenis/molgenis/blob/master/intellij-molgenis-style.xml) to fix the import order.
* Uncheck the reformat checkbox in the git commit dialog, [it is broken](https://github.com/google/google-java-format/issues/228).

## Git commit hook
If you miss the reformat checkbox, you can configure a git commit hook in the folder where you checked out the molgenis repository.
Add a file called `.git/hooks/pre-commit` with the following contents:

```#!/bin/sh
mvn fmt:format
```

## Use MOLGENIS file-templates
* Make sure that your .idea project folder lives inside your 'molgenis' git project root! Only then will we be able to auto-share templates.
* Goto 'New / Edit file templates' and switch from 'Default' to 'Project'-scope.
* Restart IntelliJ after setting the .idea directory and changing the settings

### Usage
Adds two file templates for creating Package and EntityType classes. The templates can be accessed from the 'New' menu, when right-clicking a java package:

![Pick file template](images/intellij/pick-file-template.png?raw=true, "pick-file-template")

When choosing EntityType and filling out the form...

![Set filename in  template](images/intellij/set-filename-in-template.png?raw=true, "set-file-name-in-template")

... a file is created with the necessary boilerplate:


```java
package org.molgenis.test;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;
import org.springframework.stereotype.Component;
import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.test.PigeonEncounterMetadata.ID;
import static org.molgenis.test.InterestingDataPackage.PACKAGE_INTERESTING_DATA;

@Component
public class PigeonEncounterMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "PigeonEncounter";
	public static final String PIGEON_ENCOUNTER = PACKAGE_INTERESTING_DATA + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";

	private final InterestingDataPackage interestingDataPackage;

	public PigeonEncounterMetadata(InterestingDataPackage interestingDataPackage)
	{
		super(SIMPLE_NAME, PACKAGE_INTERESTING_DATA);
		this.interestingDataPackage = requireNonNull(interestingDataPackage);
	}

	@Override
	public void init()
	{
		setPackage(interestingDataPackage);

		setLabel("Pigeon Encounter");
		//TODO setDescription("");

		addAttribute(ID, ROLE_ID).setLabel("Identifier");
	}
}

public class PigeonEncounter extends StaticEntity
{
	public PigeonEncounter(Entity entity)
	{
		super(entity);
	}

	public PigeonEncounter(EntityType entityType)
	{
		super(entityType);
	}

	public PigeonEncounter(String id, EntityType entityType)
	{
		super(entityType);
		setId(id);
	}

	public void setId(String id)
	{
		set(ID, id);
	}

	public String getId()
	{
		return getString(ID);
	}
}

@Component
public class PigeonEncounterFactory extends AbstractSystemEntityFactory<PigeonEncounter, PigeonEncounterMetadata, String>
{
	PigeonEncounterFactory(PigeonEncounterMetadata pigeonEncounterMetadata, EntityPopulator entityPopulator)
	{
		super(PigeonEncounter.class, pigeonEncounterMetadata, entityPopulator);
	}
}
```

Sadly, it's not possible to generate multiple files from one template, so the Metadata and Factory classes have to be manually moved to separate files. (Tip: Set your cursor to the class name and use Refactor > Move... or press F6)

## Deploy the backend services
Check: [deploy backend services](guide-development-deploy-backend-services.md)

## Deploy / Run in Tomcat server
* Run, Edit configurations..., `+`, Tomcat, Local.
* Call it `molgenis-app [exploded]`
* (Add and) select your Tomcat installation
* Remove any *JVM options* that are MOLGENIS specific. These will override the environment variables below. 
* In the "Open browser" change the URL to http://localhost *(this will open a browser and serve the frontend of MOLGENIS)*.
* The tomcat-port needs to point at 8080. This is also defined in the ```.env```. 
* Select the 'Startup/Connection' tab
  Copy and paste these variables in the Environment variables area and select 'Pass environment variables'.
  Fill in your home-directory in the *molgenis.home*-key.
```properties
environment=development
molgenis.home=**your own version of the data dir**
opencpu.uri.host=localhost
elasticsearch.transport.addresses=localhost:9300
db_uri=jdbc:postgresql://localhost/molgenis
db_user=molgenis
db_password=molgenis
admin.password=admin
admin.email=**your own email address**
MINIO_BUCKET_NAME=molgenis
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=molgenis
MINIO_SECRET_KEY=molgenis
CATALINA_OPTS=-Xmx2g -Xms1g 
```
* Deployment: Select `+` -> `artifact` -> `molgenis-app:war exploded`
* Application context: Select `/`
* Go back to the first tab, you should now have more options in the update and frame deactivation pulldowns.
* Select On Update: Redeploy, don't ask (This is the action that will triggered when you press the blue reload button in the run view)
* Select On frame deactivation: Update resources (This is the action that will be triggered whenever you tab out of IntelliJ)
(If you get update classes and resources to work, let us know!!)
* Select the OpenJDK 11 JRE in the 'JRE' property
* Select your favorite browser in the pulldown.
* Save the configuration
* In the tool bar, select the `molgenis-app [exploded]` configuration and press the play button.

This'll build and deploy MOLGENIS to tomcat and open it in the browser. Whenever you tab from MOLGENIS to the browser, all modified resources will be copied to the deployed exploded war.
A browser reload should display the changes.

> **MULTIPLE RUN CONFIGURATIONS** When you have 2 run configurations (e.g. run in debug and run normally) you need to apply the configuration displayed above.

> In some cases IntelliJ might not pick up all changes in the file system made during the
  build process. This may result in an error referencing a missing jar file. This can be fixed by
  selecting the 'Synchronize MOLGENIS' option from the project action menu. 

## Security
See also the [MOLGENIS Security settings](guide-security.md)
