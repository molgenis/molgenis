package org.molgenis.genomebrowser.service;

import org.mockito.Mock;
import org.molgenis.core.ui.util.GsonConfig;
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
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.molgenis.genomebrowser.meta.GenomeBrowserAttributesMetadata.*;
import static org.testng.Assert.assertEquals;

@WebAppConfiguration
@ContextConfiguration(classes = GsonConfig.class)
public class GenomeBrowserServiceTest extends AbstractMockitoTestNGSpringContextTests
{
	@Mock
	DataService dataService;
	@Mock
	UserPermissionEvaluator userPermissionEvaluator;

	@BeforeMethod
	public void beforeMethode()
	{
		reset(dataService);
	}

	@Test
	public void testGetReferenceTracks()
	{
		when(userPermissionEvaluator.hasPermission(any(), eq(EntityTypePermission.READ))).thenReturn(true);
		EntityType entity = mock(EntityType.class);
		Entity attrsEntity = mock(Entity.class);
		GenomeBrowserAttributes genomeBrowserAttributes = new GenomeBrowserAttributes(attrsEntity);

		GenomeBrowserTrack reference = GenomeBrowserTrack.create("ref_id", "label", "ref_label", entity,
				GenomeBrowserSettings.TrackType.VARIANT, null, GenomeBrowserSettings.MolgenisReferenceMode.ALL,
				genomeBrowserAttributes, null, null, null, null);

		EntityType molgenisEntity = mock(EntityType.class);
		GenomeBrowserTrack track = GenomeBrowserTrack.create("id", "label", "entityLabel", molgenisEntity,
				GenomeBrowserSettings.TrackType.VARIANT, Collections.singletonList(reference),
				GenomeBrowserSettings.MolgenisReferenceMode.CONFIGURED, genomeBrowserAttributes, "alert(\"test\")",
				"attr 1:attr1,reference attribute:REF,position on genome:POS", null, null);
		GenomeBrowserService genomeBrowserService = new GenomeBrowserService(dataService, userPermissionEvaluator);
		Map<String, GenomeBrowserTrack> result = genomeBrowserService.getReferenceTracks(track);

		assertEquals(result.size(), 1);
		assertEquals(result.get("ref_id"), reference);
	}

	@Test
	public void testGetReferenceTracksAll()
	{
		when(userPermissionEvaluator.hasPermission(any(), eq(EntityTypePermission.READ))).thenReturn(true);
		EntityType entity = mock(EntityType.class);
		Entity attrsEntity = mock(Entity.class);
		GenomeBrowserAttributes genomeBrowserAttributes = new GenomeBrowserAttributes(attrsEntity);

		GenomeBrowserTrack reference = GenomeBrowserTrack.create("ref_id", "label", "ref_label", entity,
				GenomeBrowserSettings.TrackType.VARIANT, null, GenomeBrowserSettings.MolgenisReferenceMode.NONE,
				genomeBrowserAttributes, null, null, null, null);

		EntityType molgenisEntity = mock(EntityType.class);
		GenomeBrowserTrack track = GenomeBrowserTrack.create("id", "label", "entityLabel", molgenisEntity,
				GenomeBrowserSettings.TrackType.VARIANT, Collections.singletonList(reference),
				GenomeBrowserSettings.MolgenisReferenceMode.ALL, genomeBrowserAttributes, "alert(\"test\")",
				"attr 1:attr1,reference attribute:REF,position on genome:POS", null, null);

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
		when(dataService.findAll(GenomeBrowserAttributesMetadata.GENOMEBROWSERATTRIBUTES,
				new QueryImpl<GenomeBrowserAttributes>().eq(GenomeBrowserAttributesMetadata.DEFAULT, true),
				GenomeBrowserAttributes.class)).thenReturn(
				Stream.of(refGenomeBrowserAttributes1, refGenomeBrowserAttributes2));

		EntityType type1 = mock(EntityType.class);
		EntityType type2 = mock(EntityType.class);
		EntityType type3 = mock(EntityType.class);

		Attribute labelAttr = mock(Attribute.class);
		when(labelAttr.getName()).thenReturn("refLabel");

		List<EntityType> types = new ArrayList<>();
		types.add(type1);
		when(type1.getIdValue()).thenReturn("type1");
		when(type1.getLabel()).thenReturn("label1");
		when(type1.getAttributeNames()).thenReturn(Arrays.asList("POS", "CHROM", "AAP", "NOOT", "MIES"));
		when(type1.getLabelAttribute()).thenReturn(labelAttr);
		types.add(type2);
		when(type2.getIdValue()).thenReturn("type2");
		when(type2.getLabel()).thenReturn("label2");
		when(type2.getAttributeNames()).thenReturn(Arrays.asList("pos", "chr", "ref", "alt", "aap"));
		when(type2.getLabelAttribute()).thenReturn(labelAttr);
		types.add(type3);
		when(type3.getIdValue()).thenReturn("type3");
		when(type3.getAttributeNames()).thenReturn(Arrays.asList("pos", "chr", "ref", "alternative", "monkey"));
		MetaDataService metaDataService = mock(MetaDataService.class);
		when(dataService.getMeta()).thenReturn(metaDataService);
		when(metaDataService.getEntityTypes()).thenReturn(types.stream());
		GenomeBrowserService genomeBrowserService = new GenomeBrowserService(dataService, userPermissionEvaluator);
		Map<String, GenomeBrowserTrack> result = genomeBrowserService.getReferenceTracks(track);
		assertEquals(result.size(), 2);
		assertEquals(result.get("type2"),
				GenomeBrowserTrack.create("type2", "label2", "refLabel", type2, GenomeBrowserSettings.TrackType.VARIANT,
						Collections.emptyList(), GenomeBrowserSettings.MolgenisReferenceMode.ALL,
						refGenomeBrowserAttributes1, null, null, null, null));
		assertEquals(result.get("type1"),
				GenomeBrowserTrack.create("type1", "label1", "refLabel", type1, GenomeBrowserSettings.TrackType.VARIANT,
						Collections.emptyList(), GenomeBrowserSettings.MolgenisReferenceMode.ALL,
						refGenomeBrowserAttributes2, null, null, null, null));
	}

