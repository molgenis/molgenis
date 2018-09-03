package org.molgenis.bootstrap.populate;

import static org.mockito.Mockito.*;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.mockito.Mock;
import org.molgenis.security.core.PermissionService;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.util.Pair;
import org.springframework.context.ApplicationContext;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PermissionPopulatorTest extends AbstractMockitoTest {
  @Mock private PermissionService permissionService;

  private PermissionPopulator permissionPopulator;

  @BeforeMethod
  private void setUpBeforeMethod() {
    permissionPopulator = new PermissionPopulator(permissionService);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testPermissionPopulator() {
    new PermissionPopulator(null);
  }

  @Test
  public void testPopulate() {
    ApplicationContext applicationContext = mock(ApplicationContext.class);

    ObjectIdentity objectIdentity0 = new ObjectIdentityImpl("type", "id0");
    PermissionRegistry permissionRegistry0 = mock(PermissionRegistry.class);
    Multimap<ObjectIdentity, Pair<PermissionSet, Sid>> registry0Permissions =
        ArrayListMultimap.create();
    Sid sid0 = mock(Sid.class);
    registry0Permissions.put(objectIdentity0, new Pair<>(PermissionSet.COUNT, sid0));
    when(permissionRegistry0.getPermissions()).thenReturn(registry0Permissions);

    ObjectIdentity objectIdentity1 = new ObjectIdentityImpl("type", "id1");
    Multimap<ObjectIdentity, Pair<PermissionSet, Sid>> registry1Permissions =
        ArrayListMultimap.create();
    Sid sid1 = mock(Sid.class);
    registry1Permissions.put(objectIdentity1, new Pair<>(PermissionSet.READ, sid1));
    PermissionRegistry permissionRegistry1 = mock(PermissionRegistry.class);
    when(permissionRegistry1.getPermissions()).thenReturn(registry1Permissions);

    Map<String, PermissionRegistry> registryMap = new LinkedHashMap<>();
    registryMap.put("registry0", permissionRegistry0);
    registryMap.put("registry1", permissionRegistry1);
    when(applicationContext.getBeansOfType(PermissionRegistry.class)).thenReturn(registryMap);

    permissionPopulator.populate(applicationContext);

    verify(permissionService).grant(objectIdentity0, PermissionSet.COUNT, sid0);
    verify(permissionService).grant(objectIdentity1, PermissionSet.READ, sid1);
  }
}
