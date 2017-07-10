import { postFile } from '@molgenis/molgenis-api-client'

export const IMPORT = '__IMPORT__'

export default {
  [IMPORT] ({commit}, file) {
    postFile({apiUrl: '/api/v2'}, '/one-click-importer', file)
      .then(response => {
        console.log(response)
      }, error => {
        console.log(error)
      })
  }
}
