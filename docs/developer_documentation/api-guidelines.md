# API Design Guidelines

When writing an API for a new module or feature, strive to keep it small and concise. 

We use Swagger to document our APIs. See the [Swagger reference](ref-swagger.md) for more information on how to set this 
up for your module.

### API naming convention
When adding a new API, we adhere to the following naming scheme:

`/api/<api_human_readable_identifier>/v<api_version>/*`

For example:

| API | URI | Endpoints |
|:---|:---|:---------|
| Authentication API | `/api/authentication/v1` | `/login`, `/logout` |                    
| REST API | `/api/rest/v1` | REST CRUD operations on resources and resource collections |
| Metadata API | `/api/metadata/v1` | ... |
| Version API | `/api/version/v1` | `/version` |
| Import/Export API | `/api/importexport/v1` | `/import`, `/export` |

_Note: This naming convention hasn't been applied to all internal APIs yet, but will be in the future. For an overview 
of available APIs, see the Swagger plugin in a running MOLGENIS app._

### Versioning
Up the version when:
- the response for existing endpoints has changed?
- new endpoints are added?
- the API has undergone a major overhaul?

### Cross-Origin-Resource-Sharing
MOLGENIS supports [CORS requests](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS), which are enabled for everything behind `/api`.  

- Allowed methods: DELETE
- Allowed headers: x-molgenis-token
