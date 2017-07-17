import fetch from 'isomorphic-fetch'

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
    }, error => {
      console.log(error)
    })
  }
}
