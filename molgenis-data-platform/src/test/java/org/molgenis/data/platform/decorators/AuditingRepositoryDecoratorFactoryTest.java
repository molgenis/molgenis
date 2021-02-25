package org.molgenis.data.platform.decorators;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.semantic.Vocabulary.AUDITED;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
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
  void beforeEach(){
    factory = new AuditingRepositoryDecoratorFactory(auditEventPublisher, auditSettings);
  }

  @Test
  void decorateSystemEntityTypeTurnedOn(){
    onSystemEntityType();
    when(auditSettings.getSystemAuditEnabled()).thenReturn(true);

    var decoratedRepo = factory.create(repository);

    assertTrue(decoratedRepo instanceof AuditingRepositoryDecorator);
  }

  @Test
  void decorateSystemEntityTypeTurnedOff(){
    onSystemEntityType();
    when(auditSettings.getSystemAuditEnabled()).thenReturn(false);

    var decoratedRepo = factory.create(repository);

    assertEquals(repository, decoratedRepo);
  }

  @Test
  void decorateSystemEntityTypeExcluded(){
    var entityType = onSystemEntityType();
    when(entityType.getId()).thenReturn("sys_idx_IndexAction");
    when(auditSettings.getSystemAuditEnabled()).thenReturn(true);

    var decoratedRepo = factory.create(repository);

    assertEquals(repository, decoratedRepo);
  }

  @Test
  void decorateDataEntityTypeTurnedOnForAll(){
    onDataEntityType();
    when(auditSettings.getDataAuditSetting()).thenReturn(DataAuditSetting.ALL);

    var decoratedRepo = factory.create(repository);

    assertTrue(decoratedRepo instanceof AuditingRepositoryDecorator);
  }

  @Test
  void decorateDataEntityTypeTurnedOff(){
    onDataEntityType();
    when(auditSettings.getDataAuditSetting()).thenReturn(DataAuditSetting.NONE);

    var decoratedRepo = factory.create(repository);

    assertEquals(repository, decoratedRepo);
  }

  @Test
  void decorateDataEntityTypeTurnedOnForTaggedWithTag(){
    var entityType = onDataEntityType();
    when(auditSettings.getDataAuditSetting()).thenReturn(DataAuditSetting.TAGGED);
    var tag = mock(Tag.class);
    when(tag.getObjectIri()).thenReturn(AUDITED.toString());
    when(entityType.getTags()).thenReturn(singletonList(tag));

    var decoratedRepo = factory.create(repository);

    assertTrue(decoratedRepo instanceof AuditingRepositoryDecorator);
  }

  @Test
  void decorateDataEntityTypeTurnedOnForTaggedWithoutTag(){
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
    when(entityType.getPackage()).thenReturn(pack);
    when(pack.getId()).thenReturn(PACKAGE_SYSTEM);
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