// @flow

export type GrantedAuthoritySid = {
  authority: string
}

export type PrincipalSid = {
  username: string
}

export type Sid = GrantedAuthoritySid | PrincipalSid

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

export type State = {
  me: PrincipalSid,
  sids: Array<GrantedAuthoritySid>,
  permissions: Array<string>,
  selectedSid: ?string,
  selectedEntityTypeId: ?string,
  entityTypes: Array<EntityType>,
  rows: Array<Row>,
  filter: ?string
}
