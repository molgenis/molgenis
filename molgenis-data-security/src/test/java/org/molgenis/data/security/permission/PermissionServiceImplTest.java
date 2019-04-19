package org.molgenis.data.security.permission;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.permission.EntityHelper.PLUGIN;
import static org.molgenis.data.security.permission.EntityHelper.SYS_SEC_PLUGIN;
import static org.molgenis.security.core.PermissionSet.COUNT;
import static org.molgenis.security.core.PermissionSet.READ;
import static org.molgenis.security.core.PermissionSet.READMETA;
import static org.molgenis.security.core.PermissionSet.WRITE;
import static org.molgenis.security.core.PermissionSet.WRITEMETA;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Stream;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.security.EntityIdentity;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.permission.inheritance.PermissionInheritanceResolver;
import org.molgenis.security.acl.AclClassService;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.acl.ObjectIdentityService;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PermissionServiceImplTest extends AbstractMockitoTest {
  @Mock MutableAclService mutableAclService;
  @Mock AclClassService aclClassService;
  @Mock PermissionInheritanceResolver inheritanceResolver;
  @Mock ObjectIdentityService objectIdentityService;
  @Mock DataService dataService;
  @Mock MutableAclClassService mutableAclClassService;
  @Mock UserRoleTools userRoleTools;
  @Mock EntityHelper entityHelper;
  private PermissionServiceImpl permissionsApiService;

  @BeforeMethod
  public void setUpBeforeMethod() {
    permissionsApiService =
        new PermissionServiceImpl(
            mutableAclService,
            inheritanceResolver,
            objectIdentityService,
            dataService,
            mutableAclClassService,
            userRoleTools,
            entityHelper);
  }

  @Test
  public void testGetClasses() {
    resetMocks();
    when(aclClassService.getAclClassTypes())
        .thenReturn(Arrays.asList("entity-test1", "entity-test2"));
    assertEquals(permissionsApiService.getTypes(), Arrays.asList("entity-test1", "entity-test2"));
  }

  @Test
  public void testGetAcls() {
    resetMocks();

    when(objectIdentityService.getObjectIdentities("entity-type", 10, 0))
        .thenReturn(
            Arrays.asList(
                new ObjectIdentityImpl("classId", "test1"),
                new ObjectIdentityImpl("classId", "test2")));
    doReturn(true).when(dataService).hasEntityType("type");
    when(entityHelper.getEntityTypeIdFromType("entity-type")).thenReturn("type");
    assertEquals(
        permissionsApiService.getAcls("entity-type", 1, 10), Arrays.asList("test1", "test2"));
  }

  @Test
  public void testGetSuitablePermissionsForTypeEntityType() {
    when(dataService.hasEntityType(EntityTypeMetadata.ENTITY_TYPE_META_DATA)).thenReturn(true);
    when(entityHelper.getEntityTypeIdFromType(EntityTypeIdentity.ENTITY_TYPE))
        .thenReturn(EntityTypeMetadata.ENTITY_TYPE_META_DATA);
    assertEquals(
        permissionsApiService.getSuitablePermissionsForType(EntityTypeIdentity.ENTITY_TYPE),
        newHashSet(READMETA, COUNT, READ, WRITE, WRITEMETA));
  }

  @Test
  public void testGetSuitablePermissionsForTypePackage() {
    when(dataService.hasEntityType(PackageMetadata.PACKAGE)).thenReturn(true);
    when(entityHelper.getEntityTypeIdFromType(PackageIdentity.PACKAGE))
        .thenReturn(PackageMetadata.PACKAGE);
    assertEquals(
        permissionsApiService.getSuitablePermissionsForType(PackageIdentity.PACKAGE),
        newHashSet(READMETA, COUNT, READ, WRITE, WRITEMETA));
  }

  @Test
  public void testGetSuitablePermissionsForTypePlugin() {
    when(dataService.hasEntityType(PLUGIN)).thenReturn(true);
    when(entityHelper.getEntityTypeIdFromType(PLUGIN)).thenReturn(SYS_SEC_PLUGIN);
    assertEquals(permissionsApiService.getSuitablePermissionsForType(PLUGIN), newHashSet(READ));
  }

  @Test
  public void testGetSuitablePermissionsForTypeEntity() {
    when(dataService.hasEntityType("row_level_secured_entity")).thenReturn(true);
    when(entityHelper.getEntityTypeIdFromType("entity-row_level_secured_entity"))
        .thenReturn("row_level_secured_entity");
    assertEquals(
        permissionsApiService.getSuitablePermissionsForType("entity-row_level_secured_entity"),
        newHashSet(READ, WRITE));
  }

  @Test
  public void testGetPermission() {
    doReturn(true).when(dataService).hasEntityType("typeId");
    PrincipalSid sid1 = mock(PrincipalSid.class);
    when(sid1.getPrincipal()).thenReturn("user1");
    PrincipalSid sid2 = mock(PrincipalSid.class);
    when(sid2.getPrincipal()).thenReturn("user2");
    MutableAcl acl = mock(MutableAcl.class);
    ObjectIdentity objectIdentity = mock(ObjectIdentity.class);
    when(objectIdentity.getIdentifier()).thenReturn("identifier");
    when(acl.getObjectIdentity()).thenReturn(objectIdentity);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    when(ace1.getSid()).thenReturn(sid1);
    when(ace1.getPermission()).thenReturn(COUNT);
    AccessControlEntry ace2 = mock(AccessControlEntry.class);
    when(ace2.getSid()).thenReturn(sid2);
    when(ace2.getPermission()).thenReturn(WRITEMETA);
    when(acl.getEntries()).thenReturn(Arrays.asList(ace1, ace2));

    Map<ObjectIdentity, Acl> aclMap = new HashMap<>();
    aclMap.put(new ObjectIdentityImpl("entity-typeId", "identifier"), acl);

    when(mutableAclService.readAclsById(
            singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")),
            Arrays.asList(sid1, sid2)))
        .thenReturn(aclMap);

    Entity entity = mock(Entity.class);
    @SuppressWarnings("unchecked")
    Repository<Entity> repo = mock(Repository.class);
    when(dataService.findOneById("typeId", "identifier")).thenReturn(entity);

    // FIXME  ObjectPermissions permission1 =
    // FIXME     ObjectPermissions.create(objectIdentity, null, "user2", "WRITEMETA", null);
    // FIXME ObjectPermissions permission2 =
    // FIXME     ObjectPermissions.create(objectIdentity, null, "user1", "COUNT", null);
    // FIXME Set<ObjectPermissions> expected = newHashSet(permission1, permission2);
    when(entityHelper.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
    LinkedHashSet<Sid> sids = new LinkedHashSet<>();
    sids.add(sid1);
    sids.add(sid2);
    when(userRoleTools.sortSids(sids)).thenReturn(new LinkedList(sids));

    // FIXME assertEquals(
    // FIXME     newHashSet(
    // FIXME         permissionsApiService.getPermission(
    // FIXME             new ObjectIdentityImpl("entity-typeId", "identifier"), sids, true)),
    // FIXME     expected);
  }

  @Test
  public void testGetAllPermissions() {
    Entity entity = mock(Entity.class);
    EntityType entityType = mock(EntityType.class);

    when(aclClassService.getAclClassTypes()).thenReturn(singletonList("entity-typeId"));

    doReturn(true).when(dataService).hasEntityType("typeId");
    PrincipalSid sid1 = mock(PrincipalSid.class);
    when(sid1.getPrincipal()).thenReturn("user1");
    PrincipalSid sid2 = mock(PrincipalSid.class);
    when(sid2.getPrincipal()).thenReturn("user2");
    MutableAcl acl = mock(MutableAcl.class);
    ObjectIdentity objectIdentity = mock(ObjectIdentity.class);
    when(objectIdentity.getIdentifier()).thenReturn("identifier");
    when(acl.getObjectIdentity()).thenReturn(objectIdentity);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    when(ace1.getSid()).thenReturn(sid1);
    when(ace1.getPermission()).thenReturn(COUNT);
    AccessControlEntry ace2 = mock(AccessControlEntry.class);
    when(ace2.getSid()).thenReturn(sid2);
    when(ace2.getPermission()).thenReturn(WRITEMETA);
    when(acl.getEntries()).thenReturn(Arrays.asList(ace1, ace2));

    Map<ObjectIdentity, Acl> aclMap = new HashMap<>();
    aclMap.put(new ObjectIdentityImpl("entity-typeId", "identifier"), acl);

    LinkedHashSet<Sid> sids = new LinkedHashSet<>();
    sids.add(sid1);
    sids.add(sid2);
    when(userRoleTools.getInheritedSids(sids)).thenReturn(sids);
    when(userRoleTools.sortSids(sids)).thenReturn(new LinkedList(sids));
    when(mutableAclService.readAclsById(
            singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")),
            new ArrayList<>(sids)))
        .thenReturn(aclMap);

    // FIXME ObjectPermissions permission1 =
    // FIXME     ObjectPermissions.create(objectIdentity, null, "user1", "COUNT", null);
    // FIXME ObjectPermissions permission2 =
    // FIXME     ObjectPermissions.create(objectIdentity, null, "user2", "WRITEMETA", null);
    // FIXME List<ObjectPermissions> expected = Arrays.asList(permission1, permission2);
    when(objectIdentityService.getObjectIdentities("entity-typeId", newHashSet(sid1, sid2)))
        .thenReturn(singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")));

    when(entityHelper.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
    when(userRoleTools.getInheritedSids(sids)).thenReturn(sids);

    // FIXME assertEquals(newHashSet(permissionsApiService.getAllPermissions(sids, true)),
    // expected);
  }

  @Test
  public void testGetPagedPermissionsForType() {
    Entity entity = mock(Entity.class);

    doReturn(true).when(dataService).hasEntityType("typeId");
    PrincipalSid sid1 = mock(PrincipalSid.class);
    when(sid1.getPrincipal()).thenReturn("user1");
    PrincipalSid sid2 = mock(PrincipalSid.class);
    when(sid2.getPrincipal()).thenReturn("user2");
    MutableAcl acl = mock(MutableAcl.class);
    ObjectIdentity objectIdentity = mock(ObjectIdentity.class);
    when(objectIdentity.getIdentifier()).thenReturn("identifier");
    when(acl.getObjectIdentity()).thenReturn(objectIdentity);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    when(ace1.getSid()).thenReturn(sid1);
    when(ace1.getPermission()).thenReturn(COUNT);
    AccessControlEntry ace2 = mock(AccessControlEntry.class);
    when(ace2.getSid()).thenReturn(sid2);
    when(ace2.getPermission()).thenReturn(WRITEMETA);
    when(acl.getEntries()).thenReturn(Arrays.asList(ace1, ace2));

    Map<ObjectIdentity, Acl> aclMap = new HashMap<>();
    aclMap.put(new ObjectIdentityImpl("entity-typeId", "identifier"), acl);

    when(mutableAclService.readAclsById(
            singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")),
            Arrays.asList(sid1, sid2)))
        .thenReturn(aclMap);

    // FIXME  ObjectPermissions permission1 =
    // FIXME      ObjectPermissions.create(objectIdentity, null, "user1", "COUNT", null);
    // FIXME ObjectPermissions permission2 =
    // FIXME     ObjectPermissions.create(objectIdentity, null, "user2", "WRITEMETA", null);
    // FIXME List<ObjectPermissions> expected = Arrays.asList(permission1, permission2);
    when(objectIdentityService.getObjectIdentities("entity-typeId", newHashSet(sid1, sid2), 20, 60))
        .thenReturn(singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")));

    LinkedHashSet<Sid> sids = new LinkedHashSet<>();
    sids.add(sid1);
    sids.add(sid2);
    when(userRoleTools.sortSids(sids)).thenReturn(new LinkedList(sids));
    when(entityHelper.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
    // FIXME  assertEquals(
    // FIXME      permissionsApiService.getPagedPermissionsForType("entity-typeId", sids, 4, 20),
    // expected);
  }

  @Test
  public void testGetPermissionsForType() {
    doReturn(true).when(dataService).hasEntityType("typeId");
    PrincipalSid sid1 = new PrincipalSid("user1");
    PrincipalSid sid2 = new PrincipalSid("user2");
    MutableAcl acl = mock(MutableAcl.class);
    ObjectIdentity objectIdentity = mock(ObjectIdentity.class);
    when(objectIdentity.getIdentifier()).thenReturn("identifier");
    when(acl.getObjectIdentity()).thenReturn(objectIdentity);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    when(ace1.getSid()).thenReturn(sid1);
    when(ace1.getPermission()).thenReturn(COUNT);
    AccessControlEntry ace2 = mock(AccessControlEntry.class);
    when(ace2.getSid()).thenReturn(sid2);
    when(ace2.getPermission()).thenReturn(WRITEMETA);
    when(acl.getEntries()).thenReturn(Arrays.asList(ace1, ace2));

    Map<ObjectIdentity, Acl> aclMap = new HashMap<>();
    aclMap.put(new ObjectIdentityImpl("entity-typeId", "identifier"), acl);

    when(mutableAclService.readAclsById(
            singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")),
            Arrays.asList(sid1, sid2)))
        .thenReturn(aclMap);

    Entity entity = mock(Entity.class);

    // FIXME  ObjectPermissions permission1 =
    // FIXME      ObjectPermissions.create(objectIdentity, null, "user2", "WRITEMETA", null);
    // FIXME  ObjectPermissions permission2 =
    // FIXME      ObjectPermissions.create(objectIdentity, null, "user1", "COUNT", null);
    // FIXME  List<ObjectPermissions> expected = Arrays.asList(permission2, permission1);
    when(objectIdentityService.getObjectIdentities("entity-typeId", newHashSet(sid1, sid2)))
        .thenReturn(singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")));
    when(entityHelper.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
    LinkedHashSet<Sid> sids = new LinkedHashSet<>();
    sids.add(sid1);
    sids.add(sid2);
    when(userRoleTools.getInheritedSids(sids)).thenReturn(sids);
    when(userRoleTools.sortSids(sids)).thenReturn(new LinkedList(sids));

    // FIXME  assertEquals(
    // FIXME      permissionsApiService.getPermissionsForType("entity-typeId", sids, true),
    // expected);
  }

  @Test
  public void testGetPermissionsForTypeWithoutUserQuery() {
    doReturn(true).when(dataService).hasEntityType("typeId");
    PrincipalSid sid1 = new PrincipalSid("user1");
    PrincipalSid sid2 = new PrincipalSid("user2");
    MutableAcl acl = mock(MutableAcl.class);
    ObjectIdentity objectIdentity = mock(ObjectIdentity.class);
    when(objectIdentity.getIdentifier()).thenReturn("identifier");
    when(acl.getObjectIdentity()).thenReturn(objectIdentity);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    when(ace1.getSid()).thenReturn(sid1);
    when(ace1.getPermission()).thenReturn(COUNT);
    AccessControlEntry ace2 = mock(AccessControlEntry.class);
    when(ace2.getSid()).thenReturn(sid2);
    when(ace2.getPermission()).thenReturn(WRITEMETA);
    when(acl.getEntries()).thenReturn(Arrays.asList(ace1, ace2));

    Map<ObjectIdentity, Acl> aclMap = new HashMap<>();
    aclMap.put(new ObjectIdentityImpl("entity-typeId", "identifier"), acl);

    when(mutableAclService.readAclsById(
            singletonList(new ObjectIdentityImpl("entity-typeId", "identifier"))))
        .thenReturn(aclMap);

    Entity entity = mock(Entity.class);

    // FIXME  ObjectPermissions permission1 =
    // FIXME      ObjectPermissions.create(objectIdentity, null, "user2", "WRITEMETA", null);
    // FIXME  ObjectPermissions permission2 =
    // FIXME      ObjectPermissions.create(objectIdentity, null, "user1", "COUNT", null);
    // FIXME  List<ObjectPermissions> expected = Arrays.asList(permission2, permission1);
    when(objectIdentityService.getObjectIdentities("entity-typeId"))
        .thenReturn(singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")));
    when(userRoleTools.getAllAvailableSids()).thenReturn(newHashSet(sid1, sid2));
    when(entityHelper.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");

    // FIXME  assertEquals(
    // FIXME     permissionsApiService.getPermissionsForType("entity-typeId",
    // Collections.emptySet(), true),
    // FIXME     expected);
  }

  @Test
  public void testCreateAcl() {
    Entity entity = mock(Entity.class);
    when(dataService.findOneById("typeId", "identifier")).thenReturn(entity);
    when(dataService.hasEntityType("typeId")).thenReturn(true);
    when(entityHelper.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
    permissionsApiService.createAcl(new ObjectIdentityImpl("entity-typeId", "identifier"));
    verify(mutableAclService).createAcl(new ObjectIdentityImpl("entity-typeId", "identifier"));
  }

  @Test
  public void testCreatePermission() {
    Entity entity = mock(Entity.class);
    when(dataService.findOneById("typeId", "identifier")).thenReturn(entity);
    when(dataService.hasEntityType("typeId")).thenReturn(true);

    Sid sid = mock(Sid.class);
    MutableAcl acl = mock(MutableAcl.class);
    when(mutableAclService.readAclById(new ObjectIdentityImpl("entity-typeId", "identifier")))
        .thenReturn(acl);
    // PermissionRequest permission = PermissionRequest.create("role", null, "WRITE");
    when(dataService.findOneById("typeId", "identifier")).thenReturn(entity);
    when(dataService.hasEntityType("typeId")).thenReturn(true);
    Sid expectedSid = new GrantedAuthoritySid("ROLE_role");

    when(entityHelper.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
    // FIXME permissionsApiService.createPermission(
    // FIXME     singletonList(permission), new ObjectIdentityImpl("entity-typeId", "identifier"));

    verify(acl).insertAce(0, WRITE, expectedSid, true);
    verify(mutableAclService).updateAcl(acl);
  }

  @Test
  public void testCreatePermissions() {
    Entity entity = mock(Entity.class);
    doReturn(entity).when(dataService).findOneById("typeId", "identifier");
    doReturn(entity).when(dataService).findOneById("typeId", "identifier2");
    when(dataService.hasEntityType("typeId")).thenReturn(true);

    Sid sid = mock(Sid.class);
    MutableAcl acl = mock(MutableAcl.class);
    MutableAcl acl2 = mock(MutableAcl.class);

    doReturn(acl)
        .when(mutableAclService)
        .readAclById(new ObjectIdentityImpl("entity-typeId", "identifier"));
    doReturn(acl2)
        .when(mutableAclService)
        .readAclById(new ObjectIdentityImpl("entity-typeId", "identifier2"));
    /*    PermissionRequest permission1 = PermissionRequest.create("role", null, "WRITE");
    ObjectPermissionsRequest objectPermissionsRequest1 =
        ObjectPermissionsRequest.create("identifier", singletonList(permission1));
    PermissionRequest permission2 = PermissionRequest.create(null, "user1", "READ");
    ObjectPermissionsRequest objectPermissionsRequest2 =
        ObjectPermissionsRequest.create("identifier2", singletonList(permission2));*/

    Sid expectedSid = new GrantedAuthoritySid("ROLE_role");
    Sid expectedSid2 = new PrincipalSid("user1");
    when(entityHelper.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
    doReturn(new ObjectIdentityImpl("entity-typeId", "identifier"))
        .when(entityHelper)
        .getObjectIdentity("entity-typeId", "identifier");
    doReturn(new ObjectIdentityImpl("entity-typeId", "identifier2"))
        .when(entityHelper)
        .getObjectIdentity("entity-typeId", "identifier2");
    // FIXME  permissionsApiService.createPermissions(
    // FIXME     Arrays.asList(objectPermissionsRequest1, objectPermissionsRequest2),
    // "entity-typeId");

    verify(acl).insertAce(0, WRITE, expectedSid, true);
    verify(acl2).insertAce(0, READ, expectedSid2, true);
    verify(mutableAclService).updateAcl(acl);
  }

  @Test
  public void testSetPermission() {
    Entity entity = mock(Entity.class);
    doReturn(entity).when(dataService).findOneById("typeId", "identifier");
    Sid sid = new GrantedAuthoritySid("ROLE_role");
    MutableAcl acl = mock(MutableAcl.class);
    ObjectIdentity objectIdentity = mock(ObjectIdentity.class);
    when(acl.getObjectIdentity()).thenReturn(objectIdentity);
    doReturn(acl)
        .when(mutableAclService)
        .readAclById(new ObjectIdentityImpl("entity-typeId", "identifier"));

    // PermissionRequest permission = PermissionRequest.create("role", null, "WRITE");

    doReturn(true).when(dataService).hasEntityType("typeId");
    when(objectIdentity.getIdentifier()).thenReturn("identifier");
    when(acl.getObjectIdentity()).thenReturn(objectIdentity);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    when(ace1.getSid()).thenReturn(sid);
    when(ace1.getPermission()).thenReturn(COUNT);
    when(acl.getEntries()).thenReturn(singletonList(ace1));

    Map<ObjectIdentity, Acl> aclMap = new HashMap<>();
    aclMap.put(new ObjectIdentityImpl("entity-typeId", "identifier"), acl);

    when(mutableAclService.readAclsById(
            singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")),
            singletonList(sid)))
        .thenReturn(aclMap);

    Sid expectedSid = new GrantedAuthoritySid("ROLE_role");
    when(entityHelper.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
    when(userRoleTools.sortSids(singleton(sid))).thenReturn(new LinkedList(singletonList(sid)));

    // FIXME  permissionsApiService.updatePermission(
    // FIXME     singletonList(permission), new ObjectIdentityImpl("entity-typeId", "identifier"));

    verify(acl).deleteAce(0);
    verify(acl).insertAce(1, WRITE, expectedSid, true);
    verify(mutableAclService, times(2)).updateAcl(acl);
  }

  @Test
  public void testSetPermissions() {
    Entity entity = mock(Entity.class);
    doReturn(entity).when(dataService).findOneById("typeId", "identifier");
    Sid sid = new GrantedAuthoritySid("ROLE_role");
    MutableAcl acl = mock(MutableAcl.class);
    ObjectIdentity objectIdentity = new ObjectIdentityImpl("entity-typeId", "identifier");
    when(acl.getObjectIdentity()).thenReturn(objectIdentity);
    doReturn(acl).when(mutableAclService).readAclById(objectIdentity);

    doReturn(true).when(dataService).hasEntityType("typeId");

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    when(ace1.getSid()).thenReturn(sid);
    when(ace1.getPermission()).thenReturn(COUNT);
    when(acl.getEntries()).thenReturn(singletonList(ace1));

    Map<ObjectIdentity, Acl> aclMap = new HashMap<>();
    aclMap.put(new ObjectIdentityImpl("entity-typeId", "identifier"), acl);

    when(mutableAclService.readAclsById(singletonList(objectIdentity), singletonList(sid)))
        .thenReturn(aclMap);

    Sid expectedSid = new GrantedAuthoritySid("ROLE_role");
    when(entityHelper.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
    when(entityHelper.getObjectIdentity("entity-typeId", "identifier")).thenReturn(objectIdentity);
    when(userRoleTools.sortSids(singleton(sid))).thenReturn(new LinkedList(singletonList(sid)));
    // FIXME permissionsApiService.updatePermissions(singleton(identityPermissions),
    // "entity-typeId");

    verify(acl).deleteAce(0);
    verify(acl).insertAce(1, WRITE, expectedSid, true);
    verify(mutableAclService, times(2)).updateAcl(acl);
  }

  @Test
  public void testDeletePermission() {
    Entity entity = mock(Entity.class);
    when(dataService.findOneById("typeId", "identifier")).thenReturn(entity);
    when(dataService.hasEntityType("typeId")).thenReturn(true);
    when(entityHelper.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");

    Sid sid = mock(Sid.class);
    MutableAcl acl = mock(MutableAcl.class);
    AccessControlEntry ace = mock(AccessControlEntry.class);
    when(ace.getSid()).thenReturn(sid);
    when(acl.getEntries()).thenReturn(singletonList(ace));
    ObjectIdentity objectIdentity = new ObjectIdentityImpl("entity-typeId", "identifier");

    when(mutableAclService.readAclById(objectIdentity, singletonList(sid))).thenReturn(acl);

    permissionsApiService.deletePermission(sid, objectIdentity);
    verify(acl).deleteAce(0);
    verify(mutableAclService).updateAcl(acl);
  }

  @Test
  public void testAddType() {
    when(dataService.hasEntityType("typeId")).thenReturn(true);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("typeId");
    Attribute attribute = mock(Attribute.class);
    when(entityType.getIdAttribute()).thenReturn(attribute);
    when(attribute.getDataType()).thenReturn(AttributeType.STRING);
    when(dataService.getEntityType("typeId")).thenReturn(entityType);

    Entity entity1 = mock(Entity.class);
    when(entity1.getEntityType()).thenReturn(entityType);
    when(entity1.getIdValue()).thenReturn("1");
    Entity entity2 = mock(Entity.class);
    when(entity2.getIdValue()).thenReturn("2");
    when(entity2.getEntityType()).thenReturn(entityType);
    when(dataService.findAll("typeId")).thenReturn(Stream.of(entity1, entity2));
    when(entityHelper.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
    permissionsApiService.addType("entity-typeId");

    verify(mutableAclClassService).createAclClass("entity-typeId", String.class);
    verify(mutableAclService).createAcl(new EntityIdentity("typeId", "1"));
    verify(mutableAclService).createAcl(new EntityIdentity("typeId", "2"));
  }

  @Test
  public void testDeleteType() {
    when(dataService.hasEntityType("typeId")).thenReturn(true);
    when(entityHelper.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
    permissionsApiService.deleteType("entity-typeId");
    verify(mutableAclClassService).deleteAclClass("entity-typeId");
  }

  private void resetMocks() {
    reset(
        mutableAclService,
        aclClassService,
        inheritanceResolver,
        objectIdentityService,
        dataService);
  }
}
