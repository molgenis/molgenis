package org.molgenis.security.core;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.collections.Maps.newHashMap;

import java.util.Map;
import org.mockito.Mock;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PermissionServiceImplTest extends AbstractMockitoTest {
  @Mock private MutableAclService mutableAclService;

  private PermissionService permissionService;

  @BeforeMethod
  public void beforeMethod() {
    permissionService = new PermissionServiceImpl(mutableAclService);
  }

  @Test
  public void testGrantPermissionSetForSingleObject() {
    Sid sid = mock(Sid.class);
    MutableAcl acl = mock(MutableAcl.class, RETURNS_DEEP_STUBS);
    when(acl.getEntries().size()).thenReturn(1);
    ObjectIdentity objectIdentity = mock(ObjectIdentity.class);
    when(mutableAclService.readAclById(objectIdentity)).thenReturn(acl);

    permissionService.grant(objectIdentity, PermissionSet.WRITE, sid);

    verify(acl, times(1)).insertAce(1, PermissionSet.WRITE, sid, true);
    verify(mutableAclService, times(1)).updateAcl(acl);
    verifyNoMoreInteractions(mutableAclService);
  }

  @Test
  public void testGrantPermissionSetsForMultipleObjects() {
    Sid sid = mock(Sid.class);

    MutableAcl acl1 = mock(MutableAcl.class, RETURNS_DEEP_STUBS);
    when(acl1.getEntries().size()).thenReturn(1);
    ObjectIdentity objectIdentity1 = mock(ObjectIdentity.class);

    MutableAcl acl2 = mock(MutableAcl.class, RETURNS_DEEP_STUBS);
    when(acl2.getEntries().size()).thenReturn(1);
    ObjectIdentity objectIdentity2 = mock(ObjectIdentity.class);

    when(mutableAclService.readAclById(any(ObjectIdentity.class)))
        .thenAnswer(
            invocation -> {
              Object argument = invocation.getArguments()[0];
              if (argument.equals(objectIdentity1)) {
                return acl1;
              } else if (argument.equals(objectIdentity2)) {
                return acl2;
              }
              throw new InvalidUseOfMatchersException("");
            });

    Map<ObjectIdentity, PermissionSet> permissionSetMap = newHashMap();
    permissionSetMap.put(objectIdentity1, PermissionSet.COUNT);
    permissionSetMap.put(objectIdentity2, PermissionSet.WRITEMETA);

    permissionService.grant(permissionSetMap, sid);

    verify(acl1, times(1)).insertAce(1, PermissionSet.COUNT, sid, true);
    verify(mutableAclService, times(1)).updateAcl(acl1);
    verify(acl2, times(1)).insertAce(1, PermissionSet.WRITEMETA, sid, true);
    verify(mutableAclService, times(1)).updateAcl(acl2);
    verifyNoMoreInteractions(mutableAclService);
  }

  @Test
  public void testExists() {
    ObjectIdentity objectIdentity = mock(ObjectIdentity.class);
    Sid sid = mock(Sid.class);
    MutableAcl acl = mock(MutableAcl.class);
    when(mutableAclService.readAclById(objectIdentity, singletonList(sid))).thenReturn(acl);
    AccessControlEntry ace = mock(AccessControlEntry.class);
    when(ace.getSid()).thenReturn(sid);
    when(acl.getEntries()).thenReturn(singletonList(ace));
    assertTrue(permissionService.exists(objectIdentity, sid));
  }

  @Test
  public void testExistsNotExists() {
    ObjectIdentity objectIdentity = mock(ObjectIdentity.class);
    Sid sid = mock(Sid.class);
    MutableAcl acl = mock(MutableAcl.class);
    when(mutableAclService.readAclById(objectIdentity, singletonList(sid))).thenReturn(acl);
    assertFalse(permissionService.exists(objectIdentity, sid));
  }
}
