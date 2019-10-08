package org.molgenis.genomebrowser.service;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.genomebrowser.GenomeBrowserTrack.create;
import static org.molgenis.genomebrowser.meta.GenomeBrowserAttributesMetadata.ALT;
import static org.molgenis.genomebrowser.meta.GenomeBrowserAttributesMetadata.CHROM;
import static org.molgenis.genomebrowser.meta.GenomeBrowserAttributesMetadata.POS;
import static org.molgenis.genomebrowser.meta.GenomeBrowserAttributesMetadata.REF;
import static org.molgenis.genomebrowser.meta.GenomeBrowserSettings.MolgenisReferenceMode.ALL;
import static org.molgenis.genomebrowser.meta.GenomeBrowserSettings.TrackType.VARIANT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.genomebrowser.GenomeBrowserTrack;
import org.molgenis.genomebrowser.meta.GenomeBrowserAttributes;
import org.molgenis.genomebrowser.meta.GenomeBrowserAttributesMetadata;
import org.molgenis.genomebrowser.meta.GenomeBrowserSettings;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.molgenis.web.converter.GsonConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

@WebAppConfiguration
@ContextConfiguration(classes = GsonConfig.class)
class GenomeBrowserServiceTest extends AbstractMockitoSpringContextTests {
  @Mock DataService dataService;
  @Mock UserPermissionEvaluator userPermissionEvaluator;

  @BeforeEach
  void beforeMethode() {
    reset(dataService);
  }

  @Test
  void testGetReferenceTracks() {
    when(userPermissionEvaluator.hasPermission(any(), eq(EntityTypePermission.READ_DATA)))
        .thenReturn(true);
    EntityType entity = mock(EntityType.class);
    Entity attrsEntity = mock(Entity.class);
    GenomeBrowserAttributes genomeBrowserAttributes = new GenomeBrowserAttributes(attrsEntity);

    GenomeBrowserTrack reference =
        GenomeBrowserTrack.create(
            "ref_id",
            "label",
            "ref_label",
            entity,
            GenomeBrowserSettings.TrackType.VARIANT,
            null,
            GenomeBrowserSettings.MolgenisReferenceMode.ALL,
            genomeBrowserAttributes,
            null,
            null,
            null,
            null,
            null);

    EntityType molgenisEntity = mock(EntityType.class);
    GenomeBrowserTrack track =
        GenomeBrowserTrack.create(
            "id",
            "label",
            "entityLabel",
            molgenisEntity,
            GenomeBrowserSettings.TrackType.VARIANT,
            Collections.singletonList(reference),
            GenomeBrowserSettings.MolgenisReferenceMode.CONFIGURED,
            genomeBrowserAttributes,
            "alert(\"test\")",
            "attr 1:attr1,reference attribute:REF,position on genome:POS",
            null,
            null,
            null);
    GenomeBrowserService genomeBrowserService =
        new GenomeBrowserService(dataService, userPermissionEvaluator);
    Map<String, GenomeBrowserTrack> result = genomeBrowserService.getReferenceTracks(track);

    assertEquals(1, result.size());
    assertEquals(reference, result.get("ref_id"));
  }

  @Test
  void testGetReferenceTracksAll() {
    when(userPermissionEvaluator.hasPermission(any(), eq(EntityTypePermission.READ_DATA)))
        .thenReturn(true);
    EntityType entity = mock(EntityType.class);
    Entity attrsEntity = mock(Entity.class);
    GenomeBrowserAttributes genomeBrowserAttributes = new GenomeBrowserAttributes(attrsEntity);

    GenomeBrowserTrack reference =
        GenomeBrowserTrack.create(
            "ref_id",
            "label",
            "ref_label",
            entity,
            GenomeBrowserSettings.TrackType.VARIANT,
            null,
            GenomeBrowserSettings.MolgenisReferenceMode.NONE,
            genomeBrowserAttributes,
            null,
            null,
            null,
            null,
            null);

    EntityType molgenisEntity = mock(EntityType.class);
    GenomeBrowserTrack track =
        GenomeBrowserTrack.create(
            "id",
            "label",
            "entityLabel",
            molgenisEntity,
            GenomeBrowserSettings.TrackType.VARIANT,
            Collections.singletonList(reference),
            GenomeBrowserSettings.MolgenisReferenceMode.ALL,
            genomeBrowserAttributes,
            "alert(\"test\")",
            "attr 1:attr1,reference attribute:REF,position on genome:POS",
            null,
            null,
            null);

    Entity attrsEntity1 = mock(Entity.class);
    GenomeBrowserAttributes refGenomeBrowserAttributes1 = new GenomeBrowserAttributes(attrsEntity1);

    doReturn("pos").when(attrsEntity1).getString(POS);
    doReturn("chr").when(attrsEntity1).getString(CHROM);
    doReturn("ref").when(attrsEntity1).getString(REF);
    doReturn("alt").when(attrsEntity1).getString(ALT);

    Entity attrsEntity2 = mock(Entity.class);
    GenomeBrowserAttributes refGenomeBrowserAttributes2 = new GenomeBrowserAttributes(attrsEntity2);

    doReturn("POS").when(attrsEntity2).getString(POS);
    doReturn("CHROM").when(attrsEntity2).getString(CHROM);
    when(dataService.findAll(
            GenomeBrowserAttributesMetadata.GENOMEBROWSERATTRIBUTES,
            new QueryImpl<GenomeBrowserAttributes>()
                .eq(GenomeBrowserAttributesMetadata.DEFAULT, true),
            GenomeBrowserAttributes.class))
        .thenReturn(Stream.of(refGenomeBrowserAttributes1, refGenomeBrowserAttributes2));

    EntityType type1 = mock(EntityType.class);
    EntityType type2 = mock(EntityType.class);
    EntityType type3 = mock(EntityType.class);

    Attribute labelAttr = mock(Attribute.class);
    when(labelAttr.getName()).thenReturn("refLabel");

    List<EntityType> types = new ArrayList<>();
    types.add(type1);
    when(type1.getIdValue()).thenReturn("type1");
    when(type1.getLabel()).thenReturn("label1");
    when(type1.getAttributeNames())
        .thenReturn(Arrays.asList("POS", "CHROM", "AAP", "NOOT", "MIES"));
    when(type1.getLabelAttribute()).thenReturn(labelAttr);
    types.add(type2);
    when(type2.getIdValue()).thenReturn("type2");
    when(type2.getLabel()).thenReturn("label2");
    when(type2.getAttributeNames()).thenReturn(Arrays.asList("pos", "chr", "ref", "alt", "aap"));
    when(type2.getLabelAttribute()).thenReturn(labelAttr);
    types.add(type3);
    when(type3.getIdValue()).thenReturn("type3");
    when(type3.getAttributeNames())
        .thenReturn(Arrays.asList("pos", "chr", "ref", "alternative", "monkey"));
    MetaDataService metaDataService = mock(MetaDataService.class);
    when(dataService.getMeta()).thenReturn(metaDataService);
    when(metaDataService.getEntityTypes()).thenReturn(types.stream());
    GenomeBrowserService genomeBrowserService =
        new GenomeBrowserService(dataService, userPermissionEvaluator);
    Map<String, GenomeBrowserTrack> result = genomeBrowserService.getReferenceTracks(track);
    assertEquals(2, result.size());
    assertEquals(
        create(
            "type2",
            "label2",
            "refLabel",
            type2,
            VARIANT,
            emptyList(),
            ALL,
            refGenomeBrowserAttributes1,
            null,
            null,
            null,
            null,
            null),
        result.get("type2"));
    assertEquals(
        create(
            "type1",
            "label1",
            "refLabel",
            type1,
            VARIANT,
            emptyList(),
            ALL,
            refGenomeBrowserAttributes2,
            null,
            null,
            null,
            null,
            null),
        result.get("type1"));
  }

