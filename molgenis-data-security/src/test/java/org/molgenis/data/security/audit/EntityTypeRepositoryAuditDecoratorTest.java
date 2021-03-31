package org.molgenis.data.security.audit;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    when(repository.findOneById("id")).thenReturn(null);

    decorator.update(newEntityType);

    verify(repository).update(newEntityType);
    verify(auditEventPublisher)
        .publish("bofke", ENTITY_TYPE_AUDIT_ENABLED, Map.of("entityTypeId", "id"));
  }

  @Test
  @WithMockUser("bofke")
  void testAddDisabled() {
    var newEntityType = mock(EntityType.class);
    when(newEntityType.getId()).thenReturn("id");
    when(newEntityType.getTags()).thenReturn(emptyList());
    when(repository.findOneById("id")).thenReturn(null);

    decorator.update(newEntityType);

    verify(repository).update(newEntityType);
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
    when(repository.findOneById("id")).thenReturn(null);

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
  @WithMockUser("henk")
  void testAddStreamDisabled() {
    var newEntityType = mock(EntityType.class);
    when(newEntityType.getId()).thenReturn("id");
    when(newEntityType.getTags()).thenReturn(emptyList());
    when(repository.findOneById("id")).thenReturn(null);

    decorator.update(Stream.of(newEntityType));

    var captor = ArgumentCaptor.forClass(Stream.class);
    verify(repository).update(captor.capture());
    captor
        .getValue()
        .forEach(
            e -> {
              // consume
            });
    verifyNoInteractions(auditEventPublisher);
  }

  private static Tag mockAuditTag() {
    var auditTag = mock(Tag.class);
    when(auditTag.getObjectIri()).thenReturn(AUDIT_USAGE.toString());
    when(auditTag.getRelationIri()).thenReturn(isAudited.getIRI());
    return auditTag;
  }
}
