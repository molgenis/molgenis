# Migration
The versions

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

```
cd /tmp/
psql molgenis < molgenis.sql
```

#### FileStore
Go to a terminal and tar you files into an archive (as root-user):

```
cd /tmp/
tar -pzcvf molgenis.tar.gz /home/molgenis/data
```

Restore the filestore on the new server (as root-user):

```bash
cd /tmp/
tar -pxvzf molgenis.tar.gz
```

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

### EMX models and other configuration
The following changes require manual actions (if applicable for your application):

- MREF as labels are not allowed anymore. You should use template expressions. Please check: [template expressions](../user_documentation/import-data/ref-emx.md#template).
- If users or groups were giving special permissions on the _Settings Manager_ Plugin, these permissions should be set again for the replacement _Settings_ Plugin. The plugin itself is replaced automatically.
   

