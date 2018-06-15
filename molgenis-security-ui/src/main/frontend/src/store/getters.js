// @flow
import type { Group, SecurityModel, Toast } from '../flow.type'

const getters = {
  groups: (state: SecurityModel): Array<Group> => {
    return state.groups
  },
  toast: (state: SecurityModel): Toast => {
    return state.toast
  }
}
export default getters
