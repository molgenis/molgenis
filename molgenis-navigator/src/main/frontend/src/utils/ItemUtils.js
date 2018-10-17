// @flow
import type { ApiEntityType, Item, ApiPackage } from '../flow.types'
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

export function createItemFromApiEntityType (apiEntityType: ApiEntityType): Item {
  return createItemEntityType(apiEntityType.id, apiEntityType.label,
    apiEntityType.description, isSystemEntityType(apiEntityType))
}

export function createItemFromApiPackage (apiPackage: ApiPackage): Item {
  return createItemPackage(apiPackage.id, apiPackage.label,
    apiPackage.description, isSystemPackage(apiPackage))
}
