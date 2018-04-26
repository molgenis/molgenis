# Securing your local development machine

When you deploy molgenis to a server, the server's firewall will shield
the services from unauthorized access.

When you're running on a development machine, you'll want to disallow
access to the database and ElasticSearch index you are running to 
non-localhost clients.

## ElasticSearch

Provide the following configuration items as Runtime Properties when 
running from your IDE tomcat or when running integration tests in maven:

```
-Des.discovery.zen.ping.multicast.enabled=false -Des.network.host=localhost
```

## Firewall settings
If your firewall settings are based on the executables you run, instead
on ports you open/close:

Disallow java, and postgres
to open incoming connections in your firewall.

### macbook
On macbook, you can do this as follows

* Apple, System preferences, Security and privacy,
  * FileVault -> turn it on!
  * Firewall -> turn it on, go to Firewall options
    * Java processes, all of them -> Block incoming connections
    * postgres -> Block incoming connections
    * Automatically allow signed software to receive incoming connections -> uncheck