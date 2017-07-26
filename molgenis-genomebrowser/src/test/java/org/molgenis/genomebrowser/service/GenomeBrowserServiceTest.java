package org.molgenis.genomebrowser.service;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.genomebrowser.GenomeBrowserTrack;
import org.molgenis.genomebrowser.meta.GenomeBrowserAttributes;
import org.molgenis.genomebrowser.meta.GenomeBrowserAttributesMetadata;
import org.molgenis.genomebrowser.meta.GenomeBrowserSettings;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.genomebrowser.meta.GenomeBrowserAttributesMetadata.*;
import static org.testng.Assert.assertEquals;

public class GenomeBrowserServiceTest
{
	@Mock
	DataService dataService;

	@InjectMocks
	GenomeBrowserService genomeBrowserService;

	private GenomeBrowserSettings settings;

	@BeforeClass
	public void setUp()
	{
		dataService = mock(DataService.class);
		initMocks(this);
	}

	@BeforeMethod
	public void beforeMethode()
	{
		reset(dataService);
	}

	@Test
	public void testGetReferenceTracks()
	{
		EntityType entity = mock(EntityType.class);
		GenomeBrowserAttributes genomeBrowserAttributes = getGenomeBrowserAttributes("postion", "chrom", "normal",
				"mutant");
		GenomeBrowserTrack reference = GenomeBrowserTrack.create("ref_id", "ref_label", entity,
				GenomeBrowserSettings.TrackType.VARIANT, null, GenomeBrowserSettings.MolgenisReferenceMode.ALL,
				genomeBrowserAttributes, null, null, null, null);

		EntityType molgenisEntity = mock(EntityType.class);
		GenomeBrowserTrack track = GenomeBrowserTrack.create("id", "entityLabel", molgenisEntity,
				GenomeBrowserSettings.TrackType.VARIANT, Collections.singletonList(reference),
				GenomeBrowserSettings.MolgenisReferenceMode.CONFIGURED, genomeBrowserAttributes, "alert(\"test\")",
				"attr 1:attr1,reference attribute:REF,position on genome:POS", null, null);

		Map<String, GenomeBrowserTrack> result = genomeBrowserService.getReferenceTracks(track);

		assertEquals(result.size(), 1);
		assertEquals(result.get("ref_id"), reference);
	}

