export type Toast = {
  type: 'danger' | 'success',
  message: string
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
  isSuperUser: boolean,
  groups: Array<Group>,
  toast: ?Toast
}
