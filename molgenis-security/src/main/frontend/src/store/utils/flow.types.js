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
  granted: boolean
}

export type ACL = {
  entityId: string,
  entityLabel: string,
  owner: PrincipalSid,
  aces: Array<ACE>
}

export type State = {
  me: PrincipalSid,
  sids: Array<GrantedAuthoritySid>,
  selectedSid: ?GrantedAuthoritySid,
  selectedEntityTypeId: ?string,
  entityTypes: Array<EntityType>,
  acls: Array<ACL>,
  filter: ?string
}
