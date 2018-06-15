// @flow
import type {Group, SecurityModel} from '../flow.type'

const getters = {
  groups: (state: SecurityModel): Array<Group> => {
    return state.groups
  }
}
export default getters
