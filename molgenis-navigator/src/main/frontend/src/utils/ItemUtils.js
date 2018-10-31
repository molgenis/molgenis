// @flow
import type {
  ApiEntityType,
  Item,
  ItemType,
  ApiPackage,
  RestApiPackage,
  ApiItem
} from '../flow.types'
import { createItemEntityType, createItemPackage } from '../models/Item'

const SYS_PACKAGE_ID = 'sys'

// TODO discuss: is this check to fragile?
function isSystemId (id: string): boolean {
  return id === SYS_PACKAGE_ID || id.startsWith(SYS_PACKAGE_ID + '_')
}

function isSystemEntityType (apiEntityType: ApiEntityType): boolean {
  return isSystemId(apiEntityType.id)
}

export function isSystemPackage (apiPackage: ApiPackage): boolean {
  return isSystemId(apiPackage.id)
}

function toApiItemType (itemType: ItemType) {
  let apiItemType
  switch (itemType) {
    case 'package':
      apiItemType = 'PACKAGE'
      break
    case 'entityType':
      apiItemType = 'ENTITY_TYPE'
      break
    default:
      throw new Error('unexpected item type ' + itemType)
  }
  return apiItemType
}

export function toApiItem (item: Item, allProperties: boolean = false): ApiItem {
  let apiItem = {
    id: item.id,
    type: toApiItemType(item.type),
    label: allProperties ? item.label : undefined,
    description: allProperties ? item.description : undefined
  }
  return apiItem
}

export function createItemFromApiEntityType (apiEntityType: ApiEntityType): Item {
  return createItemEntityType(apiEntityType.id, apiEntityType.label,
    apiEntityType.description, isSystemEntityType(apiEntityType))
}

export function createItemFromApiPackage (apiPackage: ApiPackage): Item {
  return createItemPackage(apiPackage.id, apiPackage.label,
    apiPackage.description, isSystemPackage(apiPackage))
}

export function createRestApiPackageFromItem (item: Item): RestApiPackage {
  return {id: item.id, label: item.label, description: item.description, parent: undefined}
}
