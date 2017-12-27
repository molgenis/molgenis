// @flow

import { EntityToStateMapper } from '@molgenis/molgenis-ui-form'

const getters = {
  getMappedFields: state => {
    return state.rawSettings &&
      state.rawSettings.meta &&
      EntityToStateMapper.generateFormFields(state.rawSettings.meta)
  }
}
export default getters
