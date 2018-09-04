export type Parent = {
  id: string,
}

export type Package = {
  id: string,
  label: string,
  description: ?string,
  parent: ?Parent
}

export type Entity = {
  id: string,
  type: string,
  label: string,
  description: ?string,
  abstract: boolean
}

export type State = {
  error: ?string,
  token: ?string,
  query: ?string,
  packages: Array<Package>,
  selectedPackageIds: Array<string>,
  entities: Array<Entity>,
  selectedEntityTypeIds: Array<string>,
  path: Array<Package>
}
