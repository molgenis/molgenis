package org.molgenis.api.permissions;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.api.permissions.PermissionSetUtils.COUNT;
import static org.molgenis.api.permissions.PermissionSetUtils.READ;
import static org.molgenis.api.permissions.PermissionSetUtils.READMETA;
import static org.molgenis.api.permissions.PermissionSetUtils.WRITE;
import static org.molgenis.api.permissions.PermissionSetUtils.WRITEMETA;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;
import static org.testng.Assert.*;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.mockito.Mock;
import org.molgenis.api.permissions.inheritance.PermissionInheritanceResolver;
import org.molgenis.api.permissions.inheritance.UserRoleInheritanceResolver;
import org.molgenis.api.permissions.model.request.ObjectPermissionsRequest;
import org.molgenis.api.permissions.model.request.PermissionRequest;
import org.molgenis.api.permissions.model.response.ObjectPermissionsResponse;
import org.molgenis.api.permissions.model.response.PermissionResponse;
import org.molgenis.api.permissions.model.response.TypePermissionsResponse;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.security.acl.AclClassService;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.acl.ObjectIdentityService;
import org.molgenis.security.core.PermissionSet;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PermissionApiServiceTest extends AbstractMolgenisSpringTest {

  @Mock MutableAclService mutableAclService;
  @Mock AclClassService aclClassService;
  @Mock PermissionInheritanceResolver inheritanceResolver;
  @Mock ObjectIdentityService objectIdentityService;
  @Mock DataService dataService;
  @Mock MutableAclClassService mutableAclClassService;
  @Mock UserRoleInheritanceResolver userRoleInheritanceResolver;
  private PermissionApiService permissionApiService;

  @BeforeMethod
  public void setUp() {
    initMocks(this);
    permissionApiService =
        new PermissionApiService(
            mutableAclService,
            aclClassService,
            inheritanceResolver,
            objectIdentityService,
            dataService,
            mutableAclClassService,
            userRoleInheritanceResolver);
  }

  @Test
  public void testGetClasses() {
    resetMocks();
    when(aclClassService.getAclClassTypes())
        .thenReturn(Arrays.asList("entity-test1", "entity-test2"));
    doReturn(true).when(dataService).hasEntityType("test1");
    doReturn(false).when(dataService).hasEntityType("test2");

    assertEquals(permissionApiService.getClasses(), Collections.singletonList("entity-test1"));
  }

  @Test
  public void testGetAcls() {
    resetMocks();
    setSuAuthentication(false);
    when(objectIdentityService.getObjectIdentities("entity-type", 10, 0))
        .thenReturn(
            Arrays.asList(
                new ObjectIdentityImpl("classId", "test1"),
                new ObjectIdentityImpl("classId", "test2")));
    doReturn(true).when(dataService).hasEntityType("type");
    assertEquals(
        permissionApiService.getAcls("entity-type", 1, 10), Arrays.asList("test1", "test2"));
  }

  @Test
  public void testGetSuitablePermissionsForType() {
    assertEquals(
        permissionApiService.getSuitablePermissionsForType(EntityTypeIdentity.ENTITY_TYPE),
        Sets.newHashSet(READMETA, COUNT, READ, WRITE, WRITEMETA));
    assertEquals(
        permissionApiService.getSuitablePermissionsForType(PackageIdentity.PACKAGE),
        Sets.newHashSet(READMETA, COUNT, READ, WRITE, WRITEMETA));
    assertEquals(
        permissionApiService.getSuitablePermissionsForType(PluginIdentity.PLUGIN),
        Sets.newHashSet(READ));
    assertEquals(
        permissionApiService.getSuitablePermissionsForType("entity-row_level_secured_entity"),
        Sets.newHashSet(READ, WRITE));
  }

  @Test
  public void testGetPermission() {
    doReturn(true).when(dataService).hasEntityType("typeId");
    setSuAuthentication(false);
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
            Collections.singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")),
            Arrays.asList(sid1, sid2)))
        .thenReturn(aclMap);

    EntityType entityType = mock(EntityType.class);
    Attribute attribute = mock(Attribute.class);

    when(entityType.getIdAttribute()).thenReturn(attribute);
    when(attribute.getDataType()).thenReturn(AttributeType.STRING);
    when(dataService.getEntityType("typeId")).thenReturn(entityType);

    Entity entity = mock(Entity.class);
    when(entity.getLabelValue()).thenReturn("label");
    Repository repo = mock(Repository.class);
    when(repo.findOneById("identifier")).thenReturn(entity);
    when(dataService.getRepository("typeId")).thenReturn(repo);

    PermissionResponse permission1 = PermissionResponse.create(null, "user2", "WRITEMETA", null);
    PermissionResponse permission2 = PermissionResponse.create(null, "user1", "COUNT", null);
    Set<PermissionResponse> expected = Sets.newHashSet(permission1, permission2);

    Set<Sid> sids = new LinkedHashSet<>();
    sids.add(sid1);
    sids.add(sid2);
    assertEquals(
        Sets.newHashSet(
            permissionApiService.getPermission("entity-typeId", "identifier", sids, true)),
        expected);
  }

  @Test
  public void testGetAllPermissions() {

    when(aclClassService.getAclClassTypes()).thenReturn(Collections.singletonList("entity-typeId"));

    doReturn(true).when(dataService).hasEntityType("typeId");
    setSuAuthentication(true);
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
            Collections.singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")),
            Arrays.asList(sid1, sid2)))
        .thenReturn(aclMap);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getLabel()).thenReturn("TypeLabel");

    Entity entity = mock(Entity.class);
    when(entity.getLabelValue()).thenReturn("label");
    Repository repo = mock(Repository.class);
    when(repo.findOneById("identifier")).thenReturn(entity);
    when(repo.getEntityType()).thenReturn(entityType);
    when(dataService.getRepository("typeId")).thenReturn(repo);

    PermissionResponse permission1 = PermissionResponse.create(null, "user1", "COUNT", null);
    PermissionResponse permission2 = PermissionResponse.create(null, "user2", "WRITEMETA", null);
    ObjectPermissionsResponse identityPermissions1 =
        ObjectPermissionsResponse.create(
            "identifier", "label", Arrays.asList(permission1, permission2));
    TypePermissionsResponse classPermissions1 =
        TypePermissionsResponse.create(
            "entity-typeId", "TypeLabel", Collections.singletonList(identityPermissions1));
    Set<TypePermissionsResponse> expected = Sets.newHashSet(classPermissions1);
    when(objectIdentityService.getObjectIdentities("entity-typeId", Sets.newHashSet(sid1, sid2)))
        .thenReturn(
            Collections.singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")));

    Set<Sid> sids = new LinkedHashSet<>();
    sids.add(sid1);
    sids.add(sid2);

    assertEquals(Sets.newHashSet(permissionApiService.getAllPermissions(sids, true)), expected);
  }

  @Test
  public void testGetPagedPermissionsForType() {
    doReturn(true).when(dataService).hasEntityType("typeId");
    setSuAuthentication(false);
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
            Collections.singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")),
            Arrays.asList(sid1, sid2)))
        .thenReturn(aclMap);

    Entity entity = mock(Entity.class);
    when(entity.getLabelValue()).thenReturn("label");
    Repository repo = mock(Repository.class);
    when(repo.findOneById("identifier")).thenReturn(entity);
    when(dataService.getRepository("typeId")).thenReturn(repo);

    PermissionResponse permission1 = PermissionResponse.create(null, "user1", "COUNT", null);
    PermissionResponse permission2 = PermissionResponse.create(null, "user2", "WRITEMETA", null);
    ObjectPermissionsResponse identityPermissions1 =
        ObjectPermissionsResponse.create(
            "identifier", "label", Arrays.asList(permission1, permission2));
    Set<ObjectPermissionsResponse> expected = Sets.newHashSet(identityPermissions1);
    when(objectIdentityService.getObjectIdentities(
            "entity-typeId", Sets.newHashSet(sid1, sid2), 20, 60))
        .thenReturn(
            Collections.singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")));
    Set<Sid> sids = new LinkedHashSet<>();
    sids.add(sid1);
    sids.add(sid2);
    assertEquals(
        permissionApiService.getPagedPermissionsForType("entity-typeId", sids, 4, 20), expected);
  }

  @Test
  public void testGetPermissionsForType() {
    doReturn(true).when(dataService).hasEntityType("typeId");
    setSuAuthentication(false);
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
            Collections.singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")),
            Arrays.asList(sid1, sid2)))
        .thenReturn(aclMap);

    Entity entity = mock(Entity.class);
    when(entity.getLabelValue()).thenReturn("label");
    Repository repo = mock(Repository.class);
    when(repo.findOneById("identifier")).thenReturn(entity);
    when(dataService.getRepository("typeId")).thenReturn(repo);

    PermissionResponse permission1 = PermissionResponse.create(null, "user2", "WRITEMETA", null);
    PermissionResponse permission2 = PermissionResponse.create(null, "user1", "COUNT", null);
    ObjectPermissionsResponse identityPermissions1 =
        ObjectPermissionsResponse.create(
            "identifier", "label", Arrays.asList(permission2, permission1));
    Set<ObjectPermissionsResponse> expected = Sets.newHashSet(identityPermissions1);
    when(objectIdentityService.getObjectIdentities("entity-typeId", Sets.newHashSet(sid1, sid2)))
        .thenReturn(
            Collections.singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")));

    Set<Sid> sids = new LinkedHashSet<>();
    sids.add(sid1);
    sids.add(sid2);

    assertEquals(permissionApiService.getPermissionsForType("entity-typeId", sids, true), expected);
  }

  @Test
  public void testGetPermissionsForTypeWithoutUserQuery() {
    doReturn(true).when(dataService).hasEntityType("typeId");
    setSuAuthentication(false);
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
            Collections.singletonList(new ObjectIdentityImpl("entity-typeId", "identifier"))))
        .thenReturn(aclMap);

    Entity entity = mock(Entity.class);
    when(entity.getLabelValue()).thenReturn("label");
    Repository repo = mock(Repository.class);
    when(repo.findOneById("identifier")).thenReturn(entity);
    when(dataService.getRepository("typeId")).thenReturn(repo);

    PermissionResponse permission1 = PermissionResponse.create(null, "user2", "WRITEMETA", null);
    PermissionResponse permission2 = PermissionResponse.create(null, "user1", "COUNT", null);
    ObjectPermissionsResponse identityPermissions1 =
        ObjectPermissionsResponse.create(
            "identifier", "label", Arrays.asList(permission2, permission1));
    Set<ObjectPermissionsResponse> expected = Sets.newHashSet(identityPermissions1);
    when(objectIdentityService.getObjectIdentities("entity-typeId"))
        .thenReturn(
            Collections.singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")));
    when(userRoleInheritanceResolver.getAllAvailableSids()).thenReturn(Sets.newHashSet(sid1, sid2));

    assertEquals(
        permissionApiService.getPermissionsForType("entity-typeId", Collections.emptySet(), true),
        expected);
  }

  @Test
  public void testCreateAcl() {
    setSuAuthentication(false);
    EntityType entityType = mock(EntityType.class);
    Attribute attribute = mock(Attribute.class);
    when(entityType.getIdAttribute()).thenReturn(attribute);
    when(attribute.getDataType()).thenReturn(AttributeType.STRING);
    when(dataService.getEntityType("typeId")).thenReturn(entityType);
    permissionApiService.createAcl("entity-typeId", "identifier");
    verify(mutableAclService).createAcl(new ObjectIdentityImpl("entity-typeId", "identifier"));
  }

  @Test
  public void testCreatePermission() {
    setSuAuthentication(true);
    Sid sid = mock(Sid.class);
    MutableAcl acl = mock(MutableAcl.class);
    when(acl.getOwner()).thenReturn(sid);
    when(mutableAclService.readAclById(new ObjectIdentityImpl("typeId", "identifier")))
        .thenReturn(acl);
    PermissionRequest permission = PermissionRequest.create("role", null, "WRITE");

    permissionApiService.createPermission(
        Collections.singletonList(permission), "typeId", "identifier");

    Sid expectedSid = new GrantedAuthoritySid("ROLE_role");
    verify(acl).insertAce(0, PermissionSet.WRITE, expectedSid, true);
    verify(mutableAclService).updateAcl(acl);
  }

  @Test
  public void testCreatePermissions() {
    setSuAuthentication(true);
    Sid sid = mock(Sid.class);
    MutableAcl acl = mock(MutableAcl.class);
    when(acl.getOwner()).thenReturn(sid);
    Sid sid2 = mock(Sid.class);
    MutableAcl acl2 = mock(MutableAcl.class);
    when(acl2.getOwner()).thenReturn(sid2);

    doReturn(acl)
        .when(mutableAclService)
        .readAclById(new ObjectIdentityImpl("entity-typeId", "identifier"));
    doReturn(acl2)
        .when(mutableAclService)
        .readAclById(new ObjectIdentityImpl("entity-typeId", "identifier2"));
    PermissionRequest permission1 = PermissionRequest.create("role", null, "WRITE");
    ObjectPermissionsRequest objectPermissionsRequest1 =
        ObjectPermissionsRequest.create("identifier", Collections.singletonList(permission1));
    PermissionRequest permission2 = PermissionRequest.create(null, "user1", "READ");
    ObjectPermissionsRequest objectPermissionsRequest2 =
        ObjectPermissionsRequest.create("identifier2", Collections.singletonList(permission2));

    EntityType entityType = mock(EntityType.class);
    Attribute attribute = mock(Attribute.class);
    when(entityType.getIdAttribute()).thenReturn(attribute);
    when(attribute.getDataType()).thenReturn(AttributeType.STRING);
    when(dataService.getEntityType("typeId")).thenReturn(entityType);

    permissionApiService.createPermissions(
        Arrays.asList(objectPermissionsRequest1, objectPermissionsRequest2), "entity-typeId");

    Sid expectedSid = new GrantedAuthoritySid("ROLE_role");
    Sid expectedSid2 = new PrincipalSid("user1");
    verify(acl).insertAce(0, PermissionSet.WRITE, expectedSid, true);
    verify(acl2).insertAce(0, PermissionSet.READ, expectedSid2, true);
    verify(mutableAclService).updateAcl(acl);
  }

  @Test
  public void testSetPermission() {
    setSuAuthentication(true);
    Sid sid = new GrantedAuthoritySid("ROLE_role");
    MutableAcl acl = mock(MutableAcl.class);
    when(acl.getOwner()).thenReturn(sid);
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
    when(acl.getEntries()).thenReturn(Arrays.asList(ace1));

    Map<ObjectIdentity, Acl> aclMap = new HashMap<>();
    aclMap.put(new ObjectIdentityImpl("entity-typeId", "identifier"), acl);

    when(mutableAclService.readAclsById(
            Collections.singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")),
            Arrays.asList(sid)))
        .thenReturn(aclMap);

    EntityType entityType = mock(EntityType.class);
    Attribute attribute = mock(Attribute.class);

    when(entityType.getIdAttribute()).thenReturn(attribute);
    when(attribute.getDataType()).thenReturn(AttributeType.STRING);
    when(dataService.getEntityType("typeId")).thenReturn(entityType);

    Entity entity = mock(Entity.class);
    when(entity.getLabelValue()).thenReturn("label");
    Repository repo = mock(Repository.class);
    when(repo.findOneById("identifier")).thenReturn(entity);
    when(dataService.getRepository("typeId")).thenReturn(repo);

    permissionApiService.updatePermission(
        Collections.singletonList(permission), "entity-typeId", "identifier");

    Sid expectedSid = new GrantedAuthoritySid("ROLE_role");
    verify(acl).deleteAce(0);
    verify(acl).insertAce(1, PermissionSet.WRITE, expectedSid, true);
    verify(mutableAclService, times(2)).updateAcl(acl);
  }

  @Test
  public void testSetPermissions() {
    setSuAuthentication(true);
    Sid sid = new GrantedAuthoritySid("ROLE_role");
    MutableAcl acl = mock(MutableAcl.class);
    when(acl.getOwner()).thenReturn(sid);
    ObjectIdentity objectIdentity = mock(ObjectIdentity.class);
    when(objectIdentity.getType()).thenReturn("entity-typeId");
    when(acl.getObjectIdentity()).thenReturn(objectIdentity);
    doReturn(acl)
        .when(mutableAclService)
        .readAclById(new ObjectIdentityImpl("entity-typeId", "identifier"));
    PermissionRequest permission = PermissionRequest.create("role", null, "WRITE");
    ObjectPermissionsRequest identityPermissions =
        ObjectPermissionsRequest.create("identifier", Collections.singletonList(permission));

    doReturn(true).when(dataService).hasEntityType("typeId");
    when(objectIdentity.getType()).thenReturn("entity-typeId");
    when(objectIdentity.getIdentifier()).thenReturn("identifier");
    when(acl.getObjectIdentity()).thenReturn(objectIdentity);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    when(ace1.getSid()).thenReturn(sid);
    when(ace1.getPermission()).thenReturn(PermissionSet.COUNT);
    when(acl.getEntries()).thenReturn(Arrays.asList(ace1));

    Map<ObjectIdentity, Acl> aclMap = new HashMap<>();
    aclMap.put(new ObjectIdentityImpl("entity-typeId", "identifier"), acl);

    when(mutableAclService.readAclsById(
            Collections.singletonList(new ObjectIdentityImpl("entity-typeId", "identifier")),
            Arrays.asList(sid)))
        .thenReturn(aclMap);

    EntityType entityType = mock(EntityType.class);
    Attribute attribute = mock(Attribute.class);

    when(entityType.getIdAttribute()).thenReturn(attribute);
    when(attribute.getDataType()).thenReturn(AttributeType.STRING);
    when(dataService.getEntityType("typeId")).thenReturn(entityType);

    Entity entity = mock(Entity.class);
    when(entity.getLabelValue()).thenReturn("label");
    Repository repo = mock(Repository.class);
    when(repo.findOneById("identifier")).thenReturn(entity);
    when(dataService.getRepository("typeId")).thenReturn(repo);

    permissionApiService.updatePermissions(
        Collections.singletonList(identityPermissions), "entity-typeId");

    Sid expectedSid = new GrantedAuthoritySid("ROLE_role");

    verify(acl).deleteAce(0);
    verify(acl).insertAce(1, PermissionSet.WRITE, expectedSid, true);
    verify(mutableAclService, times(2)).updateAcl(acl);
  }

  @Test
  public void testDeletePermission() {
    setSuAuthentication(true);
    Sid sid = mock(Sid.class);
    MutableAcl acl = mock(MutableAcl.class);
    when(acl.getOwner()).thenReturn(sid);
    AccessControlEntry ace = mock(AccessControlEntry.class);
    when(ace.getSid()).thenReturn(sid);
    when(acl.getEntries()).thenReturn(Collections.singletonList(ace));
    when(mutableAclService.readAclById(
            new ObjectIdentityImpl("typeId", "identifier"), Collections.singletonList(sid)))
        .thenReturn(acl);
    permissionApiService.deletePermission(sid, "typeId", "identifier");
    verify(acl).deleteAce(0);
    verify(mutableAclService).updateAcl(acl);
  }

  private void setSuAuthentication(boolean withDetails) {
    Authentication authentication = mock(Authentication.class);
    if (withDetails) {
      UserDetails userDetails = mock(UserDetails.class);
      when(userDetails.getUsername()).thenReturn("admin");
      when(authentication.getPrincipal()).thenReturn(userDetails);
    }
    SecurityContextHolder.getContext().setAuthentication(authentication);
    GrantedAuthority authoritySu = mock(GrantedAuthority.class);
    when(authoritySu.getAuthority()).thenReturn(AUTHORITY_SU);
    when((Collection<GrantedAuthority>) authentication.getAuthorities())
        .thenReturn(Collections.singletonList(authoritySu));
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
