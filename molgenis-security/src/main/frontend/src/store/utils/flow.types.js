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

export type Optional<T> = {
  value?: T
}

export type GroupResponse = {
  id: Optional<string>,
  parent: Optional<GroupResponse>
}
export type UserResponse = {
  id: Optional<string>
}

export type GroupMembershipResponse = {
  id: Optional<string>,
  user: UserResponse,
  group: GroupResponse,
  start: string,
  end: Optional<string>
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
