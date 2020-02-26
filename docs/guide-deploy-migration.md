# Operational migration
There are a few major changes in the migration from 7.x to 8.x.
- The PostgreSQL version was 9.6 and is now 11.0.1
- We're transitioning the filestore to [Minio](https://min.io/), but the old filestore is still in use.

There are a few services you need to migrate data from.

- [PostgreSQL](#postgresql)
  - for 7.x PostgreSQL 9.6
  - for 8.x PostgreSQL 11.0.1
  
#### MOLGENIS home dir
The file store and server properties are located in the MOLGENIS home dir.
The location of this directory is configured as follows:
- `molgenis.home` runtime property if specified in the tomcat catalina options, otherwise
- `molgenis.home` environment variable if specified, otherwise
- `.molgenis` dir in the user's home dir
  
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
