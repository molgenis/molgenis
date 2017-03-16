** 
This section summarizes the overall architecture of MOLGENIS. Obviously there is much more too it but this should give you a conceptual overview.
**

MOLGENIS is setup as a modular maven project. There is a minimal 'core' that binds everything together. In addition to this core there are many additional software modules that can be optionally be used, depending on needs. Each of these modules is then registered with the 'core'. 

MOLGENIS follows a commonly used three layer architecture:

* Frontend runs in the web browser using HTML + CSS + javascript pages, mostly interactive using AJAX
* Middleware runs on a web server using Java + Tomcat. 
* Backend runs runs either embedded in the web server (e.g. elasticsearch) or is a seperate backend service (e.g. postgresql)

The figure below summarizes this architecture.

![Architecture overview](../images/architecture.png?raw=true, "architecture")