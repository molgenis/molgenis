package org.molgenis.data.security.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class InvitationFactory extends AbstractSystemEntityFactory<Invitation, InvitationMetadata, String>
{
	InvitationFactory(InvitationMetadata invitationMetadata, EntityPopulator entityPopulator)
	{
		super(Invitation.class, invitationMetadata, entityPopulator);
	}
}
