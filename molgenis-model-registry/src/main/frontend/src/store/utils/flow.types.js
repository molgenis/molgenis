// @flow
export type Attribute = {
  name: string,
  iskey: boolean,
  figure: string,
  color: string
}

export type Node = {
  key: string,
  color: string,
  group: string,
  extends: string,
  items: Array<Attribute>
}

export type Link = {
  from: string,
  to: string,
  text: string,
  totext: string
}

export type UmlData = {
  nodes: Array<Node>,
  links: Array<Link>
}

export type State = {
  error: ?string,
  umlData: any,
  molgenisPackage: string
}
