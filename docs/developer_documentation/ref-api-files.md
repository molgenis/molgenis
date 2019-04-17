**
The Files API allows you do upload/download files.
**
# Endpoints

## Upload file

### Request
```
POST /api/files
...binary data in the body...
```

#### Request Headers
| Name                | Description                                                 | Required | Default value              |
|---------------------|-------------------------------------------------------------|----------|----------------------------|
| Content-Type *      | A standard MIME type describing the format of the file      | No       | application/octet-stream   |
| Content-Length      | File size in bytes                                          | No       |                            |
| x-molgenis-filename | Filename                                                    | No       | unknown                    |
| x-molgenis-token    | Authentication token                                        | No       | session cookie if supplied |

* All media types with the exception of `application/x-www-form-urlencoded` and `multipart/form-data` are allowed. See the JavaScript example for how to upload a file from a form file input.
### Response
| Status code         | Description                                                                       |
|---------------------|-----------------------------------------------------------------------------------|
| 201 Created         | File uploaded and file metadata created.                                          |
| 403 Forbidden       | If the authenticated user does not have WRITE permissions on file metadata.       |

#### Response Headers
| Name                | Description                                                 |
|---------------------|-------------------------------------------------------------|
| Location            | Location of a newly created resource                        |

#### Response body
Filemeta as returned by the 'Retrieve file metadata' endpoint.

### Examples

#### JavaScript
```javascript
var file = document.getElementById('my-file-input').files[0]
var request = new XMLHttpRequest()
request.open('post', '/api/files', true)
request.setRequestHeader("Content-Type", file.type)
request.setRequestHeader("Content-Length", file.size)
request.setRequestHeader("x-molgenis-filename", file.name)
request.send(file)
```

#### cURL
curl -H 'Content-Type: application/octet-stream' --data-binary @my.file /api/files

## Download file

### Request
```
GET /api/files/myFileId?alt=media
```

### Response
| Status code         | Description
|---------------------|-----------------------------------------------------------------------------------|
| 200 OK              | File downloaded                                                                   |
| 403 Forbidden       | If the authenticated user does not have READ permissions on the file metadata     |
| 404 Not Found       | If the file identifier is unknown                                                 |

## Retrieve file metadata

### Request
```
GET /api/files/myFileId
```

### Response
| Status code         | Description
|---------------------|-----------------------------------------------------------------------------------|
| 200 OK              | File metadata                                                                     |
| 403 Forbidden       | If the authenticated user does not have READ permissions on the file metadata     |

#### Response body
Example:
```json
{
  "id": "aaaac2mp6yoxgaavluayxfiaae",
  "filename": "logo_green.png",
  "contentType": "image/png",
  "size": 11189,
}
```

## Delete file

### Request
```
DELETE /api/files/myFileId
```

### Response
| Status code         | Description
|---------------------|-----------------------------------------------------------------------------------|
| 204 No Content      | File and filemeta deleted.                                                        |
| 403 Forbidden       | If the authenticated user does not have WRITE permissions on the file metadata    |
| 404 Not Found       | If the file identifier is unknown                                                 |