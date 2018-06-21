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

export type GroupMember = {
  userId: string,
  username: string,
  roleName: string,
  roleLabel: string
}

export type SecurityModel = {
  user: User,
  groups: Array<Group>,
  groupMembers: { [string]: Array<GroupMember> }
  toast: ?Toast
}

export type CreateGroupCommand = {
  groupIdentifier: string,
  name: string
}
