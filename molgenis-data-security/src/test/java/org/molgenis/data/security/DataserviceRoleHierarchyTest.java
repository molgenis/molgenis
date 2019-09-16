package org.molgenis.data.security;

import static com.google.common.collect.ImmutableSet.of;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.auth.RoleMetadata.ROLE;

import com.google.common.collect.ImmutableList;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.security.auth.Role;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class DataserviceRoleHierarchyTest extends AbstractMockitoTest {
  private DataserviceRoleHierarchy molgenisRoleHierarchy;

  @Mock private DataService dataService;

  @Mock private Role dataExplorer;
  @Mock private Role viewer;
  @Mock private Role bbmriViewer;

  @Mock private Role su;
  @Mock private Role aclTakeOwnership;
  @Mock private Role aclModifyAuditing;
  @Mock private Role aclGeneralChanges;

  @Captor private ArgumentCaptor<Query<Role>> queryCaptor;

  @BeforeEach
  void setUpBeforeMethod() {
    molgenisRoleHierarchy = new DataserviceRoleHierarchy(dataService);

    when(dataService.findAll(eq(ROLE), queryCaptor.capture(), eq(Role.class)))
        .thenReturn(
            Stream.of(
                viewer,
                bbmriViewer,
                su,
                aclTakeOwnership,
                aclModifyAuditing,
                aclGeneralChanges,
                dataExplorer));
  }

  @Test
  void testGetReachableGrantedAuthoritiesSu() {
    when(su.getName()).thenReturn("SU");
    when(su.getIncludes())
        .thenReturn(ImmutableList.of(aclTakeOwnership, aclModifyAuditing, aclGeneralChanges));
    when(aclTakeOwnership.getName()).thenReturn("ACL_TAKE_OWNERSHIP");
    when(aclModifyAuditing.getName()).thenReturn("ACL_MODIFY_AUDITING");
    when(aclGeneralChanges.getName()).thenReturn("ACL_GENERAL_CHANGES");

    assertEquals(
        of(
            new SimpleGrantedAuthority("ROLE_ACL_TAKE_OWNERSHIP"),
            new SimpleGrantedAuthority("ROLE_ACL_MODIFY_AUDITING"),
            new SimpleGrantedAuthority("ROLE_ACL_GENERAL_CHANGES"),
            new SimpleGrantedAuthority("ROLE_SU")),
        molgenisRoleHierarchy.getReachableGrantedAuthorities(
            singletonList(new SimpleGrantedAuthority("ROLE_SU"))));
  }

  @Test
  void testGetReachableGrantedAuthoritiesDeep() {
    when(bbmriViewer.getName()).thenReturn("BBMRI_VIEWER");
    when(bbmriViewer.getIncludes()).thenReturn(ImmutableList.of(viewer));

    when(viewer.getName()).thenReturn("VIEWER");
    when(viewer.getIncludes()).thenReturn(ImmutableList.of(dataExplorer));

    when(dataExplorer.getName()).thenReturn("DATA_EXPLORER");

    assertEquals(
        of(
            new SimpleGrantedAuthority("ROLE_BBMRI_VIEWER"),
            new SimpleGrantedAuthority("ROLE_VIEWER"),
            new SimpleGrantedAuthority("ROLE_DATA_EXPLORER")),
        molgenisRoleHierarchy.getReachableGrantedAuthorities(
            singletonList(new SimpleGrantedAuthority("ROLE_BBMRI_VIEWER"))));
  }
}
