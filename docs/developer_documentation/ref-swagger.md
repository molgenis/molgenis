# Swagger specification for MOLGENIS
Swagger documentation can be divided into 2 sections:

* REST API
* Module specific controllers

## How to use
The Swagger UI and the specification are served by the Swagger plugin.

### Sending a request
Trying out the end points should be pretty straightforward.
Each method is described, has a couple form elements that allow you to fill in request body and/or
request parameters and at the bottom left you'll find a `Try it out!` button that will send the
request to the server.

## Specification of the REST API
The Molgenis REST API v1 and v2 are documented using the [Swagger specification](http://www.swagger.io/)
and you can browse the endpoints using the Swagger UI.

**
This is a work in progress and not all endpoints are fully documented yet.
**

### Permissions
Admins can hand out plugin permissions on the Swagger plugin in the Permission Manager.
Only users with view permission on the plugin can access the swagger specification.
The specification uses enums for the `entity_name` parameter, and fills in the entities that the
user can see.

### URL
By default, the API documentation is added to the menu.
If you remove it from the menu, users with plugin permissions can still access the plugin on the url 
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
 
## Specification of module specific controllers
When you toggle the listbox in the header of the swagger documentation, you can choose different MOLGENIS-modules.

![Swagger toggle modules](../images/swagger/toggle.png?raw=true, "swagger/toggle")

The workflow in swagger is pretty much the same as in the REST API documentation. Authentication does not work in this part of the MOLGENIS API documentation.

### Contribute to module-API documentation
When you want to contribute to MOLGENIS and are developing a module, there are a few components that you have to configure.

- Update module pom with swagger-maven-plugin:
  ```
  <plugin>
    <groupId>com.github.kongchen</groupId>
    <artifactId>swagger-maven-plugin</artifactId>
    <configuration>
      <apiSources>
        <apiSource>
          <info>
            <title>${project.name}</title>
            <version>${project.version}</version>
            <description>${project.description}</description>
          </info>
          <springmvc>true</springmvc>
          <locations>
            <location>#location of controller#</location>
          </locations>
          <swaggerDirectory>${build.directory}/generated-resources/swagger</swaggerDirectory>
          <swaggerFileName>${project.name}-swagger</swaggerFileName>
        </apiSource>
      </apiSources>
    </configuration>
    <executions>
      <execution>
        <phase>compile</phase>
        <goals>
          <goal>generate</goal>
        </goals>
      </execution>
    </executions>
  </plugin>
  ```
- Add module to view-swagger-ui.ftl
  ```
  urls: [
    { name: 'molgenis', url: '${molgenisUrl}' },
    { name: 'core-ui', url: '${baseUrl}/swagger/core-ui-swagger.json'}
    { name: '#module name from pom.xml#', url: '${baseUrl}/swagger/#module name from pom.xml#-swagger.json'} 
  ],
  ``` 
- Add swagger-annotations dependency to module pom.xml
  ```
  <dependency>
    <groupId>io.swagger</groupId>
    <artifactId>swagger-annotations</artifactId>
  </dependency>
  ```
  Do not specify a version. The version is maintained by the master-pom.
- Add annotations to controller
  ```
  @Api("Example API")
  @Controller
  @RequestMapping(URI)
  public class ExamplePluginController extends PluginController
  {

  	@ApiOperation("Example operation")
  	@ApiResponses({
  			@ApiResponse(code = 200, message = "This is an example operation", response = String.class)
  	})
  	@GetMapping
  	public String exampleOperation(@RequestParam(defaultValue = "false") boolean exmapleBoolean)
  	{
  ```
  For real life examples see the `UserAccountController.java` in MOLGENIS.
  