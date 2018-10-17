// @flow
import type { Package } from '../flow.types'

export function createPackage (id: string, label: string, parent: ?Package, readonly: boolean): Package {
  return {id: id, label: label, parent: parent, readonly: readonly}
}
