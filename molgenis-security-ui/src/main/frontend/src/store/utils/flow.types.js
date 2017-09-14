// @flow

export type GrantedAuthoritySid = {
  authority: string
}

export type PrincipalSid = {
  username: string
}

export type Sid = GrantedAuthoritySid | PrincipalSid

export type Role = {
  id?: string,
  label: string,
  description ?: string,
  users?: string[],
  groups?: string[]
}

export type User = {
  active: boolean,
  changePassword: boolean,
  id: string,
  superuser: boolean,
  use2fa: boolean,
  username: string
}

export type Group = {
  active: boolean,
  id: string,
  name: string
}

export type EntityType = {
  id: string,
  label?: string,
  description?: string
}

export type Permission = string

export type ACE = {
  permissions: Array<Permission>,
  granting: boolean,
  securityId: Sid
}

export type EntityIdentity = {
  entityTypeId: string,
  entityId: string
}

export type ACL = {
  entityIdentity: EntityIdentity,
  owner: PrincipalSid,
  entries: Array<ACE>,
  parent?: ACL
}

export type Row = {
  acl: ACL,
  entityId: string,
  entityLabel: string
}

export type SidType = 'role' | 'user'

export type State = {
  me: PrincipalSid,
  roles: Role[],
  users: User[],
  groups: Group[],
  sidType: SidType,
  selectedSid: ?string,
  permissions: string[],
  selectedEntityTypeId: ?string,
  entityTypes: EntityType[],
  rows: Row[],
  filter: ?string,
  editRole: boolean,
  acl: ?ACL
}
