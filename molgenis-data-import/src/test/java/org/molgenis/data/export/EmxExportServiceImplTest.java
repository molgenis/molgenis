package org.molgenis.data.export;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.export.mapper.PackageMapper.PACKAGE_ATTRS;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_PACKAGES;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.excel.xlsx.XlsxWriter;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.i18n.ContextMessageSource;
import org.molgenis.jobs.Progress;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EmxExportServiceImplTest extends AbstractMockitoTest {

  @Mock DataService dataService;

  @Mock ContextMessageSource contextMessageSource;

  @Mock TimeZoneProvider timeZoneProvider;

  private EmxExportServiceImpl service;

  @BeforeMethod
  public void setUp() {
    service = new EmxExportServiceImpl(dataService, contextMessageSource, timeZoneProvider);
  }

  @Test
  public void testResolveMetadata() {
    EntityType entityType1 = mock(EntityType.class);
    EntityType entityType2 = mock(EntityType.class);
    EntityType entityType3 = mock(EntityType.class);

    Package pack1 = mock(Package.class);
    Package pack2 = mock(Package.class);
    Package pack3 = mock(Package.class);

    doReturn("test").when(entityType1).getIdValue();
    doReturn("test").when(entityType2).getIdValue();
    doReturn("test").when(entityType3).getIdValue();
    doReturn("test").when(pack1).getIdValue();
    doReturn("test").when(pack2).getIdValue();
    doReturn("test").when(pack3).getIdValue();

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

    Set<Package> expectedPackages = newHashSet(pack1, pack2, pack3);
    Set<EntityType> expectedEntityTypes = newHashSet(entityType1, entityType2, entityType3);

    assertEquals(packages, expectedPackages);
    assertEquals(entityTypes, expectedEntityTypes);
  }

  @Test
  public void testWritePackageSheet() {
    EntityType entityType1 = mock(EntityType.class);
    EntityType entityType2 = mock(EntityType.class);
    Package pack1 = mock(Package.class);
    doReturn("pack1").when(pack1).get(PackageMetadata.ID);
    Package parentPack = mock(Package.class);
    doReturn("parentPack").when(parentPack).get(PackageMetadata.ID);
    Package parentParentPack = mock(Package.class);
    doReturn("parentParentPack").when(parentParentPack).get(PackageMetadata.ID);
    Package pack2 = mock(Package.class);
    doReturn("pack2").when(pack2).get(PackageMetadata.ID);
    Package entityPack1 = mock(Package.class);
    doReturn("entityPack").when(entityPack1).get(PackageMetadata.ID);
    Package entityParentPack1 = mock(Package.class);
    doReturn("entityParentPack").when(entityParentPack1).get(PackageMetadata.ID);

    when(pack1.getParent()).thenReturn(parentPack);
    when(parentPack.getParent()).thenReturn(parentParentPack);
    when(pack2.getParent()).thenReturn(null);
    when(entityPack1.getParent()).thenReturn(entityParentPack1);
    when(entityType1.getPackage()).thenReturn(entityPack1);
    when(entityType2.getPackage()).thenReturn(null);

    Set<EntityType> entityTypes = newHashSet(entityType1, entityType2);
    Set<Package> packages = newHashSet(pack1, pack2);

    doReturn("Finished").when(contextMessageSource).getMessage("emx_export_metadata_message");

    XlsxWriter writer = mock(XlsxWriter.class);
    Progress progress = mock(Progress.class);

    List<Object> expectedRow1 = newArrayList("pack1", null, null, null, "");
    List<Object> expectedRow2 = newArrayList("parentPack", null, null, null, "");
    List<Object> expectedRow3 = newArrayList("parentParentPack", null, null, null, "");
    List<Object> expectedRow4 = newArrayList("entityPack", null, null, null, "");
    List<Object> expectedRow5 = newArrayList("entityParentPack", null, null, null, "");
    List<Object> expectedRow6 = newArrayList("pack2", null, null, null, "");
    List<List<Object>> expected =
        newArrayList(
            expectedRow4, expectedRow6, expectedRow2, expectedRow1, expectedRow5, expectedRow3);

    service.writePackageSheet(packages, entityTypes, writer, progress);
    verify(writer).createSheet(EMX_PACKAGES, newArrayList(PACKAGE_ATTRS.keySet()));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<List<Object>>> captor = ArgumentCaptor.forClass(Stream.class);
    verify(writer).writeRows(captor.capture(), eq("packages"));
    List<List<Object>> actual = captor.getValue().collect(Collectors.toList());
    assertTrue(actual.containsAll(expected));
    verify(progress).status("Finished");
  }
}
