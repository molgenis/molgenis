package org.molgenis.data.security.auth;

import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;

public class RoleMembershipDecorator extends AbstractRepositoryDecorator<RoleMembership> {

  private final RoleMembershipValidator roleMembershipValidator;

  RoleMembershipDecorator(
      Repository<RoleMembership> delegateRepository,
      RoleMembershipValidator roleMembershipValidator) {
    super(delegateRepository);
    this.roleMembershipValidator = requireNonNull(roleMembershipValidator);
  }

  @Override
  public void add(RoleMembership roleMembership) {
    validateRoleMembership(roleMembership);
    super.add(roleMembership);
  }

  @Override
  public Integer add(Stream<RoleMembership> roleMembershipStream) {
    return super.add(roleMembershipStream.filter(this::validateRoleMembership));
  }

  @Override
  public void update(RoleMembership roleMembership) {
    validateRoleMembership(roleMembership);
    super.update(roleMembership);
  }

  @Override
  public void update(Stream<RoleMembership> roleMembershipStream) {
    super.update(roleMembershipStream.filter(this::validateRoleMembership));
  }

  private boolean validateRoleMembership(RoleMembership roleMembership) {
    roleMembershipValidator.validate(roleMembership);
    return true;
  }
}
