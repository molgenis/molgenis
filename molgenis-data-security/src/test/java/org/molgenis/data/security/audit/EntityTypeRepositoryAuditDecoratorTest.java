package org.molgenis.data.security.audit;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.audit.EntityTypeRepositoryAuditDecorator.ENTITY_TYPE_AUDIT_DISABLED;
import static org.molgenis.data.security.audit.EntityTypeRepositoryAuditDecorator.ENTITY_TYPE_AUDIT_ENABLED;
import static org.molgenis.data.semantic.Relation.isAudited;
import static org.molgenis.data.semantic.Vocabulary.AUDIT_USAGE;

import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = EntityTypeRepositoryAuditDecoratorTest.Config.class)
class EntityTypeRepositoryAuditDecoratorTest extends AbstractMockitoSpringContextTests {

  @Configuration
  static class Config {}

  @Mock private AuditEventPublisher auditEventPublisher;
  @Mock private Repository<EntityType> repository;

  private EntityTypeRepositoryAuditDecorator decorator;
  private SecurityContext previousContext;

  @BeforeEach
  void setUpBeforeEach() {
    previousContext = SecurityContextHolder.getContext();
    decorator = new EntityTypeRepositoryAuditDecorator(repository, auditEventPublisher);
  }

  @AfterEach
  void tearDownAfterEach() {
    SecurityContextHolder.setContext(previousContext);
  }

  @Test
  @WithMockUser("bofke")
  void testUpdateEnable() {
    var oldEntityType = mock(EntityType.class);
    var newEntityType = mock(EntityType.class);
    when(newEntityType.getId()).thenReturn("id");
    Tag auditTag = mockAuditTag();
    var otherTag = mock(Tag.class);
    when(otherTag.getObjectIri()).thenReturn("something-else");
    when(oldEntityType.getTags()).thenReturn(singletonList(otherTag));
    when(newEntityType.getTags()).thenReturn(asList(otherTag, auditTag));
    when(repository.findOneById("id")).thenReturn(oldEntityType);

    decorator.update(newEntityType);

    verify(repository).update(newEntityType);
    verify(auditEventPublisher)
        .publish("bofke", ENTITY_TYPE_AUDIT_ENABLED, Map.of("entityTypeId", "id"));
  }

  @Test
  @WithMockUser("bofke")
  void testUpdateDisable() {
    var oldEntityType = mock(EntityType.class);
    var newEntityType = mock(EntityType.class);
    when(newEntityType.getId()).thenReturn("id");
    Tag auditTag = mockAuditTag();
    when(oldEntityType.getTags()).thenReturn(singletonList(auditTag));
    when(newEntityType.getTags()).thenReturn(emptyList());
    when(repository.findOneById("id")).thenReturn(oldEntityType);

    decorator.update(newEntityType);

    verify(repository).update(newEntityType);
    verify(auditEventPublisher)
        .publish("bofke", ENTITY_TYPE_AUDIT_DISABLED, Map.of("entityTypeId", "id"));
  }

  @SuppressWarnings("unchecked")
  @Test
  @WithMockUser("henk")
  void testUpdateStreamEnable() {
    var oldEntityType = mock(EntityType.class);
    var newEntityType = mock(EntityType.class);
    when(newEntityType.getId()).thenReturn("id");
    Tag auditTag = mockAuditTag();
    var otherTag = mock(Tag.class);
    when(otherTag.getObjectIri()).thenReturn("something-else");
    when(oldEntityType.getTags()).thenReturn(singletonList(otherTag));
    when(newEntityType.getTags()).thenReturn(asList(otherTag, auditTag));
    when(repository.findOneById("id")).thenReturn(oldEntityType);

    decorator.update(Stream.of(newEntityType));

    var captor = ArgumentCaptor.forClass(Stream.class);
    verify(repository).update(captor.capture());
    captor
        .getValue()
        .forEach(
            e -> {
              // consume
            });
    verify(auditEventPublisher)
        .publish("henk", ENTITY_TYPE_AUDIT_ENABLED, Map.of("entityTypeId", "id"));
  }