	@Test
	public void testGetReferenceTracksAll()
	{
		EntityType entity = mock(EntityType.class);
		GenomeBrowserAttributes genomeBrowserAttributes = getGenomeBrowserAttributes("postion", "chrom", "normal",
				"mutant");
		GenomeBrowserTrack reference = GenomeBrowserTrack.create("ref_id", "ref_label", entity,
				GenomeBrowserSettings.TrackType.VARIANT, null, GenomeBrowserSettings.MolgenisReferenceMode.NONE,
				genomeBrowserAttributes, null, null, null, null);

		EntityType molgenisEntity = mock(EntityType.class);
		GenomeBrowserTrack track = GenomeBrowserTrack.create("id", "entityLabel", molgenisEntity,
				GenomeBrowserSettings.TrackType.VARIANT, Collections.singletonList(reference),
				GenomeBrowserSettings.MolgenisReferenceMode.ALL, genomeBrowserAttributes, "alert(\"test\")",
				"attr 1:attr1,reference attribute:REF,position on genome:POS", null, null);

		GenomeBrowserAttributes attrs1 = getGenomeBrowserAttributes("pos", "chr", "ref", "alt");
		GenomeBrowserAttributes attrs2 = getGenomeBrowserAttributes("POS", "CHROM", null, null);
		when(dataService.findAll(GenomeBrowserAttributesMetadata.GENOMEBROWSERATTRIBUTES,
				new QueryImpl<GenomeBrowserAttributes>().eq(GenomeBrowserAttributesMetadata.DEFAULT, true),
				GenomeBrowserAttributes.class)).thenReturn(Stream.of(attrs1, attrs2));

		EntityType type1 = mock(EntityType.class);
		EntityType type2 = mock(EntityType.class);
		EntityType type3 = mock(EntityType.class);

		Attribute labelAttr = mock(Attribute.class);
		when(labelAttr.getName()).thenReturn("refLabel");

		List<EntityType> types = new ArrayList<>();
		types.add(type1);
		when(type1.getIdValue()).thenReturn("type1");
		when(type1.getAttributeNames()).thenReturn(Arrays.asList("POS", "CHROM", "AAP", "NOOT", "MIES"));
		when(type1.getLabelAttribute()).thenReturn(labelAttr);
		types.add(type2);
		when(type2.getIdValue()).thenReturn("type2");
		when(type2.getAttributeNames()).thenReturn(Arrays.asList("pos", "chr", "ref", "alt", "aap"));
		when(type2.getLabelAttribute()).thenReturn(labelAttr);
		types.add(type3);
		when(type3.getIdValue()).thenReturn("type3");
		when(type3.getAttributeNames()).thenReturn(Arrays.asList("pos", "chr", "ref", "alternative", "monkey"));
		when(type3.getLabelAttribute()).thenReturn(labelAttr);
		MetaDataService metaDataService = mock(MetaDataService.class);
		when(dataService.getMeta()).thenReturn(metaDataService);
		when(metaDataService.getEntityTypes()).thenReturn(types.stream());

		Map<String, GenomeBrowserTrack> result = genomeBrowserService.getReferenceTracks(track);
		assertEquals(result.size(), 2);
		assertEquals(result.get("type2"),
				GenomeBrowserTrack.create("type2", "refLabel", type2, GenomeBrowserSettings.TrackType.VARIANT,
						Collections.emptyList(), GenomeBrowserSettings.MolgenisReferenceMode.ALL, attrs1, null, null,
						null, null));
		assertEquals(result.get("type1"),
				GenomeBrowserTrack.create("type1", "refLabel", type1, GenomeBrowserSettings.TrackType.VARIANT,
						Collections.emptyList(), GenomeBrowserSettings.MolgenisReferenceMode.ALL, attrs2, null, null,
						null, null));
	}

	@Test
	public void testGetReferenceTracksNone()
	{
		EntityType entity = mock(EntityType.class);
		GenomeBrowserAttributes genomeBrowserAttributes = getGenomeBrowserAttributes("postion", "chrom", "normal",
				"mutant");
		GenomeBrowserTrack reference = GenomeBrowserTrack.create("ref_id", "ref_label", entity,
				GenomeBrowserSettings.TrackType.VARIANT, null, GenomeBrowserSettings.MolgenisReferenceMode.NONE,
				genomeBrowserAttributes, null, null, null, null);

		EntityType molgenisEntity = mock(EntityType.class);
		GenomeBrowserTrack track = GenomeBrowserTrack.create("id", "entityLabel", molgenisEntity,
				GenomeBrowserSettings.TrackType.VARIANT, Collections.singletonList(reference),
				GenomeBrowserSettings.MolgenisReferenceMode.NONE, genomeBrowserAttributes, "alert(\"test\")",
				"attr 1:attr1,reference attribute:REF,position on genome:POS", null, null);

		Map<String, GenomeBrowserTrack> result = genomeBrowserService.getReferenceTracks(track);

		assertEquals(result.size(), 0);
		verify(dataService, never()).getMeta();
	}

	public static GenomeBrowserAttributes getGenomeBrowserAttributes(String pos, String chrom, String ref, String alt)
	{
		Entity attrsEntity = mock(Entity.class);
		GenomeBrowserAttributes genomeBrowserAttributes = new GenomeBrowserAttributes(attrsEntity);

		when(attrsEntity.getString(POS)).thenReturn(pos);
		when(attrsEntity.getString(CHROM)).thenReturn(chrom);
		when(attrsEntity.getString(REF)).thenReturn(ref);
		when(attrsEntity.getString(ALT)).thenReturn(alt);
		return genomeBrowserAttributes;
	}
}