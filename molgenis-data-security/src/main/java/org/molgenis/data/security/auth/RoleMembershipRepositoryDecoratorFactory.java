package org.molgenis.data.security.auth;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.Repository;
import org.molgenis.data.security.SessionSecurityContextUpdater;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Component
public class RoleMembershipRepositoryDecoratorFactory
    extends AbstractSystemRepositoryDecoratorFactory<RoleMembership, RoleMembershipMetadata> {

  private final RoleMembershipValidator roleMembershipValidator;
  private final SessionSecurityContextUpdater sessionSecurityContextUpdater;

  public RoleMembershipRepositoryDecoratorFactory(
      RoleMembershipMetadata roleMembershipMetadata,
      RoleMembershipValidator roleMembershipValidator,
      SessionSecurityContextUpdater sessionSecurityContextUpdater) {
    super(roleMembershipMetadata);
    this.roleMembershipValidator = requireNonNull(roleMembershipValidator);
    this.sessionSecurityContextUpdater = requireNonNull(sessionSecurityContextUpdater);
  }

  @Override
  public Repository<RoleMembership> createDecoratedRepository(
      Repository<RoleMembership> repository) {
    return new RoleMembershipDecorator(
        repository, roleMembershipValidator, sessionSecurityContextUpdater);
  }
}
