<#-- @formatter:off -->
swagger: '2.0'
info:
  title: MOLGENIS REST API
  description: Documentation for the MOLGENIS Rest API V1 and V2
  version: "v1 and v2"
host: ${host}
schemes:
  - ${scheme}
basePath: /
consumes:
  - application/json
  - application/x-www-form-urlencoded
  - multipart/form-data
produces:
  - application/json
security:
  - token: []
securityDefinitions:
  token:
    type: apiKey
    in: header
    name: x-molgenis-token
paths:
  /api/searchall/search:
    get:
      tags:
        - Search All
      description: Searches for a search term in all data and metadata
      parameters:
        - in: query
          name: term
          type: string
          description: search term
          required: true
      produces:
        - application/json
      responses:
        200:
          description: Returns search results
          schema:
            $ref: "#/definitions/SearchAllResult"
  /plugin/one-click-importer/upload:
    post:
      tags:
        - OneClickImporter
      summary: Upload files without metadata.
      description: Upload files without metadata. Metadata is guessed based on the values in the file. Able to guess String, Numbers and Dates. Supports Excel, CSV, or zip containing CSV files
      consumes:
        - multipart/form-data
      produces:
        - text/html
      parameters:
        - in: formData
          name: file
          type: file
          description: The file you want to upload
      responses:
        200:
          description: Returns the Job ID of the scheduled OneClickImportJob
  /scripts/{name}/start:
    get:
      tags:
        - Scripts
      summary: Starts a Script Job.
      description: Will redirect the request to the jobs controller, showing the progress of the started ScriptJobExecution. The Script's output will be written to the log of the ScriptJobExecution. If the Script has an outputFile, the URL of that file will be written to the ScriptJobExecution's resultUrl. The Swagger UI can only be used to start Scripts without parameters. To start Scripts with parameters, make a regular call to the API.
      parameters:
        - name: name
          type: string
          in: path
          required: true
          description: The name of the script to start
      responses:
        302:
          description: URL of a page showing the ScriptJobExecution
    post:
      tags:
        - Scripts
      summary: Starts a Script Job.
      description: Will redirect the request to the jobs controller, showing the progress of the started ScriptJobExecution. The Script's output will be written to the log of the ScriptJobExecution. If the Script has an outputFile, the URL of that file will be written to the ScriptJobExecution's resultUrl. The Swagger UI can only be used to start Scripts without parameters. To start Scripts with parameters, make a regular call to the API.
      parameters:
        - name: name
          type: string
          in: path
          description: The name of the script to start
          required: true
      responses:
        302:
          description: URL of a page showing the ScriptJobExecution
  /scripts/{name}/run:
    get:
      tags:
        - Scripts
      summary: Runs a Script, waits for the result, serves the result
      description: The Swagger UI can only be used to run Scripts without parameters. To start Scripts with parameters, make a regular call to the API.
      parameters:
        - name: name
          type: string
          in: path
          description: The name of the Script to run
          required: true
      responses:
        302:
          description: If the result has an outputFile, will redirect to a URL where you can download the result file.
        200:
          description: Otherwise, if the result has output, will write the script output to the response and serve it as /text/plain.
        400:
          description: If the Script name is unknown or one of the Script's parameter values is missing
    post:
      tags:
        - Scripts
      summary: Runs a Script, waits for the result, serves the result
      description: The Swagger UI can only be used to run Scripts without parameters. To start Scripts with parameters, make a regular call to the API.
      parameters:
        - name: name
          type: string
          in: path
          description: The name of the Script to run
          required: true
      responses:
        302:
          description: If the result has an outputFile, will redirect to a URL where you can download the result file.
        200:
          description: Otherwise, if the result has output, will write the script output to the response and serve it as /text/plain.
        400:
          description: If the Script name is unknown or one of the Script's parameter values is missing
  /plugin/jobs/run/{scheduledJobId}:
    post:
      tags:
        - Jobs
      summary: Runs a job
      parameters:
        - name: scheduledJobId
          in: path
          type: string
          required: true
      responses:
        200:
          description: ok
  /api/v1/login:
    post:
      tags:
        - V1
      summary: Logs into a MOLGENIS user account
      parameters:
        - name: body
          in: body
          description: User credentials
          required: true
          schema:
            $ref: '#/definitions/LoginRequest'
      responses:
        200:
          description: MOLGENIS token
          schema:
            $ref: '#/definitions/LoginResponse'
        default:
          description: Unexpected error
          schema:
            $ref: '#/definitions/Error'
  /api/v2/version:
    get:
      tags:
        - V2
      summary: Retrieves the MOLGENIS version
      description: Retrieves the MOLGENIS version
      responses:
        200:
          description: Server version
  /api/v2/i18n:
    get:
      produces:
        - application/json
      tags:
        - V2
      summary: Get all localization tokens for the user's current language
      responses:
        200:
          description: JSON object with token as key, translation as value
  /api/v2/i18n/{namespace}/{language}:
    get:
      produces:
        - application/json;charset=UTF-8
      tags:
        - V2
      parameters:
        - name: namespace
          in: path
          type: string
          description: the localization namespace
          required: true
        - name: language
          in: path
          type: string
          description: language code for the items to be retrieved
          required: true
      summary: Retrieves the localization values in this namespace for this locale
      responses:
        200:
          description: JSON file
  /api/v2/i18n/{namespace}_{language}.properties:
    get:
      produces:
        - text/plain;charset=UTF-8
      tags:
        - V2
      parameters:
        - name: namespace
          in: path
          type: string
          description: the localization namespace
          required: true
        - name: language
          in: path
          type: string
          description: language code for the items to be retrieved
          required: true
      summary: Retrieves a properties file for the localization values in this namespace for this locale
      responses:
        200:
          description: Properties file
  /api/v2/i18n/{namespace}:
    post:
      tags:
        - V2
      summary: Adds missing keys to a namespace
      parameters:
        - name: namespace
          in: path
          type: string
          description: the localization namespace
          required: true
        - name: _t
          in: formData
          description: time of submission
          type: string
          required: false
        - name: placeholder_key
          in: formData
          description: one or more keys to add to this namespace
          type: string
          required: false
      responses:
        201:
          description: Keys were created
    delete:
      tags:
        - V2
      summary: Deletes an entire namespace
      parameters:
        - name: namespace
          in: path
          type: string
          description: the localization namespace
          required: true
      responses:
        204:
          description: Deleted namespace
  /api/v2/{entity_name}:
    get:
      tags:
        - V2
      summary: Retrieves an entity collection
      description: Retrieves an entity collection based on entity name
      parameters:
        - name: entity_name
          in: path
          type: string
          description: Name of the entity
          required: true
          enum:
