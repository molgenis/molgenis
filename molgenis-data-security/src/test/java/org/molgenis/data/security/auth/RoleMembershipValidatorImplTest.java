package org.molgenis.data.security.auth;

import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RoleMembershipValidatorImplTest extends AbstractMockitoTest {
  @Mock private DataService dataService;
  private RoleMembershipValidatorImpl roleMembershipValidatorImpl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    roleMembershipValidatorImpl = new RoleMembershipValidatorImpl(dataService);
  }

  @Test
  public void testValidateRoleMembershipWithoutGroup() {
    RoleMembership roleMembership = mock(RoleMembership.class);
    Role roleWithoutGroup = mock(Role.class);
    when(roleMembership.getRole()).thenReturn(roleWithoutGroup);
    roleMembershipValidatorImpl.validate(roleMembership);
    verifyZeroInteractions(dataService);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testValidateRoleMembershipGroupNoExistingRoles() {
    RoleMembership roleMembership = mock(RoleMembership.class);
    User user = mock(User.class);
    when(roleMembership.getUser()).thenReturn(user);
    Role roleWithGroup = mock(Role.class);
    Group group = mock(Group.class);
    when(roleWithGroup.getGroup()).thenReturn(group);
    when(roleMembership.getRole()).thenReturn(roleWithGroup);
    when(roleMembership.getUser()).thenReturn(user);

    Query<RoleMembership> query = mock(Query.class, RETURNS_SELF);
    when(dataService.query(RoleMembershipMetadata.ROLE_MEMBERSHIP, RoleMembership.class))
        .thenReturn(query);
    when(query.eq(RoleMembershipMetadata.USER, user).findAll()).thenReturn(Stream.empty());
    roleMembershipValidatorImpl.validate(roleMembership);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testValidateRoleMembershipGroupSameExistingRole() {
    RoleMembership roleMembership =
        when(mock(RoleMembership.class).getId()).thenReturn("roleMembershipId").getMock();
    User user = mock(User.class);
    when(roleMembership.getUser()).thenReturn(user);
    Role roleWithGroup = mock(Role.class);
    Group group = mock(Group.class);
    when(roleWithGroup.getGroup()).thenReturn(group);
    when(roleMembership.getRole()).thenReturn(roleWithGroup);
    when(roleMembership.getUser()).thenReturn(user);

    Query<RoleMembership> query = mock(Query.class, RETURNS_SELF);
    when(dataService.query(RoleMembershipMetadata.ROLE_MEMBERSHIP, RoleMembership.class))
        .thenReturn(query);
    when(query.eq(RoleMembershipMetadata.USER, user).findAll())
        .thenReturn(Stream.of(roleMembership));
    roleMembershipValidatorImpl.validate(roleMembership);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = RoleMembershipValidationException.class)
  public void testValidateRoleMembershipGroupOtherExistingRole() {
    RoleMembership roleMembership =
        when(mock(RoleMembership.class).getId()).thenReturn("roleMembershipId").getMock();
    User user = mock(User.class);
    when(roleMembership.getUser()).thenReturn(user);
    Role roleWithGroup = mock(Role.class);
    Group group = when(mock(Group.class).getId()).thenReturn("groupId").getMock();
    when(roleWithGroup.getGroup()).thenReturn(group);
    when(roleMembership.getRole()).thenReturn(roleWithGroup);
    when(roleMembership.getUser()).thenReturn(user);

    RoleMembership otherRoleMembership =
        when(mock(RoleMembership.class).getId()).thenReturn("otherRoleMembershipId").getMock();
    Role otherRole = mock(Role.class);
    when(otherRole.getGroup()).thenReturn(group);
    when(otherRoleMembership.getRole()).thenReturn(otherRole);

    Query<RoleMembership> query = mock(Query.class, RETURNS_SELF);
    when(dataService.query(RoleMembershipMetadata.ROLE_MEMBERSHIP, RoleMembership.class))
        .thenReturn(query);
    when(query.eq(RoleMembershipMetadata.USER, user).findAll())
        .thenReturn(Stream.of(otherRoleMembership));
    roleMembershipValidatorImpl.validate(roleMembership);
  }
}
