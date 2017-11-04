package org.molgenis.data.security.model;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Imports all of the security administration metadata entities.
 */
@Configuration
@Import({ SecurityPackage.class, GroupFactory.class, GroupMetadata.class, GroupMembershipFactory.class,
		GroupMembershipMetadata.class, RoleMetadata.class, RoleFactory.class, TokenFactory.class, TokenMetaData.class,
		UserFactory.class, UserMetadata.class })
public class SecurityMetadataConfig
{
}
