package org.molgenis.data.security.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.security.auth.GroupFactory;
import org.molgenis.data.security.auth.GroupMetadata;
import org.molgenis.data.security.auth.MembershipInvitationFactory;
import org.molgenis.data.security.auth.MembershipInvitationMetadata;
import org.molgenis.data.security.auth.RoleFactory;
import org.molgenis.data.security.auth.RoleMembershipFactory;
import org.molgenis.data.security.auth.RoleMembershipMetadata;
import org.molgenis.data.security.auth.RoleMetadata;
import org.molgenis.data.security.auth.SecurityPackage;
import org.molgenis.data.security.auth.TokenFactory;
import org.molgenis.data.security.auth.TokenMetadata;
import org.molgenis.data.security.auth.UserFactory;
import org.molgenis.data.security.auth.UserMetadata;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  EntityBaseTestConfig.class,
  GroupMetadata.class,
  GroupFactory.class,
  MembershipInvitationMetadata.class,
  MembershipInvitationFactory.class,
  RoleMetadata.class,
  RoleFactory.class,
  RoleMembershipMetadata.class,
  RoleMembershipFactory.class,
  TokenMetadata.class,
  TokenFactory.class,
  UserMetadata.class,
  UserFactory.class,
  SecurityPackage.class
})
public class SecurityTestConfig {}