	@Test
	public void testGetReferenceTracksNone()
	{
		when(userPermissionEvaluator.hasPermission(any(), eq(EntityTypePermission.READ))).thenReturn(true);
		EntityType entity = mock(EntityType.class);
		Entity attrsEntity = mock(Entity.class);
		GenomeBrowserAttributes genomeBrowserAttributes = new GenomeBrowserAttributes(attrsEntity);

		GenomeBrowserTrack reference = GenomeBrowserTrack.create("ref_id", "label", "ref_label", entity,
				GenomeBrowserSettings.TrackType.VARIANT, null, GenomeBrowserSettings.MolgenisReferenceMode.NONE,
				genomeBrowserAttributes, null, null, null, null);

		EntityType molgenisEntity = mock(EntityType.class);
		GenomeBrowserTrack track = GenomeBrowserTrack.create("id", "label", "entityLabel", molgenisEntity,
				GenomeBrowserSettings.TrackType.VARIANT, Collections.singletonList(reference),
				GenomeBrowserSettings.MolgenisReferenceMode.NONE, genomeBrowserAttributes, "alert(\"test\")",
				"attr 1:attr1,reference attribute:REF,position on genome:POS", null, null);
		GenomeBrowserService genomeBrowserService = new GenomeBrowserService(dataService, userPermissionEvaluator);
		Map<String, GenomeBrowserTrack> result = genomeBrowserService.getReferenceTracks(track);

		assertEquals(result.size(), 0);
		verify(dataService, never()).getMeta();
	}

	@Test
	public void testGetReferenceTracksNoPermission()
	{
		when(userPermissionEvaluator.hasPermission(any(), eq(EntityTypePermission.READ))).thenReturn(false);
		EntityType entity = mock(EntityType.class);
		Entity attrsEntity = mock(Entity.class);
		GenomeBrowserAttributes genomeBrowserAttributes = new GenomeBrowserAttributes(attrsEntity);

		GenomeBrowserTrack reference = GenomeBrowserTrack.create("ref_id", "label", "ref_label", entity,
				GenomeBrowserSettings.TrackType.VARIANT, null, GenomeBrowserSettings.MolgenisReferenceMode.NONE,
				genomeBrowserAttributes, null, null, null, null);

		EntityType molgenisEntity = mock(EntityType.class);
		GenomeBrowserTrack track = GenomeBrowserTrack.create("id", "label", "entityLabel", molgenisEntity,
				GenomeBrowserSettings.TrackType.VARIANT, Collections.singletonList(reference),
				GenomeBrowserSettings.MolgenisReferenceMode.ALL, genomeBrowserAttributes, "alert(\"test\")",
				"attr 1:attr1,reference attribute:REF,position on genome:POS", null, null);

		GenomeBrowserService genomeBrowserService = new GenomeBrowserService(dataService, userPermissionEvaluator);
		Map<String, GenomeBrowserTrack> result = genomeBrowserService.getReferenceTracks(track);
		assertEquals(result.size(), 0);
		verify(dataService, never()).getMeta();
	}
}