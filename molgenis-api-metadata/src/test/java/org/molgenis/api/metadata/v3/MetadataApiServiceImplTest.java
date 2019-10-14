package org.molgenis.api.metadata.v3;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.api.model.Query.Operator.IN;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;

import java.util.Collection;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.api.model.Query;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;

class MetadataApiServiceImplTest extends AbstractMockitoTest {
  @Mock private MetaDataService metadataService;
  @Mock private QueryMapper queryMapper;
  @Mock private SortMapper sortMapper;
  @Mock private DataService dataService;
  @Mock private EntityTypeV3Mapper entityTypeV3Mapper;
  private MetadataApiServiceImpl metadataApiServiceImpl;

  @BeforeEach
  void setUpBeforeEach() {
    metadataApiServiceImpl =
        new MetadataApiServiceImpl(metadataService, queryMapper, sortMapper, dataService);
  }

  @Test
  void testMetadataApiServiceImpl() {
    assertThrows(
        NullPointerException.class, () -> new MetadataApiServiceImpl(null, null, null, null));
  }

  @Test
  void testDeleteEntityType() {
    String entityTypeId = "MyEntityTypeId";
    metadataApiServiceImpl.deleteEntityType(entityTypeId);
    verify(metadataService).deleteEntityType(entityTypeId);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDeleteEntityTypes() {
    Query query = Query.create("id", IN, asList("MyEntityTypeId0", "MyEntityTypeId1"));
    org.molgenis.data.Query<EntityType> dataServiceQuery = mock(org.molgenis.data.Query.class);
    EntityType entityType0 = mock(EntityType.class);
    EntityType entityType1 = mock(EntityType.class);
    when(dataServiceQuery.findAll()).thenReturn(Stream.of(entityType0, entityType1));
    Repository<EntityType> entityTypeRepository = mock(Repository.class);
    when(dataService.getRepository(ENTITY_TYPE_META_DATA, EntityType.class))
        .thenReturn(entityTypeRepository);
    when(queryMapper.map(query, entityTypeRepository)).thenReturn(dataServiceQuery);

    metadataApiServiceImpl.deleteEntityTypes(query);
    ArgumentCaptor<Collection<EntityType>> entityTypesCaptor =
        ArgumentCaptor.forClass(Collection.class);
    verify(metadataService).deleteEntityType(entityTypesCaptor.capture());
    assertEquals(asList(entityType0, entityType1), entityTypesCaptor.getValue());
  }
}
