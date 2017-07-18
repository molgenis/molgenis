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
  owner: PrincipalSid,
  aces: Array<ACE>
}

export type State = {
  me: PrincipalSid,
  sids: Array<GrantedAuthoritySid>,
  selectedSid: ?GrantedAuthoritySid,
  entityTypes: Array<EntityType>,
  entityTypeId: ?string,
  acls: Array<ACL>
}