  @Test
  void testGetReferenceTracksNone() {
    when(userPermissionEvaluator.hasPermission(any(), eq(EntityTypePermission.READ_DATA)))
        .thenReturn(true);
    EntityType entity = mock(EntityType.class);
    Entity attrsEntity = mock(Entity.class);
    GenomeBrowserAttributes genomeBrowserAttributes = new GenomeBrowserAttributes(attrsEntity);

    GenomeBrowserTrack reference =
        GenomeBrowserTrack.create(
            "ref_id",
            "label",
            "ref_label",
            entity,
            GenomeBrowserSettings.TrackType.VARIANT,
            null,
            GenomeBrowserSettings.MolgenisReferenceMode.NONE,
            genomeBrowserAttributes,
            null,
            null,
            null,
            null,
            null);

    EntityType molgenisEntity = mock(EntityType.class);
    GenomeBrowserTrack track =
        GenomeBrowserTrack.create(
            "id",
            "label",
            "entityLabel",
            molgenisEntity,
            GenomeBrowserSettings.TrackType.VARIANT,
            Collections.singletonList(reference),
            GenomeBrowserSettings.MolgenisReferenceMode.NONE,
            genomeBrowserAttributes,
            "alert(\"test\")",
            "attr 1:attr1,reference attribute:REF,position on genome:POS",
            null,
            null,
            null);
    GenomeBrowserService genomeBrowserService =
        new GenomeBrowserService(dataService, userPermissionEvaluator);
    Map<String, GenomeBrowserTrack> result = genomeBrowserService.getReferenceTracks(track);

    assertEquals(0, result.size());
    verify(dataService, never()).getMeta();
  }

  @Test
  void testGetReferenceTracksNoPermission() {
    when(userPermissionEvaluator.hasPermission(any(), eq(EntityTypePermission.READ_DATA)))
        .thenReturn(false);
    EntityType entity = mock(EntityType.class);
    Entity attrsEntity = mock(Entity.class);
    GenomeBrowserAttributes genomeBrowserAttributes = new GenomeBrowserAttributes(attrsEntity);

    GenomeBrowserTrack reference =
        GenomeBrowserTrack.create(
            "ref_id",
            "label",
            "ref_label",
            entity,
            GenomeBrowserSettings.TrackType.VARIANT,
            null,
            GenomeBrowserSettings.MolgenisReferenceMode.NONE,
            genomeBrowserAttributes,
            null,
            null,
            null,
            null,
            null);

    EntityType molgenisEntity = mock(EntityType.class);
    GenomeBrowserTrack track =
        GenomeBrowserTrack.create(
            "id",
            "label",
            "entityLabel",
            molgenisEntity,
            GenomeBrowserSettings.TrackType.VARIANT,
            Collections.singletonList(reference),
            GenomeBrowserSettings.MolgenisReferenceMode.ALL,
            genomeBrowserAttributes,
            "alert(\"test\")",
            "attr 1:attr1,reference attribute:REF,position on genome:POS",
            null,
            null,
            null);

    GenomeBrowserService genomeBrowserService =
        new GenomeBrowserService(dataService, userPermissionEvaluator);
    Map<String, GenomeBrowserTrack> result = genomeBrowserService.getReferenceTracks(track);
    assertEquals(0, result.size());
    verify(dataService, never()).getMeta();
  }
}
