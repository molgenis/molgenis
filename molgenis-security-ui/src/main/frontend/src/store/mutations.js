// @flow
import type {Group, SecurityModel} from '../flow.type'

const mutations = {
  'SET_GROUPS' (state: SecurityModel, groups: Array<Group>) {
    state.groups = groups
  }
}
export default mutations
