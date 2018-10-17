// @flow
import type {State} from '../flow.types'

const getters = {
  query: (state: State): ?string => state.route.query.q,
  packageId: (state: State): ?string => state.route.params.package,
  nrSelectedItems: (state: State): number => state.selectedItems.length,
  nrClipboardItems: (state: State): number => state.clipboard ? state.clipboard.items.length : 0
}
export default getters
