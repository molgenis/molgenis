# Architecture

MOLGENIS is split into backend ( JAVA web application ) and frontend ( js bundles ). 

## Frontend
We are moving towards a solution that let's us deploy the frontend separatly from the MOLGENIS backend.

Each request passed through a reverse proxy ( NGINX ):

If the request starts with /@molgenis-ui it forwarded to https://unpkg.com, else it is passed to tomcat ( MOLGENIS backend ).

This allows for frontend updates without releasing the MOLGENIS backend.

![frontend architecture](images/frontend-architecture.png?raw=true, "frontend-architecture")

## Backend
For the backend we are moving towards a more API oriented environment in this stage we have the following setup.

![backend architecture](images/backend-architecture.png?raw=true, "backend-architecture")


