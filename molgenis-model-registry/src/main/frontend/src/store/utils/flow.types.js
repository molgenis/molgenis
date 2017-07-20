// @flow
export type Node = {
  name: String,
  iskey: Bool,
  figure: String,
  color: String
}

export type Link = {
  from: String,
  to: String,
  text: String,
  totext: String
}

export type UmlData = {
  nodes: Array<Node>,
  links: Array<Link>
}

export type Package = {
  name: String
}

export type State = {
  umlData: any,
  molgenisPackage: Package
}