<#list entityTypes as entityType>
            - ${entityType}
</#list>
        - name: attrs
          type: array
          items:
            type: string
          collectionFormat: csv
          in: query
          description: Defines which fields from the Entity to select. For each attribute that references another entity, may be postfixed with the attrs to fetch for that entity, between (). Special attribute names are ~id and ~lbl for the idAttribute and labelAttribute respectively.
        - name: q
          type: string
          in: query
          description: RSQL query to filter the Entity collection response
        - name: aggs
          type: string
          in: query
          description: "RSQL query to filter the Entity collection aggregates. The aggregation query supports the RSQL selectors 'x', 'y' and 'distinct' and the RSQL operator '=='. The selector 'x' defines the first aggregation attribute name, 'y' defines the second aggregation attribute name, 'distinct' defines the distinct aggregation attribute name."
        - name: sort
          type: array
          items:
            type: string
          collectionFormat: csv
          in: query
          description: "Sort specification. Format is a comma separated list of attribute names. Each name may be followed by :asc or :desc to indicate sort order. Default sort order is ascending."
        - name: start
          type: integer
          format: int32
          default: 0
          minimum: 0
          in: query
          description: Offset in resource collection
        - name: num
          type: integer
          format: int32
          default: 0
          minimum: 0
          maximum: 10000
          in: query
          description: Number of resources to retrieve starting at start
      responses:
        200:
          description: OK
          schema:
            $ref: "#/definitions/EntityCollectionResponseV2"
        400:
          description: "Bad request. Happens if arguments are invalid or conversions fail, or a MolgenisDataException is thrown during the execution of the request."
          schema:
            $ref: "#/definitions/ErrorMessageResponse"
        401:
          description: "The user should have READ or (in the case of aggs) COUNT permission on the entity."
          schema:
            $ref: "#/definitions/ErrorMessageResponse"
        500:
          description: "Internal Server Error. Happens if a RuntimeException is thrown during the execution of the request"
          schema:
            $ref: "#/definitions/ErrorMessageResponse"
    post:
      tags:
        - V2
      summary: Retrieves an entity collection
      description: Retrieves an entity collection based on entity name
      parameters:
        - name: _method
          type: string
          in: query
          enum:
            - GET
          description: Tunnels the GET method over a POST request, allowing you to put the request in the body
          required: true
        - name: entity_name
          in: path
          type: string
          description: Name of the entity
          required: true
          enum:
<#list entityTypes as entityType>
            - ${entityType}
