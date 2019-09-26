package org.molgenis.oneclickimporter.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.LABEL;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.oneclickimporter.service.impl.OneClickImporterNamingServiceImpl;

class OneClickImporterNamingServiceTest {
  @Mock private DataService dataService;
  private OneClickImporterNamingService oneClickImporterNamingService;

  @BeforeEach
  void beforeClass() {
    initMocks(this);
  }

  @Test
  void testGetLabelWithPostFixWhenNoDuplicate() {
    String label = "label";
    when(dataService.findAll(
            ENTITY_TYPE_META_DATA,
            new QueryImpl<EntityType>().like(LABEL, label),
            EntityType.class))
        .thenReturn(Stream.empty());

    oneClickImporterNamingService = new OneClickImporterNamingServiceImpl(dataService);
    String actual = oneClickImporterNamingService.getLabelWithPostFix(label);
    String expected = "label";

    assertEquals(expected, actual);
  }

  @Test
  void testGetLabelWithPostFixWhenOneDuplicate() {
    EntityType e1 = Mockito.mock(EntityType.class);
    when(e1.getLabel()).thenReturn("label");

    String label = "label";
    when(dataService.findAll(
            ENTITY_TYPE_META_DATA,
            new QueryImpl<EntityType>().like(LABEL, label),
            EntityType.class))
        .thenReturn(Stream.of(e1));

    oneClickImporterNamingService = new OneClickImporterNamingServiceImpl(dataService);
    String actual = oneClickImporterNamingService.getLabelWithPostFix(label);
    String expected = "label (1)";

    assertEquals(expected, actual);
  }

  @Test
  void testGetLabelWithPostFixWhenFiveDuplicate() {
    EntityType e1 = Mockito.mock(EntityType.class);
    when(e1.getLabel()).thenReturn("label");

    EntityType e2 = Mockito.mock(EntityType.class);
    when(e2.getLabel()).thenReturn("label (1)");

    EntityType e3 = Mockito.mock(EntityType.class);
    when(e3.getLabel()).thenReturn("label (2)");

    EntityType e4 = Mockito.mock(EntityType.class);
    when(e4.getLabel()).thenReturn("label (3)");

    EntityType e5 = Mockito.mock(EntityType.class);
    when(e5.getLabel()).thenReturn("label (4)");

    String label = "label";
    when(dataService.findAll(
            ENTITY_TYPE_META_DATA,
            new QueryImpl<EntityType>().like(LABEL, label),
            EntityType.class))
        .thenReturn(Stream.of(e1, e2, e3, e4, e5));

    oneClickImporterNamingService = new OneClickImporterNamingServiceImpl(dataService);
    String actual = oneClickImporterNamingService.getLabelWithPostFix(label);
    String expected = "label (5)";

    assertEquals(expected, actual);
  }

  @Test
  void testGetLabelWithPostFixDuplicateInNoOrder() {
    EntityType e1 = Mockito.mock(EntityType.class);
    when(e1.getLabel()).thenReturn("label");

    EntityType e2 = Mockito.mock(EntityType.class);
    when(e2.getLabel()).thenReturn("label (1)");

    EntityType e3 = Mockito.mock(EntityType.class);
    when(e3.getLabel()).thenReturn("label (3)");

    String label = "label";
    when(dataService.findAll(
            ENTITY_TYPE_META_DATA,
            new QueryImpl<EntityType>().like(LABEL, label),
            EntityType.class))
        .thenReturn(Stream.of(e1, e2, e3));

    oneClickImporterNamingService = new OneClickImporterNamingServiceImpl(dataService);
    String actual = oneClickImporterNamingService.getLabelWithPostFix(label);
    String expected = "label (2)";

    assertEquals(expected, actual);
  }

  @Test
  void testCreateValidIdFromFileName() {
    oneClickImporterNamingService = new OneClickImporterNamingServiceImpl(dataService);
    assertEquals(
        "test_file1", oneClickImporterNamingService.createValidIdFromFileName("test-file1.xlsx"));
    assertEquals(
        "test_f_#", oneClickImporterNamingService.createValidIdFromFileName("test-f@#.xlsx"));
    assertEquals(
        "test_##_", oneClickImporterNamingService.createValidIdFromFileName("test!##%.xlsx"));
  }

  @Test
  void testAsValidAttributeName() {
    oneClickImporterNamingService = new OneClickImporterNamingServiceImpl(dataService);
    assertEquals("name#_3", oneClickImporterNamingService.asValidColumnName("name#!3"));
  }
}
