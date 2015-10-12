[![Build Status](http://www.molgenis.org/jenkins/buildStatus/icon?job=molgenis)](http://www.molgenis.org/jenkins/job/molgenis/)

# Welcome to MOLGENIS

MOLGENIS is a collaborative open source project on a mission to generate great software infrastructure for life science research. 

The procedure below tells you how to checkout the molgenis project and build one of the example apps.

## 1. Install software needed

install java8 JDK (not JRE) from http://www.oracle.com/technetwork/java/javase/downloads/index.html

install eclipse (we used Luna) from https://www.eclipse.org/downloads/

install mysql from (we used 5.6.23 DMG) http://dev.mysql.com/downloads/mysql/

install git from http://git-scm.com/download/

## 2. Configure mysql

open terminal and start mysql client (command differs in Windows, Linux, Mac): 

    /usr/local/mysql/bin/mysql -u root
    
Note, if not running you can start the MySQL server from the System Preferences

in mysql client create database and permissions via command:

    create database omx;
    grant all privileges on omx.* to molgenis@localhost identified by 'molgenis';
    flush privileges;

## 3. Configure Eclipse

start Eclipse

when asked, create fresh workspace folder (not same folder as git!)

go to 'Help' -> 'Eclipse market place'

find and install plugins:
* 'JBoss Tools', during install only select Freemarker IDE
* 'TestNG for Eclipse'
* 'm2e-apt'

go to 'Eclipse' -> 'Preferences' 
* 'installed JREs' and select 'java 8'
* 'Maven' -> 'Annotation Processing' and select 'Automatically Configure JDT APT'

Restart

## 4. Get the code

create acount on github.com 

'fork' on http://github.com/molgenis/molgenis

copy the cloneURL

open terminal (mkdir if needed) and type 

    cd ~/git 
    git clone http://github.com/[YOURACCOUNT]/molgenis
    
Optionally select stable molgenis version:

    git fetch --tags origin
    git checkout -b <tag name: see https://github.com/molgenis/molgenis/releases>

More information about forking can be found here: https://help.github.com/articles/fork-a-repo

## 5. Configure the default admin password

Create the file ~/.molgenis/omx/molgenis-server.properties and add user and database properties:

    db_user=molgenis
    db_password=molgenis
    db_uri=jdbc:mysql://localhost/omx
    admin.password=admin
    user.password=admin

If these properties are not present, the MolgenisDatabasePopulator will fail (RuntimeException). This properties-file should be in your home folder, if the file is not there yet, just create it.    

If you are developing you might want to add the following
	
	environment=development

This property allows for at runtime javascript editing and makes sure the javascript files do not get minified and bundled.


## 6. Import, build and run MOLGENIS in Eclipse

In eclipse, if still open, close the 'Welcome' screen.

Choose 'File' -> 'Import' -> 'Import existing Maven projects'

Browse to your /git/molgenis directory 

Select it, you will then be prompted to install some maven connectors, accept these 
(else MOLGENIS will not build properly in eclipse)

Right mouse 'molgenis' -> Run as -> Maven build ... 
	In Goals type: clean install. 
	Under Goals check Update Snapshots and Resolve Workspace artifacts. 
	In the JRE tab, add the VM argument; -Xmx2g
	Click run

Right mouse 'molgenis-app' -> Run as -> Maven build ... 
	in goals type 'jetty:run' 
	In the JRE tab, add the VM argument; -Xmx2g
	Click run

Open your browser at http://localhost:8080/

You should see the application. Login as 'admin', 'admin' to be able to upload and view data, create users, etc etc.

## 7. Keep your code up to date

Add the original molgenis repository as a remote location.

    cd ~/git/molgenis
    git remote add blessed https://github.com/molgenis/molgenis.git
    
Perform regular updates so the latest changes are merged with your local clone.

    git pull blessed master
    
And push back any merges or commits of your own to your online fork.

    git push origin master

## 8. Troubleshooting

### When I build I get maven build error 'env not available'. 

Solution: start Eclipse from commandline.

### When I try to start an application, the console tells me 'Address already in use'!

Run the Maven target 'jetty:stop'. If that does not help, use your operating systems process manager to kill anything running on port 8080. For example:

    kill -9 `lsof -i :8080 -t`

## 9. Third-party software
For some modules in Molgenis, third-party software is in use. It is important to know that some of these licenses are different than the Molgenis license.

In this section you can find a list of remarks about third-party software in Molgenis modules.

### molgenis-charts module
As a non-profit organisation we are using the Highsoft software 'highstock version 1.3.6', in the molgenis-charts module to build some charts.

Important! The Highsoft software product is not free for commercial use. For Highsoft products and pricing go to: http://shop.highsoft.com/

To turn-off/deactivate this functionality you can set the RuntimeProperty “plugin.dataexplorer.mod.charts” to false:
    1. You can find the RuntimeProperty via the menu Entities -> RuntimeProperty.
    2. Search for the RuntimeProperty "plugin.dataexplorer.mod.charts" and update to false.
    3. If it does not exists:
        a. Create a new RuntimeProperty
        b. Name -> plugin.dataexplorer.mod.charts
        c. Value -> false