</#list>
        - name: body
          in: body
          description: Entity collection retrieval request
          required: true
          schema:
            $ref: '#/definitions/EntityCollectionRequestV2'
      responses:
        200:
          description: OK
          schema:
            $ref: "#/definitions/EntityCollectionResponseV2"
        400:
          description: "Bad request. Happens if arguments are invalid or conversions fail, or a MolgenisDataException is thrown during the execution of the request."
          schema:
            $ref: "#/definitions/ErrorMessageResponse"
        401:
          description: "The user should have READ or (in the case of aggs) COUNT permission on the entity."
          schema:
            $ref: "#/definitions/ErrorMessageResponse"
        500:
          description: "Internal Server Error. Happens if a RuntimeException is thrown during the execution of the request"
          schema:
            $ref: "#/definitions/ErrorMessageResponse"
  /api/v2/{entity_name}/{id}:
    get:
      tags:
        - V2
      summary: Retrieves an entity
      description: Retrieves an entity instance based on entity name and ID
      parameters:
        - name: entity_name
          in: path
          type: string
          description: Name of the entity
          required: true
          enum:
<#list entityTypes as entityType>
            - ${entityType}
</#list>
        - name: id
          in: path
          type: string
          description: ID of the entity instance
          required: true
        - name: attrs
          type: string
          in: query
          description: Defines which fields from the Entity to select
        - name: includeCategories
          type: boolean
          in: query
          required: false
          description: Flag to include category options as part of meta data, if not set defaults to false.
        - name: _method
          type: string
          in: query
          description: Tunnel request through defined method over default API operation
      responses:
        200:
          description: OK
    delete:
      tags:
        - V2
      summary: Deletes an entity
      description: Deletes an entity instance based on entity name and ID
      parameters:
        - name: entity_name
          in: path
          type: string
          description: Name of the entity
          required: true
        - name: id
          in: path
          type: string
          description: ID of the entity instance
          required: true
      responses:
        204:
          description: No content
  /api/v2/{entity_name}/meta/{attribute_name}:
    get:
      tags:
        - V2
      summary: Retrieve attribute metadata
      description: Retrieve attribute metadata based on entity name and attribute name
      parameters:
        - name: entity_name
          in: path
          type: string
          description: Name of the entity
          required: true
        - name: attribute_name
          in: path
          type: string
          description: Name of the attribute
          required: true
        - name: _method
          type: string
          in: query
          enum:
            - POST
            - GET
          description: Tunnel request through defined method over default API operation
      responses:
        200:
          description: OK
  /api/v2/copy/{entity_name}:
    post:
      tags:
        - V2
      summary: Creates a copy of an entity.
      description: The copy will be created in the same package and backend as the original entity, so both must be writable.
      parameters:
        - name: entity_name
          in: path
          type: string
          description: Name of the entity
          required: true
          enum:
<#list entityTypes as entityType>
            - ${entityType}
</#list>
        - name: body
          schema:
            $ref: "#/definitions/CopyEntityRequest"
          in: body
          required: true
      responses:
        400:
          description: For an unknown entity.
          schema:
            $ref: "#/definitions/ErrorMessageResponse"
        401:
          description: If the user lacks the proper permissions.
          schema:
            $ref: "#/definitions/ErrorMessageResponse"
        500:
          description: If a runtime exception occurs.
          schema:
            $ref: "#/definitions/ErrorMessageResponse"
  /plugin/mappingservice/map:
    post:
      tags:
        - mapping service
      produces:
        - text/plain
      summary: Run mapping service
      description: Runs the mappings in a mapping project.
      parameters:
        - name: mappingProjectId
          in: query
          type: string
          required: true
          description: ID of the mapping project
        - name: targetEntityTypeId
          in: query
          type: string
          required: true
          description: ID of the created EntityType, may be an existing EntityType
        - name: addSourceAttribute
          in: query
          type: boolean
          required: false
          description: indicates if a source attribute should be added to the EntityType, ignored when mapping to an existing EntityType
        - name: packageId
          in: query
          type: string
          required: false
          description: ID of the target package, ignored when mapping to an existing EntityType
        - name: label
          in: query
          type: string
          required: false
          description: label of the target EntityType, ignored when mapping to an existing EntityType
      responses:
        201:
          description: If the mapping job was successfully created
          headers:
            Location:
              description: The HREF where the mapping job can be found
              type: string
              format: uri
