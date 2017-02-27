**
This document explains how to get the code, install the necessary software and compile/run for the first time
**

# Software needed

install java8 JDK (not JRE) from http://www.oracle.com/technetwork/java/javase/downloads/index.html

install eclipse (we used Luna) from https://www.eclipse.org/downloads/
(or use [IntelliJ](./intellij.html))

MOLGENIS v1.21.5 or lower: [install MySQL](https://www.mysql.com/downloads/) (we used 5.6.23 DMG)
MOLGENIS v2.0 or higher: [install PostGreSQL](https://www.postgresql.org/download/)

install git from http://git-scm.com/download/

# Configure mysql

open terminal and start mysql client (command differs in Windows, Linux, Mac): 

    /usr/local/mysql/bin/mysql -u root
    
Note, if not running you can start the MySQL server from the System Preferences

in mysql client create database and permissions via command:

    create database omx;
    grant all privileges on omx.* to molgenis@localhost identified by 'molgenis';
    flush privileges;

# Configure postgresql
If you are unfamiliar with PostGreSQL, follow one of their [PostGreSQL installation guides](https://www.postgresql.org/docs/9.6/static/index.html). Once you have a PostGreSQL server running, open up the included pgAdmin application that is supplied with most PostGreSQL installations, and perform the following actions:

- Add a database 'molgenis'
- Add a user 'molgenis' (password 'molgenis') under Login Roles
- Add 'can create databases' privilege to user 'molgenis'

Now that your database server and properties file have been configured. 

# Configure Eclipse

start Eclipse

when asked, create fresh workspace folder (not same folder as git!)

go to 'Help' -> 'Eclipse market place'

find and install plugins:
* 'JBoss Tools', during install only select Freemarker IDE
* 'TestNG for Eclipse'
* 'm2e-apt'

Restart (if you didn't do that already)

go to 'Eclipse' -> 'Preferences' 
* 'installed JREs' and select 'java 8'
* 'Maven' -> 'Annotation Processing' and select 'Automatically Configure JDT APT'

Restart

# Get the code

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

# Set admin password<a name="server-props"></a>

Create directory ~/.molgenis/omx/

    mkdir -p ~/.molgenis/omx/
    
Create the file ~/.molgenis/omx/molgenis-server.properties 

    nano ~/.molgenis/omx/molgenis-server.properties
    
... and add user and database properties to this file when using MOLGENIS 1.21.5 or lower:

```
	db_user=molgenis  
	db_password=molgenis  
	db_uri=jdbc:mysql://localhost/molgenis  
	admin.password=admin  
	user.password=admin  
```
... or add user and database properties to this file when using MOLGENIS 2.0 or higher:

```
	db_user=molgenis  
	db_password=molgenis  
	db_uri=jdbc\:postgresql\://localhost/molgenis?reWriteBatchedInserts=true&autosave=CONSERVATIVE
	admin.password=admin  
	user.password=admin  
```

If these properties are not present, the MolgenisDatabasePopulator will fail (RuntimeException). This properties-file
should be in your home folder, if the file is not there yet, just create it.

# Start MOLGENIS

In eclipse, if still open, close the 'Welcome' screen.

Choose 'File' -> 'Import' -> 'Import existing Maven projects'

Browse to your /git/molgenis directory 

Select it, you will then be prompted to install some maven connectors, accept these 
(else MOLGENIS will not build properly in eclipse)

Right mouse 'molgenis' -> Run as -> Maven build ... 
	In Goals type: clean install. 
	Under Goals check Update Snapshots and Resolve Workspace artifacts. 
	In the JRE tab, add the VM arguments: 
	```
	-Xmx2g -Des.discovery.zen.ping.multicast.enabled=false -Des.network.host=localhost
	```
	Click run

Right mouse 'molgenis-app' -> Run as -> Maven build ... 
	in goals type 'jetty:run' 
	In the JRE tab, add the VM arguments:
    ```
    -Xmx2g -Des.discovery.zen.ping.multicast.enabled=false -Des.network.host=localhost
    ```
	Click run

Open your browser at http://localhost:8080/

You should see the application. Login as 'admin', 'admin' to be able to upload and view data, create users, etc etc.

# Update your code

Add the original molgenis repository as a remote location.

    cd ~/git/molgenis
    git remote add blessed https://github.com/molgenis/molgenis.git
    
Perform regular updates so the latest changes are merged with your local clone.

    git pull blessed master
    
And push back any merges or commits of your own to your online fork.

    git push origin master
    
## Security
See also the [MOLGENIS Security settings](./security)

# Troubleshooting

## When I build I get maven build error 'env not available'. 

Solution: start Eclipse from commandline.

## When I try to start an application, the console tells me 'Address already in use'!

Run the Maven target 'jetty:stop'. If that does not help, use your operating systems process manager to kill anything running on port 8080. For example:

    kill -9 `lsof -i :8080 -t`
