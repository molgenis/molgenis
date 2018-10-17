// @flow
import type {Folder, State} from '../flow.types'

const getters = {
  query: (state: State): ?string => state.route.query.q,
  folderId: (state: State): ?string => state.route.params.folderId,
  folderPath: (state: State): Array<Folder> => {
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
  nrSelectedItems: (state: State): number => state.selectedItems.length,
  nrClipboardItems: (state: State): number => state.clipboard ? state.clipboard.items.length : 0
}
export default getters
