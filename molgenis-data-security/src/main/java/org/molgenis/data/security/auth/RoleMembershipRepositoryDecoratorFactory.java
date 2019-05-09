package org.molgenis.data.security.auth;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.Repository;
import org.springframework.stereotype.Component;

@Component
public class RoleMembershipRepositoryDecoratorFactory
    extends AbstractSystemRepositoryDecoratorFactory<RoleMembership, RoleMembershipMetadata> {

  private final RoleMembershipValidator roleMembershipValidator;

  public RoleMembershipRepositoryDecoratorFactory(
      RoleMembershipMetadata roleMembershipMetadata,
      RoleMembershipValidator roleMembershipValidator) {
    super(roleMembershipMetadata);
    this.roleMembershipValidator = requireNonNull(roleMembershipValidator);
  }

  @Override
  public Repository<RoleMembership> createDecoratedRepository(
      Repository<RoleMembership> repository) {
    return new RoleMembershipDecorator(repository, roleMembershipValidator);
  }
}
