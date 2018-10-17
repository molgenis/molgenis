// @flow
import type { Item, ItemType } from '../flow.types'

export function createItem (type: ItemType, id: string, label: string, description: ?string, readonly: boolean): Item {
  return {type: type, id: id, label: label, description: description, readonly: readonly}
}

export function createItemEntityType (id: string, label: string, description: ?string, readonly: boolean): Item {
  return createItem('entityType', id, label, description, readonly)
}

export function createItemPackage (id: string, label: string, description: ?string, readonly: boolean): Item {
  return createItem('package', id, label, description, readonly)
}
