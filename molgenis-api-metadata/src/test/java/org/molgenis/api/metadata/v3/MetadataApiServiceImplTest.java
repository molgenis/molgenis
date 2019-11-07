package org.molgenis.api.metadata.v3;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.test.AbstractMockitoTest;

class MetadataApiServiceImplTest extends AbstractMockitoTest {

  @Mock private MetaDataService metadataService;
  @Mock private QueryMapper queryMapper;
  @Mock private SortMapper sortMapper;
  @Mock private DataService dataService;
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
  void testDeleteAttribute() {
    String attributeId = "attr1";
    metadataApiServiceImpl.deleteAttribute(attributeId);
    verify(metadataService).deleteAttributeById(attributeId);
  }

  @Test
  void testDeleteAttributes() {
    List<String> attributeIds = asList("attr1", "attr2");
    metadataApiServiceImpl.deleteAttributes(attributeIds);
    verify(metadataService).deleteAttributesById(attributeIds);
  }

  @Test
  void testDeleteEntityType() {
    String entityTypeId = "MyEntityTypeId";
    metadataApiServiceImpl.deleteEntityType(entityTypeId);
    verify(metadataService).deleteEntityType(entityTypeId);
  }

  @Test
  void testDeleteEntityTypes() {
    List<String> entityTypeIds = asList("MyEntityTypeId0", "MyEntityTypeId0");
    metadataApiServiceImpl.deleteEntityTypes(entityTypeIds);
    verify(metadataService).deleteEntityTypes(entityTypeIds);
  }
}
