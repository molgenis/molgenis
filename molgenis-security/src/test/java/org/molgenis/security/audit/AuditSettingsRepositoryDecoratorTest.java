package org.molgenis.security.audit;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.security.audit.AuditSettingsRepositoryDecorator.AUDIT_SETTING_CHANGED;

import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = AuditSettingsRepositoryDecoratorTest.Config.class)
class AuditSettingsRepositoryDecoratorTest extends AbstractMockitoSpringContextTests {

  @Configuration
  static class Config {}

  @Mock private AuditEventPublisher auditEventPublisher;
  @Mock private Repository<Entity> repository;

  private AuditSettingsRepositoryDecorator decorator;
  private SecurityContext previousContext;

  @BeforeEach
  void setUpBeforeEach() {
    previousContext = SecurityContextHolder.getContext();
    decorator = new AuditSettingsRepositoryDecorator(repository, auditEventPublisher);
  }

  @AfterEach
  void tearDownAfterEach() {
    SecurityContextHolder.setContext(previousContext);
  }

  @Test
  @WithMockUser("henk")
  void testUpdate() {
    setupEntityType();
    var newEntity = mock(Entity.class);
    when(newEntity.getIdValue()).thenReturn("id");
    var oldEntity = mock(Entity.class);
    when(oldEntity.get("attr1")).thenReturn(true);
    when(newEntity.get("attr1")).thenReturn(false);
    when(oldEntity.get("attr2")).thenReturn(true);
    when(newEntity.get("attr2")).thenReturn(true);

    when(repository.findOneById("id")).thenReturn(oldEntity);

    decorator.update(newEntity);

    verify(repository).update(newEntity);
    verify(auditEventPublisher)
        .publish(
            "henk",
            AUDIT_SETTING_CHANGED,
            Map.of("setting", "attr1", "oldValue", true, "newValue", false));
  }

  @SuppressWarnings("unchecked")
  @Test
  @WithMockUser("henk")
  void testUpdateStream() {
    setupEntityType();
    var newEntity = mock(Entity.class);
    when(newEntity.getIdValue()).thenReturn("id");
    var oldEntity = mock(Entity.class);
    when(oldEntity.get("attr1")).thenReturn(true);
    when(newEntity.get("attr1")).thenReturn(false);
    when(oldEntity.get("attr2")).thenReturn(false);
    when(newEntity.get("attr2")).thenReturn(true);

    when(repository.findOneById("id")).thenReturn(oldEntity);

    decorator.update(Stream.of(newEntity));

    var captor = ArgumentCaptor.forClass(Stream.class);
    verify(repository).update(captor.capture());
    captor
        .getValue()
        .forEach(
            entity -> {
              // consume
            });
    verify(auditEventPublisher)
        .publish(
            "henk",
            AUDIT_SETTING_CHANGED,
            Map.of("setting", "attr1", "oldValue", true, "newValue", false));
    verify(auditEventPublisher)
        .publish(
            "henk",
            AUDIT_SETTING_CHANGED,
            Map.of("setting", "attr2", "oldValue", false, "newValue", true));
  }

  private void setupEntityType() {
    var entityType = mock(EntityType.class);
    when(repository.getEntityType()).thenReturn(entityType);
    var attribute1 = mock(Attribute.class);
    when(attribute1.getName()).thenReturn("attr1");
    var attribute2 = mock(Attribute.class);
    when(attribute2.getName()).thenReturn("attr2");
    when(entityType.getAtomicAttributes()).thenReturn(asList(attribute1, attribute2));
  }
}
