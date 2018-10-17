// @flow
import type { ApiPackage, Folder } from '../flow.types'
import { createFolder } from '../models/Folder'
import { isSystemPackage } from './ItemUtils'

export function createFolderFromApiPackage (apiPackage: ApiPackage): Folder {
  return createFolder(apiPackage.id, apiPackage.label,
    apiPackage.parent ? createFolderFromApiPackage(apiPackage.parent) : null,
    isSystemPackage(apiPackage))
}
