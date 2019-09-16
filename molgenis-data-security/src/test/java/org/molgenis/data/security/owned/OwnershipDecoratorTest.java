package org.molgenis.data.security.owned;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.security.core.PermissionSet.WRITE;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

class OwnershipDecoratorTest extends AbstractMockitoTest {

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

  @BeforeEach
  void beforeMethod() {
    ownershipDecoratorFactory = new OwnershipDecoratorFactory(new Gson(), mutableAclService);
    ownershipDecorator =
        (OwnershipDecorator)
            ownershipDecoratorFactory.createDecoratedRepository(
                delegate, ImmutableMap.of("ownerAttribute", "owner"));
  }

  @Test
  void testAdd() {
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
    assertEquals(ownerSid, acl.getOwner());
    assertEquals(1, acl.getEntries().size());
    AccessControlEntry ace = acl.getEntries().get(0);
    assertEquals(ownerSid, ace.getSid());
    assertEquals(WRITE, ace.getPermission());
    assertTrue(ace.isGranting());
  }

  @Test
  void testAddStream() {
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
    assertEquals(singletonList(entity), streamCaptor.getValue().collect(toList()));
    verify(mutableAclService).updateAcl(acl);
    PrincipalSid ownerSid = new PrincipalSid("username");
    assertEquals(ownerSid, acl.getOwner());
    assertEquals(1, acl.getEntries().size());
    AccessControlEntry ace = acl.getEntries().get(0);
    assertEquals(ownerSid, ace.getSid());
    assertEquals(WRITE, ace.getPermission());
    assertTrue(ace.isGranting());
  }

  @Test
  void testGetFactorySchema() {
    validator.validate("{ownerAttribute: 'owner'}", ownershipDecoratorFactory.getSchema());
  }

  @Test
  void testFactorySchemaChecksForOwnerAttribute() {
    Exception exception =
        assertThrows(
            JsonValidationException.class,
            () -> validator.validate("{}", ownershipDecoratorFactory.getSchema()));
    assertThat(exception.getMessage())
        .containsPattern("violations: #: required key \\[ownerAttribute\\] not found");
  }

  @Test
  void testFactorySchemaChecksOwnerAttributeType() {
    Exception exception =
        assertThrows(
            JsonValidationException.class,
            () -> validator.validate("{ownerAttribute: 1}", ownershipDecoratorFactory.getSchema()));
    assertThat(exception.getMessage())
        .containsPattern("violations: #/ownerAttribute: expected type: String, found: Integer");
  }

  @Test
  void testGetFactoryDescription() {
    assertEquals(
        "When entities are added to the decorated repository, their owner is set to the value of the ownerAttribute.",
        ownershipDecoratorFactory.getDescription());
  }

  @Test
  void testGetFactoryLabel() {
    assertEquals("Ownership decorator", ownershipDecoratorFactory.getLabel());
  }

  @Test
  void testGetFactoryId() {
    assertEquals("ownership", ownershipDecoratorFactory.getId());
  }
}
