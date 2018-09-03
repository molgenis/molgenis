package org.molgenis.data.security;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.auth.RoleMetadata.ROLE;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.security.auth.Role;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataserviceRoleHierarchyTest extends AbstractMockitoTest {
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

  @BeforeMethod
  public void setUpBeforeMethod() {
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
  public void testGetReachableGrantedAuthoritiesSu() {
    when(su.getName()).thenReturn("SU");
    when(su.getIncludes())
        .thenReturn(ImmutableList.of(aclTakeOwnership, aclModifyAuditing, aclGeneralChanges));
    when(aclTakeOwnership.getName()).thenReturn("ACL_TAKE_OWNERSHIP");
    when(aclModifyAuditing.getName()).thenReturn("ACL_MODIFY_AUDITING");
    when(aclGeneralChanges.getName()).thenReturn("ACL_GENERAL_CHANGES");

    assertEquals(
        (Set)
            molgenisRoleHierarchy.getReachableGrantedAuthorities(
                singletonList(new SimpleGrantedAuthority("ROLE_SU"))),
        ImmutableSet.of(
            new SimpleGrantedAuthority("ROLE_ACL_TAKE_OWNERSHIP"),
            new SimpleGrantedAuthority("ROLE_ACL_MODIFY_AUDITING"),
            new SimpleGrantedAuthority("ROLE_ACL_GENERAL_CHANGES"),
            new SimpleGrantedAuthority("ROLE_SU")));
  }

  @Test
  public void testGetReachableGrantedAuthoritiesDeep() {
    when(bbmriViewer.getName()).thenReturn("BBMRI_VIEWER");
    when(bbmriViewer.getIncludes()).thenReturn(ImmutableList.of(viewer));

    when(viewer.getName()).thenReturn("VIEWER");
    when(viewer.getIncludes()).thenReturn(ImmutableList.of(dataExplorer));

    when(dataExplorer.getName()).thenReturn("DATA_EXPLORER");

    assertEquals(
        (Set)
            molgenisRoleHierarchy.getReachableGrantedAuthorities(
                singletonList(new SimpleGrantedAuthority("ROLE_BBMRI_VIEWER"))),
        ImmutableSet.of(
            new SimpleGrantedAuthority("ROLE_BBMRI_VIEWER"),
            new SimpleGrantedAuthority("ROLE_VIEWER"),
            new SimpleGrantedAuthority("ROLE_DATA_EXPLORER")));
  }
}
