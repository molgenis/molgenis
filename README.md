[![Build Status](http://molgenis.org/jenkins/job/new-molgenis/badge/icon)](http://molgenis.org/jenkins/job/new-molgenis/)

# Welcome to MOLGENIS

MOLGENIS is an collaborative open source project on a mission to generate great software infrastructure for life science research. Each app in the MOLGENIS family comes with rich data management interface and plug-in integration of analysis tools in R, Java and web services.

The procedure below tells you how to checkout the molgenis project and build one of the example apps.

## 1. Fork the molgenis repository

Create a fork of the github.com/molgenis/molgenis repository for your own Github account.
More information about forking can be found here: https://help.github.com/articles/fork-a-repo

## 2. Clone your molgenis fork on your local system

Pick a folder for your Git repositories, e.g.:

    cd ~/git

Clone your fork to this folder:

    git clone https://github.com/<yourname>/molgenis.git
   
## 3. Install and configure eclipse

MOLGENIS is created with help of Maven and Freemarker. You need a few eclipse plugins to work with those.

Important: get the LATEST version of Eclipse and all plugins mentioned. For example, using Eclipse Juno Release 1 or lower might result in infinite build loops.

Start Eclipse and select a workspace location (e.g. ~/eclipse/workspace - do not pick the ~/git folder).

Now install the plugins. Click: Help -> Eclipse marketplace. Add the following (restart Eclipse when done):

* maven integration for eclipse
* Apt M2E connector
* JBoss Tools (ONLY SELECT THE 'FreeMarker IDE feature' later in the wizard!)
* testng

## 4. Import the molgenis project into eclipse
   
Start Eclipse and select your workspace location (e.g. ~/eclipse/workspace).

Click: File -> Import ... -> Existing Maven Projects.

Set root directory to your git clone folder. E.g. ~/git (this means you can still see the 'molgenis' folder).

Eclipse discovers all molgenis modules (should be all checked).

Click next/okay; eclipse will now import the modules. Also Eclipse will automatically install maven connector plugins when needed (restart follows)

## 5. Generate the code for the first time

If still open, close the 'Welcome' screen.

Eclipse will automatically build and download jars.

Right mouse 'molgenis' -> Run as -> Maven install.

After generation eclipse will compile automagically.

## 6. Create mysql database for OMX app

Assumed is that you have installed the latest mysql.
Log in via terminal using your root credentials:

    mysql -u root -p

Give create a database with permissions to molgenis user:

    create database omx;
    grant all privileges on omx.* to molgenis@localhost identified by 'molgenis';
    flush privileges;

## 7. Configure the default admin password

Create a file called molgenis-server.properties in your home folder (so ~/molgenis-server.properties)

Add two properties to this file:

	"admin.password=admin"
	"user.password=admin"

If these properties are not present, the MolgenisDatabasePopulator will fail (RuntimeException). This properties-file should be in your home folder, if the file is not there yet, just create it.

## 8. Run the OMX app (example)

Right click 'molgenis-app-omx' -> Run as ... -> Maven build ...

In the 'goals' box type in 'jetty:start'
Before running, go the the 'JRE' tab and add the the following VM arguments:

 	-XX:MaxPermSize=512M
    -Xmx2g

Now go back to the previous tab and Choose Run. Jetty will be started.

Open your browser at http://localhost:8080/

You should see the application. Login as 'admin', 'admin' to be able to upload and view data, create users, etc etc.

To increase memory for every run do the following:

Under 'Run Configurations', select the Maven Build that starts the application. Go to the 'JRE' tab, and add the following VM arguments:

	-XX:MaxPermSize=512M
    -Xmx2g
   

## 9. Keep your code up to date

Add the original molgenis repository as a remote location.

    cd ~/git/molgenis
    git remote add blessed https://github.com/molgenis/molgenis.git
    
Perform regular updates so the latest changes are merged with your local clone.

    git pull blessed master
    
And push back any merges or commits of your own to your online fork.

    git push origin master
    
## 10. Troubleshooting

### When I try to start an application, the console tells me 'Address already in use'!

Run the Maven target 'jetty:stop'. If that does not help, use your opering systems process manager to kill anything running on port 8080. For example:

    kill -9 `lsof -i :8080 -t`
    
