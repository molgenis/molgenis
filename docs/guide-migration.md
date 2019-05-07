# Migration

## From 7.x to 8.0.x
There are a few major changes in the migration from 7.x to 8.x.
- The PostgreSQL version was 9.6 and is now 11.0.1
- We're transitioning the filestore to [Minio](https://min.io/), but the old filestore is still in use.

### Operational requirements
There are a few services you need to migrate data from.

- [PostgreSQL](#postgresql)
  - for 7.x PostgreSQL 9.6
  - for 8.x PostgreSQL 11.0.1
- [FileStore](#filestore)
  - for 7.x /srv/molgenis/data
  - for 8.x /home/molgenis/data
- [Configuration](#configuration)
  - for 7.x /srv/molgenis/molgenis-server.properties
  - for 8.x /home/molgenis/molgenis-server.properties
  
#### PostgreSQL
Go to a terminal and dump your database using (as root-user):

```bash
cd /tmp/
pg_dump molgenis > molgenis.sql
```

Load you database using on the new server (as root-user):

Be sure you drop the database before you load the customer database.

```
cd /tmp/
psql molgenis < molgenis.sql
```

#### FileStore
Go to a terminal and tar you files into an archive (as root-user):

```
cd /home/molgens/
tar -pzcvf /tmp/molgenis.tar.gz /data
```

Restore the filestore on the new server (as root-user):

```bash
cd /home/molgenis
mv data data_backup
tar -pxvzf molgenis.tar.gz
```

>note: if you use our [Ansible playbook](https://github.com/molgenis/ansible) to deploy MOLGENIS you have to do the following steps:
> - execute: ```chmod -R 770 /home/molgenis```

#### Configuration
Go to a terminal and tar you files into an archive (as root-user):

```
cd /tmp/
cp /home/molgenis/molgenis-server.properties .
```

Restore the filestore on the new server (as root-user):

```bash
cd /tmp/
cp molgenis-server.properties /home/molgenis/ .
```

You need to add additional properties to make the deployment work with MOLGENIS 8.0.x

```
MINIO_ACCESS_KEY=molgenis
MINIO_SECRET_KEY=molgenis
```

### EMX models and other configuration
The following changes require manual actions (if applicable for your application):

- MREF as labels are not allowed anymore. You should use template expressions. Please check: [template expressions](guide-emx.md#template).
    - You can migrate the database first and if you encounter the following error 
    
      ```
      Error:
        Conversion failure in entity type [#name#] attribute [#name#]; No converter found capable of converting from type [org.molgenis.data.support.DynamicEntity] to type [java.lang.String]
      ```  
    
      You can fix these attributes in the Metadata Manager by editing the expression of the mentioned attribute.
- If users or groups were giving special permissions on the _Settings Manager_ Plugin, these permissions should be set again for the replacement _Settings_ Plugin. The plugin itself is replaced automatically.
   

