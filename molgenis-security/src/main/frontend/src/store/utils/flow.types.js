// @flow
export type Sort = 'ascending' | 'descending'

export type Role = {
  id: string,
  label: string
}

export type UserOrGroup = {
  type: string,
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
  query: ?string,
  sort: ?string,
  members: Array<Member>,
  member: ?Member,
  roles: Array<Role>,
  usersGroups: Array<UserOrGroup>
}
