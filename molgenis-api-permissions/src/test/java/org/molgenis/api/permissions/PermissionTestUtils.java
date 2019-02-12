package org.molgenis.api.permissions;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

public class PermissionTestUtils {

  public static Acl getSinglePermissionAcl(Sid sid, int mask, String name) {
    return getSinglePermissionAcl(sid, mask, name, null);
  }

  public static Acl getSinglePermissionAcl(Sid sid, int mask, String name, Acl parentAcl) {
    Acl acl = mock(Acl.class, name);
    AccessControlEntry ace = mock(AccessControlEntry.class);
    when(ace.getSid()).thenReturn(sid);
    Permission permission = mock(Permission.class);
    when(permission.getMask()).thenReturn(mask);
    when(ace.getPermission()).thenReturn(permission);
    when(acl.getEntries()).thenReturn(Collections.singletonList(ace));
    if (parentAcl != null) {
      when(acl.getParentAcl()).thenReturn(parentAcl);
    }
    return acl;
  }
}
