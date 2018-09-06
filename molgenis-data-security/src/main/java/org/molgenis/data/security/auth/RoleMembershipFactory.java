package org.molgenis.data.security.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class RoleMembershipFactory
    extends AbstractSystemEntityFactory<RoleMembership, RoleMembershipMetadata, String> {
  RoleMembershipFactory(
      RoleMembershipMetadata groupMembershipMetadata, EntityPopulator entityPopulator) {
    super(RoleMembership.class, groupMembershipMetadata, entityPopulator);
  }
}
