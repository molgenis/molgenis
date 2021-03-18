package org.molgenis.data.platform.decorators;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.event.BootstrappingEvent.BootstrappingStatus.FINISHED;
import static org.molgenis.data.semantic.Relation.isAudited;
import static org.molgenis.data.semantic.Vocabulary.AUDIT_USAGE;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.event.BootstrappingEvent;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.security.audit.AuditingRepositoryDecorator;
import org.molgenis.security.audit.AuditSettings;
import org.molgenis.security.audit.DataAuditSetting;
import org.molgenis.test.AbstractMockitoTest;

class AuditingRepositoryDecoratorFactoryTest extends AbstractMockitoTest {
  @Mock private AuditSettings auditSettings;
  @Mock private AuditEventPublisher auditEventPublisher;
  @Mock private Repository<Entity> repository;

  private AuditingRepositoryDecoratorFactory factory;

  @BeforeEach
  void beforeEach() {
    factory = new AuditingRepositoryDecoratorFactory(auditEventPublisher, auditSettings);

    // fake the bootstrapping event to tell the factory that bootstrapping is finished.
    factory.onBootstrappingEvent(new BootstrappingEvent(FINISHED));
  }

  @Test
  void decorateSystemEntityTypeTurnedOn() {
    onSystemEntityType();
    when(auditSettings.getSystemAuditEnabled()).thenReturn(true);

    var decoratedRepo = factory.create(repository);

    assertTrue(decoratedRepo instanceof AuditingRepositoryDecorator);
  }

  @Test
  void decorateSystemEntityTypeTurnedOff() {
    onSystemEntityType();
    when(auditSettings.getSystemAuditEnabled()).thenReturn(false);

    var decoratedRepo = factory.create(repository);

    assertEquals(repository, decoratedRepo);
  }

  @Test
  void decorateSystemEntityTypeExcluded() {
    var entityType = onSystemEntityType();
    when(entityType.getId()).thenReturn("sys_idx_IndexAction");
    factory.excludeEntityType("sys_idx_IndexAction");

    var decoratedRepo = factory.create(repository);

    assertEquals(repository, decoratedRepo);
  }

  @Test
  void decorateDataEntityTypeTurnedOnForAll() {
    onDataEntityType();
    when(auditSettings.getDataAuditSetting()).thenReturn(DataAuditSetting.ALL);

    var decoratedRepo = factory.create(repository);

    assertTrue(decoratedRepo instanceof AuditingRepositoryDecorator);
  }

  @Test
  void decorateDataEntityTypeTurnedOff() {
    onDataEntityType();
    when(auditSettings.getDataAuditSetting()).thenReturn(DataAuditSetting.NONE);

    var decoratedRepo = factory.create(repository);

    assertEquals(repository, decoratedRepo);
  }

  @Test
  void decorateDataEntityTypeTurnedOnForTaggedWithTag() {
    var entityType = onDataEntityType();
    when(auditSettings.getDataAuditSetting()).thenReturn(DataAuditSetting.TAGGED);
    var tag = mock(Tag.class);
    when(tag.getObjectIri()).thenReturn(AUDIT_USAGE.toString());
    when(tag.getRelationIri()).thenReturn(isAudited.getIRI());
    when(entityType.getTags()).thenReturn(singletonList(tag));

    var decoratedRepo = factory.create(repository);

    assertTrue(decoratedRepo instanceof AuditingRepositoryDecorator);
  }

  @Test
  void decorateDataEntityTypeTurnedOnForTaggedWithoutTag() {
    var entityType = onDataEntityType();
    when(auditSettings.getDataAuditSetting()).thenReturn(DataAuditSetting.TAGGED);
    var tag = mock(Tag.class);
    when(tag.getObjectIri()).thenReturn("not-interested");
    when(entityType.getTags()).thenReturn(singletonList(tag));

    var decoratedRepo = factory.create(repository);

    assertEquals(repository, decoratedRepo);
  }

  private EntityType onSystemEntityType() {
    var entityType = mock(EntityType.class);
    var pack = mock(Package.class);
    when(repository.getEntityType()).thenReturn(entityType);
    lenient().when(entityType.getPackage()).thenReturn(pack);
    lenient().when(pack.getId()).thenReturn(PACKAGE_SYSTEM);
    return entityType;
  }

  private EntityType onDataEntityType() {
    var entityType = mock(EntityType.class);
    var pack = mock(Package.class);
    when(repository.getEntityType()).thenReturn(entityType);
    when(entityType.getPackage()).thenReturn(pack);
    when(pack.getId()).thenReturn("cohort");
    return entityType;
  }
}
