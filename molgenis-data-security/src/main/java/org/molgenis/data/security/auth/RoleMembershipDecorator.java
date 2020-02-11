package org.molgenis.data.security.auth;

import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.security.SessionSecurityContextUpdater;

public class RoleMembershipDecorator extends AbstractRepositoryDecorator<RoleMembership> {
  private static final int BATCH_SIZE = 1000;

  private final RoleMembershipValidator roleMembershipValidator;
  private final SessionSecurityContextUpdater sessionSecurityContextUpdater;

  RoleMembershipDecorator(
      Repository<RoleMembership> delegateRepository,
      RoleMembershipValidator roleMembershipValidator,
      SessionSecurityContextUpdater securityContextUpdater) {
    super(delegateRepository);
    this.roleMembershipValidator = requireNonNull(roleMembershipValidator);
    this.sessionSecurityContextUpdater = requireNonNull(securityContextUpdater);
  }

  @Override
  public void add(RoleMembership roleMembership) {
    preAdd(roleMembership);
    super.add(roleMembership);
  }

  @Override
  public Integer add(Stream<RoleMembership> roleMembershipStream) {
    return super.add(roleMembershipStream.filter(this::preAdd));
  }

  @Override
  public void update(RoleMembership roleMembership) {
    preUpdate(roleMembership);
    super.update(roleMembership);
  }

  @Override
  public void update(Stream<RoleMembership> roleMembershipStream) {
    super.update(roleMembershipStream.filter(this::preUpdate));
  }

  @Override
  public void delete(RoleMembership roleMembership) {
    preDelete(roleMembership);
    super.delete(roleMembership);
  }

  @Override
  public void deleteById(Object id) {
    preDeleteById(id);
    super.deleteById(id);
  }

  @Override
  public void deleteAll() {
    forEachBatched(roleMemberships -> roleMemberships.forEach(this::preDelete), BATCH_SIZE);
    super.deleteAll();
  }

  @Override
  public void delete(Stream<RoleMembership> roleMembershipStream) {
    super.delete(roleMembershipStream.filter(this::preDelete));
  }

  @Override
  public void deleteAll(Stream<Object> ids) {
    super.deleteAll(ids.filter(this::preDeleteById));
  }

  private boolean preAdd(RoleMembership roleMembership) {
    roleMembershipValidator.validate(roleMembership);
    sessionSecurityContextUpdater.resetAuthorities(roleMembership.getUser());
    return true;
  }

  private boolean preUpdate(RoleMembership roleMembership) {
    RoleMembership currentRoleMembership = findOneById(roleMembership.getId());
    sessionSecurityContextUpdater.resetAuthorities(currentRoleMembership.getUser());
    sessionSecurityContextUpdater.resetAuthorities(roleMembership.getUser());
    roleMembershipValidator.validate(roleMembership);
    return true;
  }

  private boolean preDeleteById(Object roleMembershipId) {
    RoleMembership roleMembership = findOneById(roleMembershipId);
    if (roleMembership == null) {
      throw new UnknownEntityException(getEntityType(), roleMembershipId);
    }
    return preDelete(roleMembership);
  }

  private boolean preDelete(RoleMembership roleMembership) {
    sessionSecurityContextUpdater.resetAuthorities(roleMembership.getUser());
    return true;
  }
}
