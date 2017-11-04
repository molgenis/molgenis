// @flow
export type Alert = {
  'message': ?string,
  'type': ?string
}

export type User = {
  id: string,
  label: string,
  active: boolean
}

export type Group = {
  id: string,
  label: string,
  active: boolean
}

export type GroupMembership = {
  id: string,
  group: string,
  user: string,
  start: string,
  end: ?string
}

export type GroupResponse = {
  id: string,
  parent?: GroupResponse
}
export type UserResponse = {
  id: string
}

export type GroupMembershipResponse = {
  id: string,
  user: UserResponse,
  group: GroupResponse,
  start: string,
  end: string
}

export type GroupRoleMutation = {
  groupId: string,
  roleId: string
}

export type UserGroupMembershipDeletion = {
  userId: string,
  groupId: string,
}

export type GroupMembershipMutation = {
  userId: string,
  groupId: string,
  start: string,
  end: ?string
}

export type Sort = 'ascending' | 'descending'

// The simpler UI datatype, provided by a getter
export type Member = {
  type: string,
  id: string,
  label: string,
  role: string,
  from: string,
  until: ?string
}

export type State = {
  loading: number,
  alerts: Array<Alert>,
  filter: ?string,
  sort: ?string,
  groupMemberships: Array<GroupMembership>,
  users: { [string]: User },
  groups: { [string]: Group },
  route?: {
    params: {
      groupId?: string,
      membershipId?: string
    }
  }
}