  @SuppressWarnings("unchecked")
  @Test
  @WithMockUser("bofke")
  void testUpdateStreamDisable() {
    var oldEntityType = mock(EntityType.class);
    var newEntityType = mock(EntityType.class);
    when(newEntityType.getId()).thenReturn("id");
    Tag auditTag = mockAuditTag();
    when(oldEntityType.getTags()).thenReturn(singletonList(auditTag));
    when(newEntityType.getTags()).thenReturn(emptyList());
    when(repository.findOneById("id")).thenReturn(oldEntityType);

    decorator.update(Stream.of(newEntityType));

    var captor = ArgumentCaptor.forClass(Stream.class);
    verify(repository).update(captor.capture());
    captor
        .getValue()
        .forEach(
            e -> {
              // consume
            });
    verify(auditEventPublisher)
        .publish("bofke", ENTITY_TYPE_AUDIT_DISABLED, Map.of("entityTypeId", "id"));
  }

  @Test
  @WithMockUser("bofke")
  void testAdd() {
    var newEntityType = mock(EntityType.class);
    when(newEntityType.getId()).thenReturn("id");
    Tag auditTag = mockAuditTag();
    var otherTag = mock(Tag.class);
    when(otherTag.getObjectIri()).thenReturn("something-else");
    when(newEntityType.getTags()).thenReturn(asList(otherTag, auditTag));

    decorator.add(newEntityType);

    verify(repository).add(newEntityType);
    verify(auditEventPublisher)
        .publish("bofke", ENTITY_TYPE_AUDIT_ENABLED, Map.of("entityTypeId", "id"));
  }

  @Test
  @WithMockUser("bofke")
  void testAddDisabled() {
    var newEntityType = mock(EntityType.class);
    when(newEntityType.getTags()).thenReturn(emptyList());

    decorator.add(newEntityType);

    verify(repository).add(newEntityType);
    verifyNoInteractions(auditEventPublisher);
  }

  @SuppressWarnings("unchecked")
  @Test
  @WithMockUser("henk")
  void testAddStream() {
    var newEntityType = mock(EntityType.class);
    when(newEntityType.getId()).thenReturn("id");
    Tag auditTag = mockAuditTag();
    var otherTag = mock(Tag.class);
    when(otherTag.getObjectIri()).thenReturn("something-else");
    when(newEntityType.getTags()).thenReturn(asList(otherTag, auditTag));

    decorator.add(Stream.of(newEntityType));

    var captor = ArgumentCaptor.forClass(Stream.class);
    verify(repository).add(captor.capture());
    captor
        .getValue()
        .forEach(
            e -> {
              // consume
            });
    verify(auditEventPublisher)
        .publish("henk", ENTITY_TYPE_AUDIT_ENABLED, Map.of("entityTypeId", "id"));
  }

  @SuppressWarnings("unchecked")
  @Test
  @WithMockUser("henk")
  void testAddStreamDisabled() {
    var newEntityType = mock(EntityType.class);
    when(newEntityType.getTags()).thenReturn(emptyList());

    decorator.add(Stream.of(newEntityType));

    var captor = ArgumentCaptor.forClass(Stream.class);
    verify(repository).add(captor.capture());
    captor
        .getValue()
        .forEach(
            e -> {
              // consume
            });
    verifyNoInteractions(auditEventPublisher);
  }

  @Test
  @WithMockUser("bofke")
  void testDelete() {
    var entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("id");
    Tag auditTag = mockAuditTag();
    when(entityType.getTags()).thenReturn(singletonList(auditTag));

    decorator.delete(entityType);

    verify(repository).delete(entityType);
    verify(auditEventPublisher)
        .publish("bofke", ENTITY_TYPE_AUDIT_DISABLED, Map.of("entityTypeId", "id"));
  }

