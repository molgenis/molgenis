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

start Eclipse from the commandline (via Dock will not work!)

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

## 6. Import, build and run MOLGENIS in Eclipse

In eclpise, if still open, close the 'Welcome' screen.

Choose 'File' -> 'Import' -> 'Import existing Maven projects'

Browse to your /git/molgenis directory 

Select and wait to install all kinds of maven connectors (this takes a while!)

Right mouse 'molgenis' -> Run as -> Maven install.

Right mouse 'molgenis-app' -> Run as -> Maven build ... -> in goals type 'jetty:run' and push run button

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

Run the Maven target 'jetty:stop'. If that does not help, use your opering systems process manager to kill anything running on port 8080. For example:

    kill -9 `lsof -i :8080 -t`



    
