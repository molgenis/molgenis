<#-- @formatter:off -->
swagger: '2.0'
info:
  title: MOLGENIS REST API
  description: Documentation for the MOLGENIS Rest API V2
  version: "v2"
host: molgenis01.gcc.rug.nl
schemes:
  - https
basePath: /api/
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
  /v1/login:
    post:
      tags:
        - V1
      summary: Logs into a MOLGENIS user account
      description: Awesome usage of the MOLGENIS v1 login thingy
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
  /v2/version:
    get:
      tags:
        - V2
      summary: Retrieves the MOLGENIS version
      description: Retrieves the MOLGENIS version
      responses:
        200:
          description: Server version
  /v2/{entity_name}:
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
            - ${entityType.getName()}
</#list>
        - name: attrs
          type: string
          in: query
          description: Defines which fields from the Entity to select
        - name: q
          type: string
          in: query
          description: RSQL query to filter the Entity collection response
        - name: sort
          type: string
          in: query
          description: The name of the attribute to sort on. Optionally followed by :asc or :desc
        - name: start
          type: number
          format: integer
          in: query
          description: Offset in resource collection
        - name: num
          type: number
          format: integer
          in: query
          description: Number of resources to retrieve starting at start
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
          schema:
            $ref: "#/definitions/EntityCollectionResponseV2"
  /v2/{entity_name}/{id}:
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
            - ${entityType.getName()}
</#list>
        - name: id
          in: path
          type: string
          description: ID of the user
          required: true
        - name: attrs
          type: string
          in: query
          description: Defines which fields from the Entity to select
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
          description: ID of the user
          required: true
      responses:
        204:
          description: OK
  /v2/{entity_name}/meta/{attribute_name}:
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
definitions:
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
  EntityCollectionResponseV2:
    type: object
    properties:
      href:
        type: string
      meta:
        "$ref": "#/definitions/EntityTypeResponseV2"
      start:
        type: integer
      num:
        type: integer
      total:
        type: long
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
        type: String
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
        type: long
      refEntity:
        type: object
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
        type: object
        $ref: "#/definitions/Range"
      expression:
        type: string
      visibleExpression:
        type: string
      validationExpression:
        type: string
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
        type: long
      max:
        type: long
#TODO
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