  @Test
  @WithMockUser("bofke")
  void testDeleteDisabled() {
    var entityType = mock(EntityType.class);
    var otherTag = mock(Tag.class);
    when(otherTag.getObjectIri()).thenReturn("something-else");
    when(entityType.getTags()).thenReturn(singletonList(otherTag));

    decorator.delete(entityType);

    verify(repository).delete(entityType);
    verifyNoInteractions(auditEventPublisher);
  }

  @Test
  @WithMockUser("bofke")
  void testDeleteById() {
    var entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("id");
    Tag auditTag = mockAuditTag();
    when(entityType.getTags()).thenReturn(singletonList(auditTag));
    when(repository.findOneById("id")).thenReturn(entityType);

    decorator.deleteById("id");

    verify(repository).delete(entityType);
    verify(auditEventPublisher)
        .publish("bofke", ENTITY_TYPE_AUDIT_DISABLED, Map.of("entityTypeId", "id"));
  }

  @Test
  @WithMockUser("bofke")
  void testDeleteByIdDisabled() {
    var entityType = mock(EntityType.class);
    when(entityType.getTags()).thenReturn(emptyList());
    when(repository.findOneById("id")).thenReturn(entityType);

    decorator.deleteById("id");

    verify(repository).delete(entityType);
    verifyNoInteractions(auditEventPublisher);
  }

  @Test
  @WithMockUser("bofke")
  void testDeleteByIdUnknown() {
    when(repository.findOneById("id")).thenReturn(null);

    assertThrows(UnknownEntityTypeException.class, () -> decorator.deleteById("id"));

    verifyNoMoreInteractions(repository);
    verifyNoInteractions(auditEventPublisher);
  }

  @Test
  void testDeleteAll() {
    assertThrows(UnsupportedOperationException.class, () -> decorator.deleteAll());
  }

  @SuppressWarnings("unchecked")
  @Test
  @WithMockUser("henk")
  void testDeleteStream() {
    var entityType1 = mock(EntityType.class);
    var entityType2 = mock(EntityType.class);
    when(entityType1.getId()).thenReturn("id1");
    Tag auditTag = mockAuditTag();
    when(entityType1.getTags()).thenReturn(singletonList(auditTag));
    when(entityType2.getTags()).thenReturn(emptyList());

    decorator.delete(Stream.of(entityType1, entityType2));

    var captor = ArgumentCaptor.forClass(Stream.class);
    verify(repository).delete(captor.capture());
    captor
        .getValue()
        .forEach(
            e -> {
              // consume
            });
    verify(auditEventPublisher)
        .publish("henk", ENTITY_TYPE_AUDIT_DISABLED, Map.of("entityTypeId", "id1"));
  }

  @SuppressWarnings("unchecked")
  @Test
  @WithMockUser("henk")
  void testDeleteAllStream() {
    var entityType1 = mock(EntityType.class);
    var entityType2 = mock(EntityType.class);
    when(entityType1.getId()).thenReturn("id1");
    Tag auditTag = mockAuditTag();
    when(entityType1.getTags()).thenReturn(singletonList(auditTag));
    when(entityType2.getTags()).thenReturn(emptyList());
    when(repository.findAll(any(Stream.class))).thenReturn(Stream.of(entityType1, entityType2));

    decorator.deleteAll(Stream.of("id1", "id2"));

    var captor = ArgumentCaptor.forClass(Stream.class);
    verify(repository).delete(captor.capture());
    captor
        .getValue()
        .forEach(
            e -> {
              // consume
            });
    verify(auditEventPublisher)
        .publish("henk", ENTITY_TYPE_AUDIT_DISABLED, Map.of("entityTypeId", "id1"));
  }

  private static Tag mockAuditTag() {
    var auditTag = mock(Tag.class);
    when(auditTag.getObjectIri()).thenReturn(AUDIT_USAGE.toString());
    when(auditTag.getRelationIri()).thenReturn(isAudited.getIRI());
    return auditTag;
  }
}
