package org.molgenis.data.security.auth;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.auth.RoleMembershipMetadata.ROLE_MEMBERSHIP;

import java.util.stream.Stream;
import org.molgenis.data.DataService;
import org.springframework.stereotype.Component;

@Component
public class RoleMembershipValidatorImpl implements RoleMembershipValidator {
  private final DataService dataService;

  RoleMembershipValidatorImpl(DataService dataService) {
    this.dataService = requireNonNull(dataService);
  }

  @Override
  public void validate(RoleMembership roleMembership) {
    Group group = roleMembership.getRole().getGroup();
    if (group != null) {
      getUserRoleMemberships(roleMembership.getUser())
          .filter(
              userRoleMembership -> isDifferentRoleMembership(userRoleMembership, roleMembership))
          .forEach(otherUserRoleMembership -> validateGroup(otherUserRoleMembership, group));
    }
  }

  private Stream<RoleMembership> getUserRoleMemberships(User user) {
    return dataService
        .query(ROLE_MEMBERSHIP, RoleMembership.class)
        .eq(RoleMembershipMetadata.USER, user)
        .findAll();
  }

  private boolean isDifferentRoleMembership(
      RoleMembership thisRoleMembership, RoleMembership thatRoleMembership) {
    return !thisRoleMembership.getId().equals(thatRoleMembership.getId());
  }

  private void validateGroup(RoleMembership otherUserRoleMembership, Group group) {
    Group userRoleMembershipGroup = otherUserRoleMembership.getRole().getGroup();
    if (userRoleMembershipGroup != null && group.getId().equals(userRoleMembershipGroup.getId())) {
      throw new RoleMembershipValidationException(otherUserRoleMembership);
    }
  }
}
