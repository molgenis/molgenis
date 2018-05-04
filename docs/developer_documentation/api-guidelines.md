# API Design Guidelines

When writing an API for a new module or feature, strive to keep it small and concise. 

We use Swagger to document our APIs. See the [Swagger reference](ref-swagger.md) for more information on how to set this 
up for your module.

### Public vs internal APIs
There's a difference between public and internal APIs: internal APIs are tailor made for specific plugins and are subject
to change. Public APIs are stable and versioned and can be used by third parties when integrating with MOLGENIS.

### API naming convention
When adding a new API, we adhere to the following naming scheme:

Public APIs:
`/api/<api_human_readable_identifier>/v<api_version>/*`

Internal APIs:
`/api/internal/<api_human_readable_identifier>/*`
Internal APIs don't have a version because the corresponding plugins will evolve together with the API.

For example:

| API | URI | Endpoints |
|:---|:---|:---------|
| Authentication API | `/api/auth/v1` | `/login`, `/logout` |                    
| REST API | `/api/rest/v1` | REST CRUD operations on resources and resource collections |
| Metadata API | `/api/internal/meta/v1` | ... |
| Version API | `/api/version/v1` | `/version` |
| Import API | `/api/import/v1` | `/import` |
| Export API | `/api/export/v1` | `/export` |
| Job API | `/api/job/v1/` | `/run`, `/jobs` |

_Note: This naming convention hasn't been applied to any APIs yet, but will be in the future. For an overview 
of available APIs, see the API Documentation plugin in a running MOLGENIS app._

### Versioning
Use [semantic versioning](https://semver.org), showing MAJOR version number in the API URL.
* Up the MAJOR version when you make incompatible API changes,
* Up the MINOR version when you add functionality in a backwards-compatible manner, and
* Up the PATCH version when you make backwards-compatible bug fixes

### Cross-Origin-Resource-Sharing
MOLGENIS supports [CORS requests](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS), which are enabled for everything
behind `/api` except `/api/internal`.  

- Allowed origins: *
- Allowed methods: any
- Allowed headers: any
