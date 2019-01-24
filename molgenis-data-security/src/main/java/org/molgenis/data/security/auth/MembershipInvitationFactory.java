package org.molgenis.data.security.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class MembershipInvitationFactory
    extends AbstractSystemEntityFactory<
        MembershipInvitation, MembershipInvitationMetadata, String> {
  MembershipInvitationFactory(
      MembershipInvitationMetadata invitationMetadata, EntityPopulator entityPopulator) {
    super(MembershipInvitation.class, invitationMetadata, entityPopulator);
  }
}
