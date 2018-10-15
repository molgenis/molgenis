// @flow
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

export type Item = {
  type: string,
  id: string,
  label: string
}

export type Clipboard = {
  mode: "cut" | "copy",
  items: Array<Item>
}

export type State = {
  error: ?string,
  token: ?string,
  query: ?string,
  path: Array<Package>,
  items: Array<Item>,
  selectedItems: Array<Item>,
  clipboard: Array<Item>
}
