// @flow
export type Sort = 'ascending' | 'descending'

export type Role = {
  id: string,
  label: string
}

export type User = {
  id: string,
  label: string
}

export type Group = {
  id: string,
  label: string
}

export type Member = {
  type: string,
  id: string,
  label: string,
  role: string,
  from: string,
  until: string
}

export type State = {
  filter: ?string,
  sort: ?string,
  members: Array<Member>,
  member: ?Member,
  roles: Array<Role>,
  users: Array<User>,
  groups:Array<Group>,
  context: Group
}
