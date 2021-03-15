package org.molgenis.data.security.permission;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.auth.VOGroupRoleMembershipMetadata.ROLE;
import static org.molgenis.data.security.auth.VOGroupRoleMembershipMetadata.VOGROUP_ROLE_MEMBERSHIP;
import static org.molgenis.data.security.auth.VOGroupRoleMembershipMetadata.VO_GROUP;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.VOGroup;
import org.molgenis.data.security.auth.VOGroupRoleMembership;
import org.molgenis.data.security.auth.VOGroupRoleMembershipFactory;
import org.molgenis.test.AbstractMockitoTest;

class VOGroupRoleMembershipServiceImplTest extends AbstractMockitoTest {

  @Mock private DataService dataService;

  @Mock(answer = RETURNS_SELF)
  Query<VOGroupRoleMembership> query;

  @Mock private VOGroup voGroup;
  @Mock private Role role;
  @Mock private VOGroupRoleMembership currentMembership;
  @Mock private VOGroupRoleMembership pastMembership;
  @Mock private VOGroupRoleMembershipFactory voGroupRoleMembershipFactory;

  private VOGroupRoleMembershipServiceImpl voGroupRoleMembershipService;

  @BeforeEach
  void setUp() {
    voGroupRoleMembershipService =
        new VOGroupRoleMembershipServiceImpl(dataService, voGroupRoleMembershipFactory);
  }

  @Test
  void getCurrentMemberships() {
    var voGroups = List.of(this.voGroup);

    when(dataService.query(VOGROUP_ROLE_MEMBERSHIP, VOGroupRoleMembership.class)).thenReturn(query);
    when(query.in(VO_GROUP, voGroups).findAll())
        .thenReturn(Stream.of(pastMembership, currentMembership));

    when(currentMembership.isCurrent()).thenReturn(true);

    assertEquals(
        List.of(currentMembership), voGroupRoleMembershipService.getCurrentMemberships(voGroups));
  }

  @Test
  void getMemberships() {
    when(dataService.query(VOGROUP_ROLE_MEMBERSHIP, VOGroupRoleMembership.class)).thenReturn(query);
    final var roles = List.of(this.role);
    when(query.in(ROLE, roles).fetch(any(Fetch.class)).findAll())
        .thenReturn(Stream.of(pastMembership, currentMembership));
    when(currentMembership.isCurrent()).thenReturn(true);

    assertEquals(List.of(currentMembership), voGroupRoleMembershipService.getMemberships(roles));
  }

  @Test
  void add() {
    when(voGroupRoleMembershipFactory.create()).thenReturn(currentMembership);

    voGroupRoleMembershipService.add(voGroup, role);

    verify(currentMembership).setRole(role);
    verify(currentMembership).setVOGroup(voGroup);
    verify(currentMembership).setFrom(any(Instant.class));
    verify(dataService).add(VOGROUP_ROLE_MEMBERSHIP, currentMembership);
  }

  @Test
  void remove() {
    when(dataService.findOneById(
            eq(VOGROUP_ROLE_MEMBERSHIP),
            eq("membershipId"),
            any(Fetch.class),
            eq(VOGroupRoleMembership.class)))
        .thenReturn(currentMembership);

    voGroupRoleMembershipService.removeMembership("membershipId");

    verify(currentMembership).setTo(any(Instant.class));
    verify(dataService).update(VOGROUP_ROLE_MEMBERSHIP, currentMembership);
  }

  @Test
  void removeUnknown() {
    when(dataService.findOneById(
            eq(VOGROUP_ROLE_MEMBERSHIP),
            eq("membershipId"),
            any(Fetch.class),
            eq(VOGroupRoleMembership.class)))
        .thenReturn(null);

    assertThrows(
        UnknownEntityException.class,
        () -> voGroupRoleMembershipService.removeMembership("membershipId"));
  }

  @Test
  void update() {
    when(dataService.findOneById(
            eq(VOGROUP_ROLE_MEMBERSHIP),
            eq("membershipId"),
            any(Fetch.class),
            eq(VOGroupRoleMembership.class)))
        .thenReturn(pastMembership);
    when(voGroupRoleMembershipFactory.create()).thenReturn(currentMembership);

    voGroupRoleMembershipService.updateMembership("membershipId", role);

    verify(pastMembership).setTo(any(Instant.class));
    verify(dataService).update(VOGROUP_ROLE_MEMBERSHIP, pastMembership);
    verify(currentMembership).setFrom(any(Instant.class));
    verify(currentMembership).setRole(role);
    verify(dataService).add(VOGROUP_ROLE_MEMBERSHIP, currentMembership);
  }

  @Test
  void updateUnknown() {
    when(dataService.findOneById(
            eq(VOGROUP_ROLE_MEMBERSHIP),
            eq("membershipId"),
            any(Fetch.class),
            eq(VOGroupRoleMembership.class)))
        .thenReturn(null);

    assertThrows(
        UnknownEntityException.class,
        () -> voGroupRoleMembershipService.updateMembership("membershipId", role));
  }
}
