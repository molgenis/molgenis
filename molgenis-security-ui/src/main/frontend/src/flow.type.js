export type Group = {
  name: string,
  description?: string
}

export type SecurityModel = {
  groups: Array<Group>
}
