# molgenis-app

## Context hierarchy
The web application has multiple Spring ```DispatcherServlet``` with each of the servlets having their own application context as well as a parent context containing the shared beans.

### Root context
Contains configuration that is shared with the other contexts.
 
### API context
Context containing public API org.molgenis.api classes and configuration. 

### Web context 
Context containing all org.molgenis (except org.molgenis.api) classes and configuration. 