export type Toast = {
  type: 'danger' | 'success',
  message: string
}

export type User = {
  name: string,
  isSuperUser: boolean
}

export type Group = {
  name: string,
  label: string
}

export type CreateGroupCommand = {
  groupIdentifier: string,
  name: string
}

export type SecurityModel = {
  user: User,
  groups: Array<Group>,
  toast: ?Toast
}
