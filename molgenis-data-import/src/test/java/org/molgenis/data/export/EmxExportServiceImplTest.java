package org.molgenis.data.export;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.export.mapper.PackageMapper.PACKAGE_ATTRS;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_PACKAGES;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

class EmxExportServiceImplTest extends AbstractMockitoTest {

  @Mock DataService dataService;

  @Mock ContextMessageSource contextMessageSource;

  @Mock TimeZoneProvider timeZoneProvider;

  private EmxExportServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new EmxExportServiceImpl(dataService, contextMessageSource, timeZoneProvider);
  }

  @Test
  void testResolveMetadata() {
    String entityType1Id = "e1";
    String entityType2Id = "e2";
    String entityType3Id = "e3";
    EntityType entityType1 =
        when(mock(EntityType.class).getId()).thenReturn(entityType1Id).getMock();
    EntityType entityType2 =
        when(mock(EntityType.class).getId()).thenReturn(entityType2Id).getMock();
    EntityType entityType3 =
        when(mock(EntityType.class).getId()).thenReturn(entityType3Id).getMock();
    String pack1Id = "p1";
    String pack2Id = "p2";
    String pack3Id = "p3";
    Package pack1 = when(mock(Package.class).getId()).thenReturn(pack1Id).getMock();
    Package pack2 = when(mock(Package.class).getId()).thenReturn(pack2Id).getMock();
    Package pack3 = when(mock(Package.class).getId()).thenReturn(pack3Id).getMock();

    doReturn("test").when(entityType1).getIdValue();
    doReturn("test").when(entityType2).getIdValue();
    doReturn("test").when(entityType3).getIdValue();

    doReturn(newArrayList(pack3)).when(pack1).getChildren();
    doReturn(emptyList()).when(pack2).getChildren();
    doReturn(emptyList()).when(pack3).getChildren();
    doReturn(emptyList()).when(pack1).getEntityTypes();
    doReturn(newArrayList(entityType2)).when(pack2).getEntityTypes();
    doReturn(newArrayList(entityType1)).when(pack3).getEntityTypes();

    Map<String, Package> packages = new HashMap<>();
    Map<String, EntityType> entityTypes = new HashMap<>();
    service.resolveMetadata(
        newArrayList(entityType1, entityType3), newArrayList(pack1, pack2), packages, entityTypes);

    Map<String, Package> expectedPackages =
        ImmutableMap.of(pack1Id, pack1, pack2Id, pack2, pack3Id, pack3);
    Map<String, EntityType> expectedEntityTypes =
        ImmutableMap.of(
            entityType1Id, entityType1, entityType2Id, entityType2, entityType3Id, entityType3);

    assertEquals(expectedPackages, packages);
    assertEquals(expectedEntityTypes, entityTypes);
  }

  @Test
  void writeEntityTypesTest() {
    EntityType entityType1 = mock(EntityType.class);
    EntityType entityType2 = mock(EntityType.class);
    EntityType entityType3 = mock(EntityType.class);
    EntityType entityType4 = mock(EntityType.class);
    when(entityType1.isAbstract()).thenReturn(false);
    when(entityType2.isAbstract()).thenReturn(false);
    when(entityType3.isAbstract()).thenReturn(true);
    when(entityType4.isAbstract()).thenReturn(false);
    when(entityType3.getId()).thenReturn("3");

    LinkedList<EntityType> entityTypes = new LinkedList<>();
    entityTypes.add(entityType1);
    entityTypes.add(entityType2);
    entityTypes.add(entityType3);
    entityTypes.add(entityType4);
    XlsxWriter xlsxWriter = mock(XlsxWriter.class);
    service.writeEntityTypes(entityTypes, xlsxWriter);

    ArgumentCaptor<Stream> argumentCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(xlsxWriter).writeRows(argumentCaptor.capture(), eq(EMX_ENTITIES));
    Stream<List> argument = argumentCaptor.getValue();
    List<Object> actual = argument.collect(Collectors.toList()).get(0);
    // Check that the abstract entityType comes first in the stream to the writer
    assertEquals("3", actual.get(0));
  }

  @Test
  void testWritePackageSheet() {
    String entityType1Id = "e1";
    EntityType entityType1 = mock(EntityType.class);
    String entityType2Id = "e2";
    EntityType entityType2 = mock(EntityType.class);
    String pack1Id = "parentParentPack_parentPack_pack1";
    Package pack1 = mock(Package.class);
    doReturn(pack1Id).when(pack1).get(PackageMetadata.ID);
    String parentPackId = "parentParentPack_parentPack";
    Package parentPack = when(mock(Package.class).getId()).thenReturn(parentPackId).getMock();
    doReturn(parentPackId).when(parentPack).get(PackageMetadata.ID);
    doReturn(parentPackId).when(parentPack).getIdValue();
    String parentParentPackId = "parentParentPack";
    Package parentParentPack =
        when(mock(Package.class).getId()).thenReturn(parentParentPackId).getMock();
    doReturn(parentParentPackId).when(parentParentPack).get(PackageMetadata.ID);
    doReturn(parentParentPackId).when(parentParentPack).getIdValue();
    String pack2Id = "pack2";
    Package pack2 = mock(Package.class);
    doReturn(pack2Id).when(pack2).get(PackageMetadata.ID);
    String entityPackId = "entityParentPack_entityPack";
    Package entityPack1 = when(mock(Package.class).getId()).thenReturn(entityPackId).getMock();
    doReturn(entityPackId).when(entityPack1).get(PackageMetadata.ID);
    doReturn(entityPackId).when(entityPack1).getIdValue();
    String entityParentPackId = "entityParentPack";
    Package entityParentPack1 =
        when(mock(Package.class).getId()).thenReturn(entityParentPackId).getMock();
    doReturn(entityParentPackId).when(entityParentPack1).get(PackageMetadata.ID);
    doReturn(entityParentPackId).when(entityParentPack1).getIdValue();

    when(pack1.getParent()).thenReturn(parentPack);
    when(parentPack.getParent()).thenReturn(parentParentPack);
    when(pack2.getParent()).thenReturn(null);
    when(entityPack1.getParent()).thenReturn(entityParentPack1);
    when(entityType1.getPackage()).thenReturn(entityPack1);
    when(entityType2.getPackage()).thenReturn(null);

    Map<String, EntityType> entityTypes = new HashMap<>();
    entityTypes.put(entityType1Id, entityType1);
    entityTypes.put(entityType2Id, entityType2);
    Map<String, Package> packages = new HashMap<>();
    packages.put(pack1Id, pack1);
    packages.put(pack2Id, pack2);

    doReturn("Finished").when(contextMessageSource).getMessage("emx_export_metadata_message");

    XlsxWriter writer = mock(XlsxWriter.class);
    Progress progress = mock(Progress.class);

    List<Object> expectedRow1 =
        newArrayList(
            "parentParentPack_parentPack_pack1", null, null, "parentParentPack_parentPack", "");
    List<Object> expectedRow2 =
        newArrayList("parentParentPack_parentPack", null, null, "parentParentPack", "");
    List<Object> expectedRow3 = newArrayList("parentParentPack", null, null, null, "");
    List<Object> expectedRow4 =
        newArrayList("entityParentPack_entityPack", null, null, "entityParentPack", "");
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
