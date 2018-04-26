# Compile MOLGENIS locally

# Get the code

create acount on github.com 

'fork' on http://github.com/molgenis/molgenis

copy the cloneURL

open terminal (mkdir if needed) and type 

    cd ~/git 
    git clone http://github.com/[YOURACCOUNT]/molgenis
    
Optionally select stable molgenis version:

    git fetch --tags origin
    git checkout <tag name: see https://github.com/molgenis/molgenis/releases>

More information about forking can be found here: https://help.github.com/articles/fork-a-repo

# Configure postgresql
If you are unfamiliar with PostGreSQL, follow one of their [PostGreSQL installation guides](https://www.postgresql.org/docs/9.6/static/index.html). Once you have a PostGreSQL server running, open up the included pgAdmin application that is supplied with most PostGreSQL installations, and perform the following actions:

- Add a database 'molgenis'
- Add a user 'molgenis' (password 'molgenis') under Login Roles
- Add 'can create databases' privilege to user 'molgenis'

Now that your database server and properties file have been configured. 

# Set admin password<a name="server-props"></a>

Create directory ~/.molgenis/omx/

    mkdir -p ~/.molgenis/omx/
    
Create the file ~/.molgenis/omx/molgenis-server.properties 

    nano ~/.molgenis/omx/molgenis-server.properties
    
... and add user and database properties to this file:

```
	db_user=molgenis  
	db_password=molgenis  
	db_uri=jdbc\:postgresql\://localhost/molgenis
	admin.password=admin  
	user.password=admin  
```

If these properties are not present, the MolgenisDatabasePopulator will fail (RuntimeException). This properties-file
should be in your home folder, if the file is not there yet, just create it.

# Start MOLGENIS

In IntelliJ 

TODO...

# Update your code

Add the original molgenis repository as a remote location.

    cd ~/git/molgenis
    git remote add blessed https://github.com/molgenis/molgenis.git
    
Perform regular updates so the latest changes are merged with your local clone.

    git pull blessed master
    
And push back any merges or commits of your own to your online fork.

    git push origin master
