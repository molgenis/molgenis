import type { State } from './utils/flow.types'

export default {
  selectedEntityType (state: State) {
    return state.entityTypes.find(entityType => entityType.id === state.selectedEntityTypeId)
  }
}
