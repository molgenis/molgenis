// @flow
export type Node = {
  name: string,
  iskey: boolean,
  figure: string,
  color: string
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

export type Package = {
  name: string
}

export type State = {
  error: ?string,
  umlData: any,
  molgenisPackage: Package
}
