# Welcome to MOLGENIS

MOLGENIS is an collaborative open source project on a mission to generate great software infrastructure for life science research. Each app in the MOLGENIS family comes with rich data management interface and plug-in integration of analysis tools in R, Java and web services.

The procedure below tells you how to checkout the molgenis project and build one of the example apps

## 1. clone the molgenis repo

Go to directory

	cd ~/git


Create a new workspace

	mkdir molgenis
	cd molgenis


Clone the repo

	git clone https://github.com/<yourname>/molgenis.git


Now you have a folder molgenis in your workspace directory.

## 2. install and configure eclipse == 
(tested with latest eclipse download, J2EE Juno 4.2 SR1, Mac OSX 64bit)

MOLGENIS is created with help of Maven and Freemarker. You need a few eclipse plugins to work with those.

start eclipse:

	~/Software/eclipse-juno-4.2/eclipse


When asked chose (new) workspace directory. I choose to simply same directory ~/git/molgenis as before

Now install the plugins by choosing {{{Help -> Eclipse marketplace}}}. 
Add the following (you can restart Eclipse when done):
* maven integration for eclipse
* Apt M2E connector
* JBoss Tools (ONLY SELECT THE 'FreeMarker IDE feature' later in the wizard!)

Now you have configure Eclipse. Restart eclipse.

## 3. import the molgenis project into eclipse

Start Eclipse, select your workspace if asked.

Click: File -> Import ... -> Existing Maven Projects 

Set root directory to your git checkout folder. E.g. ~/git/molgenis.
(you can still see the molgenis13.2 folder).

Eclipse discovers all molgenis modules (should be all checked).

Click next/okay; eclipse will now import the modules. Also Eclipse will automatically install maven connector plugins when needed (restart follows)

## 4. generate the code for the first time

If still open, close the 'Welcome' screen

Eclipse will automatically build and download jars

Right mouse 'molgenis13.2' -> Run as -> Maven generate-sources

After generation eclipse will compile automagically

## 5. create mysql database for omicsconnect app

Assumed is that you installed mysql
Log in via terminal using your root credentials

	mysql -u root -p

Give create a database with permissions to molgenis user

	create database omicsconnect;
	grant all privileges on omicsconnect.* to molgenis@localhost identified by 'molgenis';
	flush privileges;

Load schema:

TODO: this should use JPA so that database need not to this additional setup

	use omicsconnect;
	\. /path/git/molgenis13.2/molgenis-app-omicsconnect/target/generated-sources/molgenis/sql/create_tables.sql

## 6. run the omicsconnect app (example)

TODO: this needs to be improved so it starts immediately without generate/build if not needed

Right click 'molgenis-app-omicsconnect' -> Run as ... -> Maven build ...

In the 'goals' box type in 'jetty:run'

Choose Run. Now jetty will be started.

Open your browser at http://localhost:8080/
