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
import static org.molgenis.security.core.PermissionSet.COUNT;
import static org.molgenis.security.core.PermissionSet.READ;
import static org.molgenis.security.core.PermissionSet.READMETA;
import static org.molgenis.security.core.PermissionSet.WRITE;
import static org.molgenis.security.core.PermissionSet.WRITEMETA;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityIdentity;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.permission.inheritance.PermissionInheritanceResolver;
import org.molgenis.data.security.permission.model.LabelledObject;
import org.molgenis.data.security.permission.model.LabelledObjectIdentity;
import org.molgenis.data.security.permission.model.LabelledPermission;
import org.molgenis.data.security.permission.model.LabelledType;
import org.molgenis.data.security.permission.model.Permission;
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
    when(mutableAclClassService.getAclClassTypes())
        .thenReturn(Arrays.asList("entity-test1", "entity-test2"));
    doReturn("label1").when(entityHelper).getLabel("entity-test1");
    doReturn("entityType1").when(entityHelper).getEntityTypeIdFromType("entity-test1");
    doReturn("label2").when(entityHelper).getLabel("entity-test2");
    doReturn("entityType2").when(entityHelper).getEntityTypeIdFromType("entity-test2");
    assertEquals(
        permissionsApiService.getTypes(),
        Arrays.asList(
            LabelledType.create("entity-test1", "entityType1", "label1"),
            LabelledType.create("entity-test2", "entityType2", "label2")));
  }

  @Test
  public void testGetAcls() {
    resetMocks();

    when(objectIdentityService.getObjectIdentities("entity-type", 10, 0))
        .thenReturn(
            Arrays.asList(
                new ObjectIdentityImpl("classId", "test1"),
                new ObjectIdentityImpl("classId", "test2")));
    doReturn("label1").when(entityHelper).getLabel("classId", "test1");
    doReturn("label2").when(entityHelper).getLabel("classId", "test2");
    assertEquals(
        permissionsApiService.getObjects("entity-type", 1, 10),
        Arrays.asList(
            LabelledObject.create("test2", "label2"), LabelledObject.create("test1", "label1")));
  }

  @Test
  public void testGetSuitablePermissionsForTypeEntityType() {
    assertEquals(
        permissionsApiService.getSuitablePermissionsForType(EntityTypeIdentity.ENTITY_TYPE),
        newHashSet(READMETA, COUNT, READ, WRITE, WRITEMETA));
  }

  @Test
  public void testGetSuitablePermissionsForTypePackage() {
    assertEquals(
        permissionsApiService.getSuitablePermissionsForType(PackageIdentity.PACKAGE),
        newHashSet(READMETA, COUNT, READ, WRITE, WRITEMETA));
  }

  @Test
  public void testGetSuitablePermissionsForTypePlugin() {
    assertEquals(permissionsApiService.getSuitablePermissionsForType(PLUGIN), newHashSet(READ));
  }

  @Test
  public void testGetSuitablePermissionsForTypeEntity() {
    assertEquals(
        permissionsApiService.getSuitablePermissionsForType("entity-row_level_secured_entity"),
        newHashSet(READ, WRITE));
  }

  @Test
  public void testGetPermission() {
    PrincipalSid sid1 = mock(PrincipalSid.class);
    PrincipalSid sid2 = mock(PrincipalSid.class);
    MutableAcl acl = mock(MutableAcl.class);
    ObjectIdentity objectIdentity = new ObjectIdentityImpl("entity-typeId", "identifier");
    when(acl.getObjectIdentity()).thenReturn(objectIdentity);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    when(ace1.getSid()).thenReturn(sid1);
    when(ace1.getPermission()).thenReturn(COUNT);
    AccessControlEntry ace2 = mock(AccessControlEntry.class);
    when(ace2.getSid()).thenReturn(sid2);
    when(ace2.getPermission()).thenReturn(WRITEMETA);
    when(acl.getEntries()).thenReturn(Arrays.asList(ace1, ace2));
    when(acl.getObjectIdentity()).thenReturn(objectIdentity);

    when(mutableAclService.readAclById(objectIdentity)).thenReturn(acl);

    Entity entity = mock(Entity.class);

    LabelledPermission permission1 =
        LabelledPermission.create(
            sid2,
            LabelledObjectIdentity.create(
                "entity-typeId", "typeLabel", "identifier", "identifierLabel"),
            WRITEMETA,
            null);
    LabelledPermission permission2 =
        LabelledPermission.create(
            sid1,
            LabelledObjectIdentity.create(
                "entity-typeId", "typeLabel", "identifier", "identifierLabel"),
            COUNT,
            null);
    Set<LabelledPermission> expected = newHashSet(permission1, permission2);
    when(entityHelper.getLabelledObjectIdentity(acl.getObjectIdentity()))
        .thenReturn(
            LabelledObjectIdentity.create(
                "entity-typeId", "typeLabel", "identifier", "identifierLabel"));

    LinkedHashSet<Sid> sids = new LinkedHashSet<>();
    sids.add(sid1);
    sids.add(sid2);

    assertEquals(
        newHashSet(
            permissionsApiService.getPermissionsForObject(
                new ObjectIdentityImpl("entity-typeId", "identifier"), sids, true)),
        expected);
  }

  @Test
  public void testGetAllPermissions() {
    when(mutableAclClassService.getAclClassTypes()).thenReturn(singletonList("entity-typeId"));

    doReturn(true).when(dataService).hasEntityType("typeId");
    PrincipalSid sid1 = mock(PrincipalSid.class);
    PrincipalSid sid2 = mock(PrincipalSid.class);
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

    LabelledPermission permission1 =
        LabelledPermission.create(
            sid1,
            LabelledObjectIdentity.create(
                "entity-typeId", "typeLabel", "identifier", "identifierLabel"),
            COUNT,
            null);
    LabelledPermission permission2 =
        LabelledPermission.create(
            sid2,
            LabelledObjectIdentity.create(
                "entity-typeId", "typeLabel", "identifier", "identifierLabel"),
            WRITEMETA,
            null);
    Set<LabelledPermission> expected = Sets.newHashSet(permission1, permission2);
    when(objectIdentityService.getObjectIdentities("entity-typeId", newHashSet(sid1, sid2)))
        .thenReturn(singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")));

    when(entityHelper.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
    when(entityHelper.getLabel("entity-typeId")).thenReturn("label");
    when(entityHelper.getLabelledObjectIdentity(acl.getObjectIdentity()))
        .thenReturn(
            LabelledObjectIdentity.create(
                "entity-typeId", "typeLabel", "identifier", "identifierLabel"));
    when(userRoleTools.getInheritedSids(sids)).thenReturn(sids);

    assertEquals(newHashSet(permissionsApiService.getPermissions(sids, true)), expected);
  }

  @Test
  public void testGetPagedPermissionsForType() {
    PrincipalSid sid1 = mock(PrincipalSid.class);
    PrincipalSid sid2 = mock(PrincipalSid.class);
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

    when(objectIdentityService.getObjectIdentities("entity-typeId", newHashSet(sid1, sid2), 20, 60))
        .thenReturn(singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")));
    LinkedHashSet<Sid> sids = new LinkedHashSet<>();
    sids.add(sid1);
    sids.add(sid2);
    when(userRoleTools.sortSids(sids)).thenReturn(new LinkedList(sids));
    when(entityHelper.getLabelledObjectIdentity(acl.getObjectIdentity()))
        .thenReturn(
            LabelledObjectIdentity.create(
                "entity-typeId", "typeLabel", "identifier", "identifierLabel"));

    LabelledPermission permission1 =
        LabelledPermission.create(
            sid1,
            LabelledObjectIdentity.create(
                "entity-typeId", "typeLabel", "identifier", "identifierLabel"),
            COUNT,
            null);
    LabelledPermission permission2 =
        LabelledPermission.create(
            sid2,
            LabelledObjectIdentity.create(
                "entity-typeId", "typeLabel", "identifier", "identifierLabel"),
            WRITEMETA,
            null);
    Map<String, HashSet<LabelledPermission>> expected =
        Collections.singletonMap("identifier", newHashSet(permission1, permission2));
    assertEquals(
        permissionsApiService.getPermissionsForType("entity-typeId", sids, 4, 20), expected);
  }

  @Test
  public void testGetPermissionsForType() {
    PrincipalSid sid1 = mock(PrincipalSid.class);
    PrincipalSid sid2 = mock(PrincipalSid.class);
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

    when(objectIdentityService.getObjectIdentities("entity-typeId", newHashSet(sid1, sid2)))
        .thenReturn(singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")));
    LinkedHashSet<Sid> sids = new LinkedHashSet<>();
    sids.add(sid1);
    sids.add(sid2);
    when(userRoleTools.sortSids(sids)).thenReturn(new LinkedList(sids));
    when(entityHelper.getLabelledObjectIdentity(acl.getObjectIdentity()))
        .thenReturn(
            LabelledObjectIdentity.create(
                "entity-typeId", "typeLabel", "identifier", "identifierLabel"));

    LabelledPermission permission1 =
        LabelledPermission.create(
            sid1,
            LabelledObjectIdentity.create(
                "entity-typeId", "typeLabel", "identifier", "identifierLabel"),
            COUNT,
            null);
    LabelledPermission permission2 =
        LabelledPermission.create(
            sid2,
            LabelledObjectIdentity.create(
                "entity-typeId", "typeLabel", "identifier", "identifierLabel"),
            WRITEMETA,
            null);
    Map<String, HashSet<LabelledPermission>> expected =
        Collections.singletonMap("identifier", newHashSet(permission1, permission2));
    assertEquals(
        permissionsApiService.getPermissionsForType("entity-typeId", sids, false), expected);
  }

  @Test
  public void testGetPermissionsForTypeWithoutUserQuery() {
    PrincipalSid sid1 = mock(PrincipalSid.class);
    PrincipalSid sid2 = mock(PrincipalSid.class);
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

    when(objectIdentityService.getObjectIdentities("entity-typeId"))
        .thenReturn(singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")));

    when(entityHelper.getLabelledObjectIdentity(acl.getObjectIdentity()))
        .thenReturn(
            LabelledObjectIdentity.create(
                "entity-typeId", "typeLabel", "identifier", "identifierLabel"));
    when(userRoleTools.getAllAvailableSids()).thenReturn(Sets.newHashSet(sid1, sid2));

    LabelledPermission permission1 =
        LabelledPermission.create(
            sid1,
            LabelledObjectIdentity.create(
                "entity-typeId", "typeLabel", "identifier", "identifierLabel"),
            COUNT,
            null);
    LabelledPermission permission2 =
        LabelledPermission.create(
            sid2,
            LabelledObjectIdentity.create(
                "entity-typeId", "typeLabel", "identifier", "identifierLabel"),
            WRITEMETA,
            null);
    Map<String, HashSet<LabelledPermission>> expected =
        Collections.singletonMap("identifier", newHashSet(permission1, permission2));
    assertEquals(
        permissionsApiService.getPermissionsForType("entity-typeId", Collections.emptySet(), false),
        expected);
  }

  @Test
  public void testCreateAcl() {
    permissionsApiService.createAcl(new ObjectIdentityImpl("entity-typeId", "identifier"));
    verify(mutableAclService).createAcl(new ObjectIdentityImpl("entity-typeId", "identifier"));
  }

  @Test
  public void testCreatePermission() {
    MutableAcl acl = mock(MutableAcl.class);
    when(mutableAclService.readAclById(new ObjectIdentityImpl("entity-typeId", "identifier")))
        .thenReturn(acl);

    Sid role = new GrantedAuthoritySid("ROLE_role");

    permissionsApiService.createPermission(
        Permission.create(new ObjectIdentityImpl("entity-typeId", "identifier"), role, WRITE));

    verify(acl).insertAce(0, WRITE, role, true);
    verify(mutableAclService).updateAcl(acl);
  }

  @Test
  public void testCreatePermissions() {
    MutableAcl acl = mock(MutableAcl.class);
    MutableAcl acl2 = mock(MutableAcl.class);

    doReturn(acl)
        .when(mutableAclService)
        .readAclById(new ObjectIdentityImpl("entity-typeId", "identifier"));
    doReturn(acl2)
        .when(mutableAclService)
        .readAclById(new ObjectIdentityImpl("entity-typeId", "identifier2"));
    Permission permission1 =
        Permission.create(
            new ObjectIdentityImpl("entity-typeId", "identifier"),
            new GrantedAuthoritySid("ROLE_role"),
            WRITE);
    Permission permission2 =
        Permission.create(
            new ObjectIdentityImpl("entity-typeId", "identifier2"),
            new PrincipalSid("user1"),
            READ);

    Sid expectedSid = new GrantedAuthoritySid("ROLE_role");
    Sid expectedSid2 = new PrincipalSid("user1");
    permissionsApiService.createPermissions(Sets.newHashSet(permission1, permission2));

    verify(acl).insertAce(0, WRITE, expectedSid, true);
    verify(acl2).insertAce(0, READ, expectedSid2, true);
    verify(mutableAclService).updateAcl(acl);
  }

  @Test
  public void testSetPermission() {
    Sid role = new GrantedAuthoritySid("ROLE_role");
    Entity entity = mock(Entity.class);
    MutableAcl acl = mock(MutableAcl.class);
    ObjectIdentity objectIdentity = mock(ObjectIdentity.class);
    when(acl.getObjectIdentity()).thenReturn(objectIdentity);
    doReturn(acl)
        .when(mutableAclService)
        .readAclById(new ObjectIdentityImpl("entity-typeId", "identifier"));

    when(objectIdentity.getIdentifier()).thenReturn("identifier");
    when(acl.getObjectIdentity()).thenReturn(objectIdentity);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    when(ace1.getSid()).thenReturn(role);
    when(ace1.getPermission()).thenReturn(COUNT);
    when(acl.getEntries()).thenReturn(singletonList(ace1));

    when(entityHelper.getLabelledObjectIdentity(acl.getObjectIdentity()))
        .thenReturn(
            LabelledObjectIdentity.create(
                "entity-typeId", "typeLabel", "identifier", "identifierLabel"));

    permissionsApiService.updatePermission(
        Permission.create(new ObjectIdentityImpl("entity-typeId", "identifier"), role, WRITE));

    verify(acl).deleteAce(0);
    verify(acl).insertAce(1, WRITE, role, true);
    verify(mutableAclService, times(2)).updateAcl(acl);
  }

  @Test
  public void testSetPermissions() {
    Entity entity = mock(Entity.class);
    Sid sid = new GrantedAuthoritySid("ROLE_role");
    MutableAcl acl = mock(MutableAcl.class);
    ObjectIdentity objectIdentity = new ObjectIdentityImpl("entity-typeId", "identifier");
    when(acl.getObjectIdentity()).thenReturn(objectIdentity);
    doReturn(acl).when(mutableAclService).readAclById(objectIdentity);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    when(ace1.getSid()).thenReturn(sid);
    when(ace1.getPermission()).thenReturn(COUNT);
    when(acl.getEntries()).thenReturn(singletonList(ace1));

    when(entityHelper.getLabelledObjectIdentity(acl.getObjectIdentity()))
        .thenReturn(
            LabelledObjectIdentity.create(
                "entity-typeId", "typeLabel", "identifier", "identifierLabel"));

    permissionsApiService.updatePermissions(
        singleton(Permission.create(objectIdentity, sid, WRITE)));

    verify(acl).deleteAce(0);
    verify(acl).insertAce(1, WRITE, sid, true);
    verify(mutableAclService, times(2)).updateAcl(acl);
  }

  @Test
  public void testDeletePermission() {
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
    permissionsApiService.deleteType("entity-typeId");
    verify(mutableAclClassService).deleteAclClass("entity-typeId");
  }

  private void resetMocks() {
    reset(mutableAclService, inheritanceResolver, objectIdentityService, dataService);
  }
}
