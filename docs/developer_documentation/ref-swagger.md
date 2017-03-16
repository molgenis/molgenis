# Swagger specification of the REST API
The Molgenis REST API v1 and v2 are documented using the [Swagger specification](http://www.swagger.io/)
and you can browse the endpoints using the Swagger UI.

**
This is a work in progress and not all endpoints are fully documented yet.
**

## How to use
The Swagger UI and the specification are served by the Swagger plugin.
### Sending a request
Trying out the end points should be pretty straightforward.
Each method is described, has a couple form elements that allow you to fill in request body and/or
request parameters and at the bottom left you'll find a `Try it out!` button that will send the
request to the server.

### Permissions
Admins can hand out plugin permissions on the Swagger plugin in the Permission Manager.
Only users with view permission on the plugin can access the swagger specification.
The specification uses enums for the `entity_name` parameter, and fills in the entities that the
user can see.

### URL
Admins can add the plugin to the menu in the Menu Manager. Then users can access it by selecting the menu item.
If you do not wish to add it to the menu, users with plugin permissions can still access the plugin on the url 
`<server>/plugin/swagger/`.

## Authorization
The Molgenis REST api supports two forms of authentication: The standard session token header and 
an x-molgenis-token header.

### ApiKey
The Swagger 2 specification only supports ApiKey authorization.
So the Molgenis Swagger plugin generates a Molgenis token and fills it in as the ApiKey in the Swagger UI.
This token will then be added as an `x-molgenis-token` header by the Swagger UI to each request.
You can view and edit the token by clicking the dark green `Authorize` button at the top of the page.

![Api key authorization](../images/swagger/apikey.png?raw=true, "swagger/apikey")