// @flow
export const searchall = window.searchall || {}

export type State = {
  query: ?string,
  result: ?Result,
  error: ?string,
  submitted: ?boolean
}

const state: State = {
  query: '',
  result: null,
  error: '',
  submitted: false
}

export type Result = {
  packages: Array<Package>,
  entityTypes: Array<EntityType>,
}

export type EntityType = {
  id: string,
  label: string,
  attributes: Array<Attribute>,
  nrOfDataRows: number
}

export type Package = {
  id: string,
  label: string
}

export type Attribute = {
  id: string,
  label: string
}

export default state
