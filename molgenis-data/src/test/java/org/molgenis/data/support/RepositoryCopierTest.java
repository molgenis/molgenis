package org.molgenis.data.support;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.test.AbstractMockitoTest;

class RepositoryCopierTest extends AbstractMockitoTest {
  private RepositoryCopier repositoryCopier;
  @Mock private MetaDataService metaDataService;
  @Mock private AttributeFactory attributeFactory;

  @BeforeEach
  void setUpBeforeMethod() {
    repositoryCopier = new RepositoryCopier(metaDataService, attributeFactory);
  }

  @Test
  void testRepositoryCopier() {
    assertThrows(NullPointerException.class, () -> new RepositoryCopier(null, null));
  }

  @Test
  void testCopyRepository() {
    Repository<Entity> repository = getMockRepository();
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    @SuppressWarnings("unchecked")
    Stream<Entity> entitiesStream = mock(Stream.class);
    when(query.findAll()).thenReturn(entitiesStream);
    when(repository.query()).thenReturn(query);
    String entityTypeId = "copiedEntityTypeId";
    Package package_ = mock(Package.class);
    when(package_.toString()).thenReturn("Package");
    String entityTypeLabel = "copiedEntityTypeLabel";

    @SuppressWarnings("unchecked")
    Repository<Entity> copiedRepository = mock(Repository.class);
    when(metaDataService.createRepository(any(EntityType.class))).thenReturn(copiedRepository);

    assertEquals(
        copiedRepository,
        repositoryCopier.copyRepository(repository, entityTypeId, package_, entityTypeLabel));

    ArgumentCaptor<EntityType> entityTypeCaptor = ArgumentCaptor.forClass(EntityType.class);
    verify(metaDataService).createRepository(entityTypeCaptor.capture());
    EntityType copiedEntityType = entityTypeCaptor.getValue();
    assertEquals(entityTypeId, copiedEntityType.getId());
    assertEquals(entityTypeLabel, copiedEntityType.getLabel());
    assertEquals(package_, copiedEntityType.getPackage());

    verify(copiedRepository).add(entitiesStream);
  }

  private Repository<Entity> getMockRepository() {
    EntityType entityTypeMeta = createEntityTypeMeta();
    EntityType entityType = mock(EntityType.class);
    when(entityType.getEntityType()).thenReturn(entityTypeMeta);
    when(entityType.getOwnAllAttributes()).thenReturn(emptyList());
    when(entityType.getTags()).thenReturn(emptyList());

    @SuppressWarnings("unchecked")
    Repository<Entity> repository = mock(Repository.class);
    when(repository.getName()).thenReturn("Repository");
    when(repository.getEntityType()).thenReturn(entityType);
    return repository;
  }

  private static EntityType createEntityTypeMeta() {
    EntityType entityTypeMeta = mock(EntityType.class);
    Attribute strAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    Attribute intAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    Attribute boolAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute xrefAttr = when(mock(Attribute.class).getDataType()).thenReturn(XREF).getMock();
    Attribute mrefAttr = when(mock(Attribute.class).getDataType()).thenReturn(MREF).getMock();
    doReturn(strAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.ID);
    doReturn(xrefAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.PACKAGE);
    doReturn(strAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.LABEL);
    doReturn(mrefAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.ATTRIBUTES);
    doReturn(boolAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.IS_ABSTRACT);
    doReturn(mrefAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.TAGS);
    doReturn(intAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.INDEXING_DEPTH);
    return entityTypeMeta;
  }
}