definitions:
  SearchAllResult:
    type: object
    properties:
      entityTypes:
        type: array
        items:
          $ref: "#/definitions/SearchAllEntityTypeResult"
      packages:
        type: array
        items:
          $ref: "#/definitions/SearchAllPackageResult"
    required:
      - entityTypes
      - packages
  SearchAllEntityTypeResult:
    type: object
    properties:
      id:
        type: string
      label:
        type: string
      description:
        type: string
      packageId:
        type: string
      labelMatch:
        type: boolean
      descriptionMatch:
        type: boolean
      attributes:
        type: array
        items:
          $ref: "#/definitions/SearchAllAttributeResult"
    required:
      - id
      - label
      - packageId
      - labelMatch
      - descriptionMatch
      - attributes
  SearchAllAttributeResult:
    type: object
    properties:
      label:
        type: string
      description:
        type: string
      dataType:
        type: string
    required:
      - label
      - dataType
  SearchAllPackageResult:
    type: object
    properties:
      id:
        type: string
      label:
        type: string
      description:
        type: string
    required:
      - id
      - label
  CopyEntityRequest:
    type: object
    properties:
      newEntityName:
        type: string
    required:
      - newEntityName
  LoginRequest:
    type: object
    properties:
      username:
        type: string
      password:
        type: string
  LoginResponse:
    type: object
    properties:
      token:
        type: string
      username:
        type: string
  EntityCollectionRequestV2:
    type: object
    properties:
      q:
        type: string
      aggs:
        type: string
      sort:
        type: string
      attrs:
        type: string
      start:
        type: integer
        format: int32
        default: 0
        minimum: 0
        description: Offset in resource collection
      num:
        type: integer
        format: int32
        default: 0
        minimum: 0
        maximum: 10000
        description: Number of resources to retrieve starting at start
  EntityCollectionResponseV2:
    type: object
    properties:
      href:
        type: string
      meta:
        $ref: "#/definitions/EntityTypeResponseV2"
      start:
        type: integer
        format: int32
      num:
        type: integer
        format: int32
      total:
        type: integer
        format: int64
      prevHref:
        type: string
      nextHref:
        type: string
      items:
        type: array
        items:
          type: object
    required:
      - href
      - meta
      - start
      - num
      - total
      - items
  EntityTypeResponseV2:
    type: object
    properties:
      href:
        type: string
      hrefCollection:
        type: string
      name:
        type: string
      label:
        type: string
      attributes:
        type: array
        items:
          $ref: "#/definitions/AttributeResponseV2"
      labelAttribute:
        type: string
      idAttribute:
        type: string
      lookupAttributes:
        type: array
        items:
          type: string
      isAbstract:
        type: boolean
      writable:
        type: boolean
      languageCode:
        type: string
    required:
      - href
      - hrefCollection
      - name
      - label
      - attributes
      - labelAttribute
      - idAttribute
      - lookupAttributes
      - isAbstract
      - writable
      - languageCode
  AttributeResponseV2:
    type: object
    properties:
      href:
        type: string
      fieldType:
        type: string
        enum:
<#list attributeTypes as attributeType>
          - ${attributeType?upper_case}
</#list>
      name:
        type: string
      label:
        type: string
      description:
        type: string
      attributes:
        type: array
        items:
          $ref: "#/definitions/AttributeResponseV2"
      enumOptions:
        type: array
        items:
          type: string
      maxLength:
        type: integer
        format: int64
      refEntity:
        $ref: "#/definitions/EntityTypeResponseV2"
      mappedBy:
        type: string
      auto:
        type: boolean
      nillable:
        type: boolean
      readOnly:
        type: boolean
      defaultValue:
        type: string
      labelAttribute:
        type: boolean
      unique:
        type: boolean
      visible:
        type: boolean
      lookupAttribute:
        type: boolean
      isAggregatable:
        type: boolean
      range:
        $ref: "#/definitions/Range"
      expression:
        type: string
      nullableExpression:
        type: string
      visibleExpression:
        type: string
      validationExpression:
        type: string
      categoricalOptions:
        type: array
        items:
          $ref: "#/definitions/CategoricalOptionV2"
    required:
      - href
      - fieldType
      - name
      - label
      - auto
      - nillable
      - readOnly
      - labelAttribute
      - unique
      - visible
      - lookupAttribute
      - isAggregatable
  Range:
    type: object
    properties:
      min:
        type: integer
        format: int64
      max:
        type: integer
        format: int64
  CategoricalOptionV2:
    type: object
    properties:
      id:
        type: object
      label:
        type: object
  Error:
    type: object
    properties:
      code:
        type: integer
        format: int32
      message:
        type: string
      fields:
        type: string
  ErrorMessageResponse:
    type: object
    properties:
      errors:
        type: array
        items:
          $ref: "#/definitions/ErrorMessage"
    required:
      - errors
  ErrorMessage:
    type: object
    properties:
      message:
        type: string
      code:
        type: integer
        format: int32
    required:
      - message