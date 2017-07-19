import fetch from 'isomorphic-fetch'
import _Promise from 'babel-runtime/core-js/promise'

export const IMPORT = '__IMPORT__'

export default {
  [IMPORT] ({commit}, file) {
    const formData = new FormData()
    formData.append('file', file)

    const options = {
      body: formData,
      method: 'POST',
      credentials: 'same-origin'
    }

    fetch('/plugin/one-click-importer/upload', options).then(response => {
      console.log(response)
      if (response.headers.get('content-type') === 'application/json') {
        return response.json().then(function (json) {
          return response.ok ? json : _Promise.reject(json.errors[0].message)
        })
      } else {
        return response.ok ? response : _Promise.reject(response)
      }
      // const dataSetUri = response.headers.get('Location')
      // const entityId = dataSetUri.substring(dataSetUri.lastIndexOf('/') + 1)
      // window.location.href = 'http://localhost:8080/menu/main/dataexplorer?entity=' + entityId
    }, error => {
      console.log(error)
    })
  }
}
