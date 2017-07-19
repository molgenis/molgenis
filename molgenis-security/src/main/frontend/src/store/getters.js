import type { EntityType, State } from './utils/flow.types'

export default {
  /**
   * Returns the selected EntityType
   * @param state: State the state
   */
  selectedEntityType: (state: State): EntityType => state.entityTypes.find(entityType => entityType.id === state.selectedEntityTypeId)
}
