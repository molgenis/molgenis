package org.molgenis.data.security.owned;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.security.core.PermissionSet.WRITE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityIdentity;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.validation.JsonValidationException;
import org.molgenis.validation.JsonValidator;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclImpl;
import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.MutableAclService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OwnershipDecoratorTest extends AbstractMockitoTest {

  private OwnershipDecoratorFactory ownershipDecoratorFactory;

  @Mock private Entity entity;
  @Mock private EntityType entityType;
  @Mock private Repository<Entity> delegate;
  @Mock private MutableAclService mutableAclService;
  @Mock private AclAuthorizationStrategy authorizationStrategy;
  @Mock private AuditLogger auditLogger;
  @Captor private ArgumentCaptor<Stream<Entity>> streamCaptor;

  private OwnershipDecorator ownershipDecorator;
  private JsonValidator validator = new JsonValidator();

  @BeforeMethod
  public void beforeMethod() {
    ownershipDecoratorFactory = new OwnershipDecoratorFactory(new Gson(), mutableAclService);
    ownershipDecorator =
        (OwnershipDecorator)
            ownershipDecoratorFactory.createDecoratedRepository(
                delegate, ImmutableMap.of("ownerAttribute", "owner"));
  }

  @Test
  public void testAdd() {
    EntityIdentity entityIdentity = new EntityIdentity("MyQuestionnaire", "id");
    when(entity.getString("owner")).thenReturn("username");
    when(entity.getIdValue()).thenReturn("id");
    when(entity.getEntityType()).thenReturn(entityType);
    when(entityType.getId()).thenReturn("MyQuestionnaire");
    AclImpl acl = new AclImpl(entityIdentity, 1, authorizationStrategy, auditLogger);
    acl.insertAce(0, PermissionSet.WRITE, new PrincipalSid("otheruser"), true);
    when(mutableAclService.readAclById(entityIdentity)).thenReturn(acl);

    ownershipDecorator.add(entity);

    verify(delegate).add(entity);
    verify(mutableAclService).updateAcl(acl);
    PrincipalSid ownerSid = new PrincipalSid("username");
    assertEquals(acl.getOwner(), ownerSid);
    assertEquals(acl.getEntries().size(), 1);
    AccessControlEntry ace = acl.getEntries().get(0);
    assertEquals(ace.getSid(), ownerSid);
    assertEquals(ace.getPermission(), WRITE);
    assertTrue(ace.isGranting());
  }

  @Test
  public void testAddStream() {
    EntityIdentity entityIdentity = new EntityIdentity("MyQuestionnaire", "id");
    when(entity.getString("owner")).thenReturn("username");
    when(entity.getIdValue()).thenReturn("id");
    when(entity.getEntityType()).thenReturn(entityType);
    when(entityType.getId()).thenReturn("MyQuestionnaire");
    AclImpl acl = new AclImpl(entityIdentity, 1, authorizationStrategy, auditLogger);
    acl.insertAce(0, PermissionSet.WRITE, new PrincipalSid("otheruser"), true);
    when(mutableAclService.readAclById(entityIdentity)).thenReturn(acl);

    ownershipDecorator.add(Stream.of(entity));

    verify(delegate).add(streamCaptor.capture());
    assertEquals(streamCaptor.getValue().collect(toList()), singletonList(entity));
    verify(mutableAclService).updateAcl(acl);
    PrincipalSid ownerSid = new PrincipalSid("username");
    assertEquals(acl.getOwner(), ownerSid);
    assertEquals(acl.getEntries().size(), 1);
    AccessControlEntry ace = acl.getEntries().get(0);
    assertEquals(ace.getSid(), ownerSid);
    assertEquals(ace.getPermission(), WRITE);
    assertTrue(ace.isGranting());
  }

  @Test
  public void testGetFactorySchema() {
    validator.validate("{ownerAttribute: 'owner'}", ownershipDecoratorFactory.getSchema());
  }

  @Test(
      expectedExceptions = JsonValidationException.class,
      expectedExceptionsMessageRegExp =
          "violations: #: required key \\[ownerAttribute\\] not found")
  public void testFactorySchemaChecksForOwnerAttribute() {
    validator.validate("{}", ownershipDecoratorFactory.getSchema());
  }

  @Test(
      expectedExceptions = JsonValidationException.class,
      expectedExceptionsMessageRegExp =
          "violations: #/ownerAttribute: expected type: String, found: Integer")
  public void testFactorySchemaChecksOwnerAttributeType() {
    validator.validate("{ownerAttribute: 1}", ownershipDecoratorFactory.getSchema());
  }

  @Test
  public void testGetFactoryDescription() {
    assertEquals(
        ownershipDecoratorFactory.getDescription(),
        "When entities are added to the decorated repository, their owner is set to the value of the ownerAttribute.");
  }

  @Test
  public void testGetFactoryLabel() {
    assertEquals(ownershipDecoratorFactory.getLabel(), "Ownership decorator");
  }

  @Test
  public void testGetFactoryId() {
    assertEquals(ownershipDecoratorFactory.getId(), "ownership");
  }
}
