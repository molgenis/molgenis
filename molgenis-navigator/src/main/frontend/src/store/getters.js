// @flow
import type {PathComponent, State} from '../flow.types'

const getters = {
  query: (state: State): ?string => state.route.query.q,
  folderId: (state: State): ?string => state.route.params.folderId,
  folderPath: (state: State): Array<PathComponent> => {
    let folderPath = []
    if (state.folder) {
      let folder = state.folder
      while (folder) {
        folderPath.push({id: folder.id, label: folder.label})
        folder = folder.parent
      }
      folderPath.reverse()
    }
    return folderPath
  },
  nrSelectedResources: (state: State): number => state.selectedResources.length,
  nrClipboardResources: (state: State): number => state.clipboard ? state.clipboard.resources.length : 0
}
export default getters
