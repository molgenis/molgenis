# Notes from upgrading to Java 9

#Compiling
## Api packages partially included in the JDK
Documentation of maven compiler plugin suggests to add `<release>9</release>` tag.
Packages javax.xml.bind and javax.annotation have only part of their classes included in the JDK.
As a "Fix" for this, the included classes are no longer picked up by the compiler unless you create a module.info and
explicitly include the package.
I added maven jars for the API packages to pom.xml to get tomcat to compile.

#Running
## Tomcat 8
The endorsed dir is not supported in Java 9 but gets set by catalina.sh in 7.0.76.
I upgraded to 7.0.82, didn't help.

```
[2018-01-19 02:27:23,580] Artifact molgenis-app:war exploded: Waiting for server connection to start artifact deployment...
-Djava.endorsed.dirs=/usr/local/Cellar/tomcat@7/7.0.82/libexec/endorsed is not supported. Endorsed standards and standalone APIs
Error: Could not create the Java Virtual Machine.
in modular form will be supported via the concept of upgradeable modules.
Error: A fatal exception has occurred. Program will exit.
Disconnected from server
```

Added JAVA_ENDORSED_DIRS with empty value, didn't help.

Upgraded to tomcat 8.5.24, tomcat starts!
Previously we ran into trouble where resources don't get updated if you are live editing them.
Still not fixed.
In `conf/context.xml` added `<Resources cachingAllowed="false"/>` to the `<Context>` element.
Didn't help.

## Spring 5
Spring 4 gives errors with