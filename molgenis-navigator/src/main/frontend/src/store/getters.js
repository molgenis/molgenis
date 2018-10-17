// @flow
import type {Package, State} from '../flow.types'

const getters = {
  query: (state: State): ?string => state.route.query.q,
  packageId: (state: State): ?string => state.route.params.package,
  packagePath: (state: State): Array<Package> => {
    let packagePath = []
    if (state.package) {
      let _package = state.package
      while (_package) {
        packagePath.push(_package)
        _package = _package.parent
      }
      packagePath.reverse()
    }
    return packagePath
  },
  nrSelectedItems: (state: State): number => state.selectedItems.length,
  nrClipboardItems: (state: State): number => state.clipboard ? state.clipboard.items.length : 0
}
export default getters
