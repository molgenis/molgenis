package org.molgenis.api.permissions;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.api.permissions.PermissionSetUtils.COUNT;
import static org.molgenis.api.permissions.PermissionSetUtils.READ;
import static org.molgenis.api.permissions.PermissionSetUtils.READMETA;
import static org.molgenis.api.permissions.PermissionSetUtils.WRITE;
import static org.molgenis.api.permissions.PermissionSetUtils.WRITEMETA;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.mockito.Mock;
import org.molgenis.api.permissions.inheritance.PermissionInheritanceResolver;
import org.molgenis.api.permissions.model.request.ObjectPermissionsRequest;
import org.molgenis.api.permissions.model.request.PermissionRequest;
import org.molgenis.api.permissions.model.response.ObjectPermission;
import org.molgenis.api.permissions.model.response.ObjectPermissionsResponse;
import org.molgenis.api.permissions.model.response.TypePermissionsResponse;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginMetadata;
import org.molgenis.data.security.EntityIdentity;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.security.acl.AclClassService;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.acl.ObjectIdentityService;
import org.molgenis.security.core.PermissionSet;
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
import org.springframework.security.core.context.SecurityContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PermissionsServiceImplTest extends AbstractMockitoTest {
  @Mock MutableAclService mutableAclService;
  @Mock AclClassService aclClassService;
  @Mock PermissionInheritanceResolver inheritanceResolver;
  @Mock ObjectIdentityService objectIdentityService;
  @Mock DataService dataService;
  @Mock MutableAclClassService mutableAclClassService;
  @Mock UserRoleTools userRoleTools;
  @Mock IdentityTools identityTools;
  private PermissionsServiceImpl permissionsApiService;
  private SecurityContext previousContext;

  @BeforeMethod
  public void setUpBeforeMethod() {
    permissionsApiService =
        new PermissionsServiceImpl(
            mutableAclService,
            aclClassService,
            inheritanceResolver,
            objectIdentityService,
            dataService,
            mutableAclClassService,
            userRoleTools,
            identityTools);
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
    when(identityTools.getEntityTypeIdFromType("entity-type")).thenReturn("type");
    assertEquals(
        permissionsApiService.getAcls("entity-type", 1, 10), Arrays.asList("test1", "test2"));
  }

  @Test
  public void testGetSuitablePermissionsForTypeEntityType() {
    when(dataService.hasEntityType(EntityTypeMetadata.ENTITY_TYPE_META_DATA)).thenReturn(true);
    when(identityTools.getEntityTypeIdFromType(EntityTypeIdentity.ENTITY_TYPE))
        .thenReturn(EntityTypeMetadata.ENTITY_TYPE_META_DATA);
    assertEquals(
        permissionsApiService.getSuitablePermissionsForType(EntityTypeIdentity.ENTITY_TYPE),
        newHashSet(READMETA, COUNT, READ, WRITE, WRITEMETA));
  }

  @Test
  public void testGetSuitablePermissionsForTypePackage() {
    when(dataService.hasEntityType(PackageMetadata.PACKAGE)).thenReturn(true);
    when(identityTools.getEntityTypeIdFromType(PackageIdentity.PACKAGE))
        .thenReturn(PackageMetadata.PACKAGE);
    assertEquals(
        permissionsApiService.getSuitablePermissionsForType(PackageIdentity.PACKAGE),
        newHashSet(READMETA, COUNT, READ, WRITE, WRITEMETA));
  }

  @Test
  public void testGetSuitablePermissionsForTypePlugin() {
    when(dataService.hasEntityType(PluginMetadata.PLUGIN)).thenReturn(true);
    when(identityTools.getEntityTypeIdFromType(PluginIdentity.PLUGIN))
        .thenReturn(PluginMetadata.PLUGIN);
    assertEquals(
        permissionsApiService.getSuitablePermissionsForType(PluginIdentity.PLUGIN),
        newHashSet(READ));
  }

  @Test
  public void testGetSuitablePermissionsForTypeEntity() {
    when(dataService.hasEntityType("row_level_secured_entity")).thenReturn(true);
    when(identityTools.getEntityTypeIdFromType("entity-row_level_secured_entity"))
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
    when(objectIdentity.getType()).thenReturn("entity-typeId");
    when(objectIdentity.getIdentifier()).thenReturn("identifier");
    when(acl.getObjectIdentity()).thenReturn(objectIdentity);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    when(ace1.getSid()).thenReturn(sid1);
    when(ace1.getPermission()).thenReturn(PermissionSet.COUNT);
    AccessControlEntry ace2 = mock(AccessControlEntry.class);
    when(ace2.getSid()).thenReturn(sid2);
    when(ace2.getPermission()).thenReturn(PermissionSet.WRITEMETA);
    when(acl.getEntries()).thenReturn(Arrays.asList(ace1, ace2));

    Map<ObjectIdentity, Acl> aclMap = new HashMap<>();
    aclMap.put(new ObjectIdentityImpl("entity-typeId", "identifier"), acl);

    when(mutableAclService.readAclsById(
            singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")),
            Arrays.asList(sid1, sid2)))
        .thenReturn(aclMap);

    EntityType entityType = mock(EntityType.class);
    Attribute attribute = mock(Attribute.class);

    Entity entity = mock(Entity.class);
    when(entity.getLabelValue()).thenReturn("label");
    @SuppressWarnings("unchecked")
    Repository<Entity> repo = mock(Repository.class);
    when(repo.findOneById("identifier")).thenReturn(entity);
    when(dataService.getRepository("typeId")).thenReturn(repo);
    when(dataService.findOneById("typeId", "identifier")).thenReturn(entity);

    ObjectPermission permission1 = ObjectPermission.create(null, "user2", "WRITEMETA", null);
    ObjectPermission permission2 = ObjectPermission.create(null, "user1", "COUNT", null);
    Set<ObjectPermission> expected = newHashSet(permission1, permission2);
    when(identityTools.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
    LinkedHashSet<Sid> sids = new LinkedHashSet<>();
    sids.add(sid1);
    sids.add(sid2);
    when(userRoleTools.sortSids(sids)).thenReturn(new LinkedList(sids));

    assertEquals(
        newHashSet(
            permissionsApiService.getPermission(
                new ObjectIdentityImpl("entity-typeId", "identifier"), sids, true)),
        expected);
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
    when(objectIdentity.getType()).thenReturn("entity-typeId");
    when(objectIdentity.getIdentifier()).thenReturn("identifier");
    when(acl.getObjectIdentity()).thenReturn(objectIdentity);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    when(ace1.getSid()).thenReturn(sid1);
    when(ace1.getPermission()).thenReturn(PermissionSet.COUNT);
    AccessControlEntry ace2 = mock(AccessControlEntry.class);
    when(ace2.getSid()).thenReturn(sid2);
    when(ace2.getPermission()).thenReturn(PermissionSet.WRITEMETA);
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

    when(entityType.getLabel()).thenReturn("TypeLabel");
    when(entity.getLabelValue()).thenReturn("label");
    @SuppressWarnings("unchecked")
    Repository<Entity> repo = mock(Repository.class);
    when(repo.findOneById("identifier")).thenReturn(entity);
    when(repo.getEntityType()).thenReturn(entityType);
    when(dataService.getRepository("typeId")).thenReturn(repo);

    ObjectPermission permission1 = ObjectPermission.create(null, "user1", "COUNT", null);
    ObjectPermission permission2 = ObjectPermission.create(null, "user2", "WRITEMETA", null);
    ObjectPermissionsResponse identityPermissions1 =
        ObjectPermissionsResponse.create(
            "identifier", "label", Arrays.asList(permission1, permission2));
    TypePermissionsResponse classPermissions1 =
        TypePermissionsResponse.create(
            "entity-typeId", "TypeLabel", singletonList(identityPermissions1));
    Set<TypePermissionsResponse> expected = newHashSet(classPermissions1);
    when(objectIdentityService.getObjectIdentities("entity-typeId", newHashSet(sid1, sid2)))
        .thenReturn(singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")));

    when(identityTools.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
    when(userRoleTools.getInheritedSids(sids)).thenReturn(sids);

    assertEquals(newHashSet(permissionsApiService.getAllPermissions(sids, true)), expected);
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
    when(objectIdentity.getType()).thenReturn("entity-typeId");
    when(objectIdentity.getIdentifier()).thenReturn("identifier");
    when(acl.getObjectIdentity()).thenReturn(objectIdentity);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    when(ace1.getSid()).thenReturn(sid1);
    when(ace1.getPermission()).thenReturn(PermissionSet.COUNT);
    AccessControlEntry ace2 = mock(AccessControlEntry.class);
    when(ace2.getSid()).thenReturn(sid2);
    when(ace2.getPermission()).thenReturn(PermissionSet.WRITEMETA);
    when(acl.getEntries()).thenReturn(Arrays.asList(ace1, ace2));

    Map<ObjectIdentity, Acl> aclMap = new HashMap<>();
    aclMap.put(new ObjectIdentityImpl("entity-typeId", "identifier"), acl);

    when(mutableAclService.readAclsById(
            singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")),
            Arrays.asList(sid1, sid2)))
        .thenReturn(aclMap);

    when(entity.getLabelValue()).thenReturn("label");
    @SuppressWarnings("unchecked")
    Repository<Entity> repo = mock(Repository.class);
    when(repo.findOneById("identifier")).thenReturn(entity);
    when(dataService.getRepository("typeId")).thenReturn(repo);

    ObjectPermission permission1 = ObjectPermission.create(null, "user1", "COUNT", null);
    ObjectPermission permission2 = ObjectPermission.create(null, "user2", "WRITEMETA", null);
    ObjectPermissionsResponse identityPermissions1 =
        ObjectPermissionsResponse.create(
            "identifier", "label", Arrays.asList(permission1, permission2));
    Set<ObjectPermissionsResponse> expected = newHashSet(identityPermissions1);
    when(objectIdentityService.getObjectIdentities("entity-typeId", newHashSet(sid1, sid2), 20, 60))
        .thenReturn(singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")));

    LinkedHashSet<Sid> sids = new LinkedHashSet<>();
    sids.add(sid1);
    sids.add(sid2);
    when(userRoleTools.sortSids(sids)).thenReturn(new LinkedList(sids));
    when(identityTools.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
    assertEquals(
        permissionsApiService.getPagedPermissionsForType("entity-typeId", sids, 4, 20), expected);
  }

  @Test
  public void testGetPermissionsForType() {
    doReturn(true).when(dataService).hasEntityType("typeId");
    PrincipalSid sid1 = new PrincipalSid("user1");
    PrincipalSid sid2 = new PrincipalSid("user2");
    MutableAcl acl = mock(MutableAcl.class);
    ObjectIdentity objectIdentity = mock(ObjectIdentity.class);
    when(objectIdentity.getType()).thenReturn("entity-typeId");
    when(objectIdentity.getIdentifier()).thenReturn("identifier");
    when(acl.getObjectIdentity()).thenReturn(objectIdentity);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    when(ace1.getSid()).thenReturn(sid1);
    when(ace1.getPermission()).thenReturn(PermissionSet.COUNT);
    AccessControlEntry ace2 = mock(AccessControlEntry.class);
    when(ace2.getSid()).thenReturn(sid2);
    when(ace2.getPermission()).thenReturn(PermissionSet.WRITEMETA);
    when(acl.getEntries()).thenReturn(Arrays.asList(ace1, ace2));

    Map<ObjectIdentity, Acl> aclMap = new HashMap<>();
    aclMap.put(new ObjectIdentityImpl("entity-typeId", "identifier"), acl);

    when(mutableAclService.readAclsById(
            singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")),
            Arrays.asList(sid1, sid2)))
        .thenReturn(aclMap);

    Entity entity = mock(Entity.class);
    when(entity.getLabelValue()).thenReturn("label");
    @SuppressWarnings("unchecked")
    Repository<Entity> repo = mock(Repository.class);
    when(repo.findOneById("identifier")).thenReturn(entity);
    when(dataService.getRepository("typeId")).thenReturn(repo);

    ObjectPermission permission1 = ObjectPermission.create(null, "user2", "WRITEMETA", null);
    ObjectPermission permission2 = ObjectPermission.create(null, "user1", "COUNT", null);
    ObjectPermissionsResponse identityPermissions1 =
        ObjectPermissionsResponse.create(
            "identifier", "label", Arrays.asList(permission2, permission1));
    Set<ObjectPermissionsResponse> expected = newHashSet(identityPermissions1);
    when(objectIdentityService.getObjectIdentities("entity-typeId", newHashSet(sid1, sid2)))
        .thenReturn(singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")));
    when(identityTools.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
    LinkedHashSet<Sid> sids = new LinkedHashSet<>();
    sids.add(sid1);
    sids.add(sid2);
    when(userRoleTools.getInheritedSids(sids)).thenReturn(sids);
    when(userRoleTools.sortSids(sids)).thenReturn(new LinkedList(sids));

    assertEquals(
        permissionsApiService.getPermissionsForType("entity-typeId", sids, true), expected);
  }

  @Test
  public void testGetPermissionsForTypeWithoutUserQuery() {
    doReturn(true).when(dataService).hasEntityType("typeId");
    PrincipalSid sid1 = new PrincipalSid("user1");
    PrincipalSid sid2 = new PrincipalSid("user2");
    MutableAcl acl = mock(MutableAcl.class);
    ObjectIdentity objectIdentity = mock(ObjectIdentity.class);
    when(objectIdentity.getType()).thenReturn("entity-typeId");
    when(objectIdentity.getIdentifier()).thenReturn("identifier");
    when(acl.getObjectIdentity()).thenReturn(objectIdentity);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    when(ace1.getSid()).thenReturn(sid1);
    when(ace1.getPermission()).thenReturn(PermissionSet.COUNT);
    AccessControlEntry ace2 = mock(AccessControlEntry.class);
    when(ace2.getSid()).thenReturn(sid2);
    when(ace2.getPermission()).thenReturn(PermissionSet.WRITEMETA);
    when(acl.getEntries()).thenReturn(Arrays.asList(ace1, ace2));

    Map<ObjectIdentity, Acl> aclMap = new HashMap<>();
    aclMap.put(new ObjectIdentityImpl("entity-typeId", "identifier"), acl);

    when(mutableAclService.readAclsById(
            singletonList(new ObjectIdentityImpl("entity-typeId", "identifier"))))
        .thenReturn(aclMap);

    Entity entity = mock(Entity.class);
    when(entity.getLabelValue()).thenReturn("label");
    @SuppressWarnings("unchecked")
    Repository<Entity> repo = mock(Repository.class);
    when(repo.findOneById("identifier")).thenReturn(entity);
    when(dataService.getRepository("typeId")).thenReturn(repo);

    ObjectPermission permission1 = ObjectPermission.create(null, "user2", "WRITEMETA", null);
    ObjectPermission permission2 = ObjectPermission.create(null, "user1", "COUNT", null);
    ObjectPermissionsResponse identityPermissions1 =
        ObjectPermissionsResponse.create(
            "identifier", "label", Arrays.asList(permission2, permission1));
    Set<ObjectPermissionsResponse> expected = newHashSet(identityPermissions1);
    when(objectIdentityService.getObjectIdentities("entity-typeId"))
        .thenReturn(singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")));
    when(userRoleTools.getAllAvailableSids()).thenReturn(newHashSet(sid1, sid2));
    when(identityTools.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");

    assertEquals(
        permissionsApiService.getPermissionsForType("entity-typeId", Collections.emptySet(), true),
        expected);
  }

  @Test
  public void testCreateAcl() {
    Entity entity = mock(Entity.class);
    when(dataService.findOneById("typeId", "identifier")).thenReturn(entity);
    when(dataService.hasEntityType("typeId")).thenReturn(true);
    when(identityTools.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
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
    PermissionRequest permission = PermissionRequest.create("role", null, "WRITE");
    when(dataService.findOneById("typeId", "identifier")).thenReturn(entity);
    when(dataService.hasEntityType("typeId")).thenReturn(true);
    Sid expectedSid = new GrantedAuthoritySid("ROLE_role");
    when(userRoleTools.getSid(null, "role")).thenReturn(expectedSid);

    when(identityTools.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
    permissionsApiService.createPermission(
        singletonList(permission), new ObjectIdentityImpl("entity-typeId", "identifier"));

    verify(acl).insertAce(0, PermissionSet.WRITE, expectedSid, true);
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
    PermissionRequest permission1 = PermissionRequest.create("role", null, "WRITE");
    ObjectPermissionsRequest objectPermissionsRequest1 =
        ObjectPermissionsRequest.create("identifier", singletonList(permission1));
    PermissionRequest permission2 = PermissionRequest.create(null, "user1", "READ");
    ObjectPermissionsRequest objectPermissionsRequest2 =
        ObjectPermissionsRequest.create("identifier2", singletonList(permission2));

    Sid expectedSid = new GrantedAuthoritySid("ROLE_role");
    doReturn(expectedSid).when(userRoleTools).getSid(null, "role");
    Sid expectedSid2 = new PrincipalSid("user1");
    doReturn(expectedSid2).when(userRoleTools).getSid("user1", null);
    when(identityTools.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
    doReturn(new ObjectIdentityImpl("entity-typeId", "identifier"))
        .when(identityTools)
        .getObjectIdentity("entity-typeId", "identifier");
    doReturn(new ObjectIdentityImpl("entity-typeId", "identifier2"))
        .when(identityTools)
        .getObjectIdentity("entity-typeId", "identifier2");
    permissionsApiService.createPermissions(
        Arrays.asList(objectPermissionsRequest1, objectPermissionsRequest2), "entity-typeId");

    verify(acl).insertAce(0, PermissionSet.WRITE, expectedSid, true);
    verify(acl2).insertAce(0, PermissionSet.READ, expectedSid2, true);
    verify(mutableAclService).updateAcl(acl);
  }

  @Test
  public void testSetPermission() {
    Entity entity = mock(Entity.class);
    doReturn(entity).when(dataService).findOneById("typeId", "identifier");
    Sid sid = new GrantedAuthoritySid("ROLE_role");
    MutableAcl acl = mock(MutableAcl.class);
    ObjectIdentity objectIdentity = mock(ObjectIdentity.class);
    when(objectIdentity.getType()).thenReturn("entity-typeId");
    when(acl.getObjectIdentity()).thenReturn(objectIdentity);
    doReturn(acl)
        .when(mutableAclService)
        .readAclById(new ObjectIdentityImpl("entity-typeId", "identifier"));

    PermissionRequest permission = PermissionRequest.create("role", null, "WRITE");

    doReturn(true).when(dataService).hasEntityType("typeId");
    when(objectIdentity.getType()).thenReturn("entity-typeId");
    when(objectIdentity.getIdentifier()).thenReturn("identifier");
    when(acl.getObjectIdentity()).thenReturn(objectIdentity);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    when(ace1.getSid()).thenReturn(sid);
    when(ace1.getPermission()).thenReturn(PermissionSet.COUNT);
    when(acl.getEntries()).thenReturn(singletonList(ace1));

    Map<ObjectIdentity, Acl> aclMap = new HashMap<>();
    aclMap.put(new ObjectIdentityImpl("entity-typeId", "identifier"), acl);

    when(mutableAclService.readAclsById(
            singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")),
            singletonList(sid)))
        .thenReturn(aclMap);

    when(entity.getLabelValue()).thenReturn("label");
    @SuppressWarnings("unchecked")
    Repository<Entity> repo = mock(Repository.class);
    when(repo.findOneById("identifier")).thenReturn(entity);
    when(dataService.getRepository("typeId")).thenReturn(repo);

    Sid expectedSid = new GrantedAuthoritySid("ROLE_role");
    doReturn(expectedSid).when(userRoleTools).getSid(null, "role");
    when(identityTools.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
    when(userRoleTools.sortSids(singleton(sid))).thenReturn(new LinkedList(singletonList(sid)));

    permissionsApiService.updatePermission(
        singletonList(permission), new ObjectIdentityImpl("entity-typeId", "identifier"));

    verify(acl).deleteAce(0);
    verify(acl).insertAce(1, PermissionSet.WRITE, expectedSid, true);
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
    PermissionRequest permission = PermissionRequest.create("role", null, "WRITE");
    ObjectPermissionsRequest identityPermissions =
        ObjectPermissionsRequest.create("identifier", singletonList(permission));

    doReturn(true).when(dataService).hasEntityType("typeId");

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    when(ace1.getSid()).thenReturn(sid);
    when(ace1.getPermission()).thenReturn(PermissionSet.COUNT);
    when(acl.getEntries()).thenReturn(singletonList(ace1));

    Map<ObjectIdentity, Acl> aclMap = new HashMap<>();
    aclMap.put(new ObjectIdentityImpl("entity-typeId", "identifier"), acl);

    when(mutableAclService.readAclsById(singletonList(objectIdentity), singletonList(sid)))
        .thenReturn(aclMap);

    when(entity.getLabelValue()).thenReturn("label");
    @SuppressWarnings("unchecked")
    Repository<Entity> repo = mock(Repository.class);
    when(repo.findOneById("identifier")).thenReturn(entity);
    when(dataService.getRepository("typeId")).thenReturn(repo);

    Sid expectedSid = new GrantedAuthoritySid("ROLE_role");
    doReturn(expectedSid).when(userRoleTools).getSid(null, "role");
    when(identityTools.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
    when(identityTools.getObjectIdentity("entity-typeId", "identifier")).thenReturn(objectIdentity);
    when(userRoleTools.sortSids(singleton(sid))).thenReturn(new LinkedList(singletonList(sid)));
    permissionsApiService.updatePermissions(singletonList(identityPermissions), "entity-typeId");

    verify(acl).deleteAce(0);
    verify(acl).insertAce(1, PermissionSet.WRITE, expectedSid, true);
    verify(mutableAclService, times(2)).updateAcl(acl);
  }

  @Test
  public void testDeletePermission() {
    Entity entity = mock(Entity.class);
    when(dataService.findOneById("typeId", "identifier")).thenReturn(entity);
    when(dataService.hasEntityType("typeId")).thenReturn(true);
    when(identityTools.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");

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
    when(identityTools.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
    permissionsApiService.addType("entity-typeId");

    verify(mutableAclClassService).createAclClass("entity-typeId", String.class);
    verify(mutableAclService).createAcl(new EntityIdentity("typeId", "1"));
    verify(mutableAclService).createAcl(new EntityIdentity("typeId", "2"));
  }

  @Test
  public void testDeleteType() {
    when(dataService.hasEntityType("typeId")).thenReturn(true);
    when(identityTools.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
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
