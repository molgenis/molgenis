// @flow
import type { ApiPackage, Package } from '../flow.types'
import { createPackage } from '../models/Package'
import { isSystemPackage } from './ItemUtils'

export function createPackageFromApiPackage (apiPackage: ApiPackage): Package {
  return createPackage(apiPackage.id, apiPackage.label,
    apiPackage.parent ? createPackageFromApiPackage(apiPackage.parent) : null,
    isSystemPackage(apiPackage))
}
