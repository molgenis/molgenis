package org.molgenis.data.security.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class VOGroupRoleMembershipFactory
    extends AbstractSystemEntityFactory<
        VOGroupRoleMembership, VOGroupRoleMembershipMetadata, String> {
  VOGroupRoleMembershipFactory(
      VOGroupRoleMembershipMetadata voGroupRoleMembershipMetadata,
      EntityPopulator entityPopulator) {
    super(VOGroupRoleMembership.class, voGroupRoleMembershipMetadata, entityPopulator);
  }
}
