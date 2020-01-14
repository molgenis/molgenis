package org.molgenis.data.security.auth;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.security.DataserviceRoleHierarchy;
import org.molgenis.data.transaction.TransactionManager;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class CachedRoleHierarchyImplTest extends AbstractMockitoTest {
  @Mock private DataserviceRoleHierarchy dataserviceRoleHierarchy;
  @Mock private TransactionManager transactionManager;
  private CachedRoleHierarchyImpl cachedRoleHierarchyImpl;

  @BeforeEach
  void setUpBeforeEach() {
    cachedRoleHierarchyImpl =
        new CachedRoleHierarchyImpl(dataserviceRoleHierarchy, transactionManager);
  }

  @Test
  void testGetReachableGrantedAuthoritiesEmpty() {
    assertEquals(emptyList(), cachedRoleHierarchyImpl.getReachableGrantedAuthorities(emptyList()));
  }

  @Test
  void testGetReachableGrantedAuthoritiesWithoutCache() {
    cachedRoleHierarchyImpl.markRoleHierarchyCacheDirty();

    GrantedAuthority managerAuthority = new SimpleGrantedAuthority("ROLE_MANAGER");
    GrantedAuthority editorAuthority = new SimpleGrantedAuthority("ROLE_EDITOR");
    GrantedAuthority viewerAuthority = new SimpleGrantedAuthority("ROLE_VIEWER");

    doReturn(asList(managerAuthority, editorAuthority, viewerAuthority))
        .when(dataserviceRoleHierarchy)
        .getReachableGrantedAuthorities(singletonList(managerAuthority));

    assertEquals(
        asList(managerAuthority, editorAuthority, viewerAuthority),
        cachedRoleHierarchyImpl.getReachableGrantedAuthorities(singletonList(managerAuthority)));
  }

  @Test
  void testGetReachableGrantedAuthoritiesUsingCache() {
    TransactionSynchronizationManager.setCurrentTransactionReadOnly(true);

    GrantedAuthority managerAuthority = new SimpleGrantedAuthority("ROLE_MANAGER");
    GrantedAuthority editorAuthority = new SimpleGrantedAuthority("ROLE_EDITOR");
    GrantedAuthority viewerAuthority = new SimpleGrantedAuthority("ROLE_VIEWER");
    ImmutableMap<GrantedAuthority, ImmutableSet<GrantedAuthority>> authorityInclusions =
        ImmutableMap.<GrantedAuthority, ImmutableSet<GrantedAuthority>>builder()
            .put(managerAuthority, ImmutableSet.of(editorAuthority))
            .put(editorAuthority, ImmutableSet.of(viewerAuthority))
            .put(viewerAuthority, ImmutableSet.of())
            .build();
    when(dataserviceRoleHierarchy.getAllGrantedAuthorityInclusions())
        .thenReturn(authorityInclusions);
    assertEquals(
        ImmutableSet.of(managerAuthority, editorAuthority, viewerAuthority),
        cachedRoleHierarchyImpl.getReachableGrantedAuthorities(singletonList(managerAuthority)));
  }

  @Test
  void testGetReachableGrantedAuthoritiesUsingCacheMultiple() {
    TransactionSynchronizationManager.setCurrentTransactionReadOnly(true);

    GrantedAuthority managerAuthority = new SimpleGrantedAuthority("ROLE_MANAGER");
    GrantedAuthority editorAuthority = new SimpleGrantedAuthority("ROLE_EDITOR");
    GrantedAuthority viewerAuthority = new SimpleGrantedAuthority("ROLE_VIEWER");
    ImmutableMap<GrantedAuthority, ImmutableSet<GrantedAuthority>> authorityInclusions =
        ImmutableMap.<GrantedAuthority, ImmutableSet<GrantedAuthority>>builder()
            .put(managerAuthority, ImmutableSet.of(editorAuthority))
            .put(editorAuthority, ImmutableSet.of(viewerAuthority))
            .put(viewerAuthority, ImmutableSet.of())
            .build();
    when(dataserviceRoleHierarchy.getAllGrantedAuthorityInclusions())
        .thenReturn(authorityInclusions);
    assertEquals(
        ImmutableSet.of(managerAuthority, editorAuthority, viewerAuthority),
        cachedRoleHierarchyImpl.getReachableGrantedAuthorities(
            asList(managerAuthority, editorAuthority)));
  }
}
