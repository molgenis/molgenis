// @flow
import type { EntityType, Item, Package } from '../flow.types'
import { createItemEntityType, createItemPackage } from '../models/Item'

const SYS_PACKAGE_ID = 'sys'

// TODO discuss: is this check to fragile?
function isSystemId (id: string): boolean {
  return id.startsWith(SYS_PACKAGE_ID + '_')
}

function isSystemEntityType (apiEntityType: EntityType): boolean {
  return isSystemId(apiEntityType.id)
}

function isSystemPackage (apiPackage: Package): boolean {
  return isSystemId(apiPackage.id)
}

export function createItemFromApiEntityType (apiEntityType: EntityType): Item {
  return createItemEntityType(apiEntityType.id, apiEntityType.label,
    apiEntityType.description, isSystemEntityType(apiEntityType))
}

export function createItemFromApiPackage (apiPackage: Package): Item {
  return createItemPackage(apiPackage.id, apiPackage.label,
    apiPackage.description, isSystemPackage(apiPackage))
}
