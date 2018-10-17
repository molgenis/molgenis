// @flow
import type { Folder } from '../flow.types'

export function createFolder (id: string, label: string, parent: ?Folder, readonly: boolean): Folder {
  return {id: id, label: label, parent: parent, readonly: readonly}
}
