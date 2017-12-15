// @flow
export const searchall = window.searchall || {}

export type State = {
  result: ?Result,
  error: ?string,
  loading: boolean
}

const state: State = {
  result: {
    query: '',
    response: null
  },
  error: '',
  loading: false
}

export type ApiResponse = {
  packages: Array<Package>,
  entityTypes: Array<EntityType>,
}

export type Result = {
  query: ?string,
  response: ?ApiResponse
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
