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
      summary: Logs in the user to MOLGENIS
      description:
        Awesome usage of the MOLGENIS v1 login thingy
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
  /v2/{entity_name}:
    get:
      tags:
        - V2
      summary: Retrieves an entity collection
      description:
        Retrieves an entity collection based on entity name
      parameters:
        - name: entity_name
          in: path
          type: string
          description: Name of the entity
          required: true
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
  /v2/{entity_name}/{id}:
    get:
      tags:
        - V2
      summary: Retrieves an entity
      description:
        Retrieves an entity instance based on entity name and ID
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