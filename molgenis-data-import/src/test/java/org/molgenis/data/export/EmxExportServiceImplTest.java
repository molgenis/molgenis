package org.molgenis.data.export;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

import java.util.HashSet;
import java.util.Set;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.Test;

public class EmxExportServiceImplTest extends AbstractMockitoTest {

  @Mock DataService dataService;

  @Mock MetaDataService metaDataService;

  @Test
  public void testResolveMetadata() {
    EmxExportServiceImpl service = new EmxExportServiceImpl(dataService);

    EntityType entityType1 = mock(EntityType.class);
    EntityType entityType2 = mock(EntityType.class);
    EntityType entityType3 = mock(EntityType.class);
    doReturn("e1").when(entityType1).getId();
    doReturn("e3").when(entityType3).getId();

    Package pack1 = mock(Package.class);
    Package pack2 = mock(Package.class);
    Package pack3 = mock(Package.class);

    doReturn(newArrayList(pack3)).when(pack1).getChildren();
    doReturn(emptyList()).when(pack2).getChildren();
    doReturn(emptyList()).when(pack3).getChildren();
    doReturn(emptyList()).when(pack1).getEntityTypes();
    doReturn(newArrayList(entityType2)).when(pack2).getEntityTypes();
    doReturn(newArrayList(entityType1)).when(pack3).getEntityTypes();

    Set<Package> packages = new HashSet<>();
    Set<EntityType> entityTypes = new HashSet<>();
    service.resolveMetadata(
        newArrayList(entityType1, entityType3), newArrayList(pack1, pack2), packages, entityTypes);

    Set<Package> expectedPackages = new HashSet<>();
    expectedPackages.addAll(newArrayList(pack1, pack2, pack3));
    Set<EntityType> expectedEntityTypes = new HashSet<>();
    expectedEntityTypes.addAll(newArrayList(entityType1, entityType2, entityType3));

    assertEquals(packages, expectedPackages);
    assertEquals(entityTypes, expectedEntityTypes);
  }
}
