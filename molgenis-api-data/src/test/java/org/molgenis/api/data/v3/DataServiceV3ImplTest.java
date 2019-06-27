package org.molgenis.api.data.v3;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.UnknownRepositoryException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataServiceV3ImplTest extends AbstractMockitoTest {
  @Mock private MetaDataService metaDataService;
  private DataServiceV3Impl dataServiceV3Impl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    dataServiceV3Impl = new DataServiceV3Impl(metaDataService);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFind() {
    String entityTypeId = "MyEntityType";
    String entityId = "MyId";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.EMPTY_SELECTION;

    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getName()).thenReturn("id");
    when(idAttribute.getDataType()).thenReturn(AttributeType.STRING);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getIdAttribute()).thenReturn(idAttribute);
    when(entityType.getAtomicAttributes()).thenReturn(Collections.singletonList(idAttribute));

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);
    Fetch fetch = new Fetch().field("id");

    Entity entity = mock(Entity.class);
    when(repository.findOneById(entityId, fetch)).thenReturn(entity);

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));

    assertEquals(dataServiceV3Impl.find(entityTypeId, entityId, filter, expand), entity);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFindExpand() {
    String entityTypeId = "MyEntityType";
    String entityId = "MyId";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.FULL_SELECTION;

    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getName()).thenReturn("id");
    when(idAttribute.getDataType()).thenReturn(AttributeType.STRING);

    EntityType refEntityType = mock(EntityType.class);

    Attribute refAttribute = mock(Attribute.class);
    when(refAttribute.getName()).thenReturn("refAttr");
    when(refAttribute.getDataType()).thenReturn(AttributeType.XREF);
    when(refAttribute.getRefEntity()).thenReturn(refEntityType);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getIdAttribute()).thenReturn(idAttribute);
    when(entityType.getAtomicAttributes()).thenReturn(asList(idAttribute, refAttribute));

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);
    Fetch fetch = new Fetch().field("id").field("refAttr");

    Entity entity = mock(Entity.class);
    when(repository.findOneById(entityId, fetch)).thenReturn(entity);

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));

    assertEquals(dataServiceV3Impl.find(entityTypeId, entityId, filter, expand), entity);
  }

  @Test(expectedExceptions = UnknownRepositoryException.class)
  public void testFindUnknownRepository() {
    String entityTypeId = "MyEntityType";
    String entityId = "MyId";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.EMPTY_SELECTION;

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.empty());

    dataServiceV3Impl.find(entityTypeId, entityId, filter, expand);
  }

  @Test(expectedExceptions = UnknownEntityException.class)
  public void testFindUnknownEntity() {
    String entityTypeId = "MyEntityType";
    String entityId = "MyId";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.EMPTY_SELECTION;

    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getName()).thenReturn("id");
    when(idAttribute.getDataType()).thenReturn(AttributeType.STRING);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getIdAttribute()).thenReturn(idAttribute);
    when(entityType.getAtomicAttributes()).thenReturn(Collections.singletonList(idAttribute));

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));

    dataServiceV3Impl.find(entityTypeId, entityId, filter, expand);
  }
}
