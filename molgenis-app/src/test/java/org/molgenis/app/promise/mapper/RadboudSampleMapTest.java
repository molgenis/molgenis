package org.molgenis.app.promise.mapper;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.molgenis.app.promise.mapper.RadboudMapper.XML_ID;
import static org.molgenis.app.promise.mapper.RadboudMapper.XML_IDAA;
import static org.molgenis.app.promise.mapper.RadboudSampleMap.*;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.*;
import static org.testng.Assert.assertEquals;

public class RadboudSampleMapTest
{
	private RadboudSampleMap radboudSampleMap;
	private DataService dataService;
	private ArgumentCaptor<Stream> streamCaptor = ArgumentCaptor.forClass(Stream.class);

	@BeforeMethod
	@SuppressWarnings("unchecked")
	public void beforeMethod()
	{
		dataService = mock(DataService.class);
		radboudSampleMap = new RadboudSampleMap(dataService);

		Stream<Entity> resultStream = mock(Stream.class);
		List<Entity> resultEntities = mock(List.class);
		when(resultStream.collect(toList())).thenReturn(resultEntities);
		when(dataService.findAll(any(String.class), any(Stream.class))).thenReturn(resultStream);

		Entity sample1 = mock(Entity.class);
		when(sample1.getString(XML_ID)).thenReturn("9000");
		when(sample1.getString(XML_IDAA)).thenReturn("100");
		when(sample1.getString(XML_DEELBIOBANKS)).thenReturn("100");
		when(sample1.getString(XML_GENDER)).thenReturn("2");
		when(sample1.getString(XML_BIRTHDATE)).thenReturn("1960-01-01T00:00:00+02:00");
		when(sample1.getString(XML_INCLUSIE)).thenReturn("2000-01-01T00:00:00+02:00");
		when(sample1.getString(XML_BLOED)).thenReturn("2");
		when(sample1.getString(XML_BLOEDPLASMA)).thenReturn("2");
		when(sample1.getString(XML_BLOEDSERUM)).thenReturn("2");
		when(sample1.getString(XML_DNA)).thenReturn("1");
		when(sample1.getString(XML_RNA)).thenReturn("2");
		when(sample1.getString(XML_GWASOMNI)).thenReturn("1");
		when(sample1.getString(XML_GASTROINTMUC)).thenReturn("2");
		when(sample1.getString(XML_URINE)).thenReturn("2");
		when(sample1.getString(XML_LIQUOR)).thenReturn("2");
		when(sample1.getString(XML_FECES)).thenReturn("2");
		when(sample1.getString(XML_CELLBEENMERG)).thenReturn("2");
		when(sample1.getString(XML_DNABEENMERG)).thenReturn("2");
		when(sample1.getString(XML_RNABEENMERG)).thenReturn("2");
		when(sample1.getString(XML_SPEEKSEL)).thenReturn("2");
		when(sample1.getString(XML_MONONUCLBLOED)).thenReturn("2");
		when(sample1.getString(XML_MONONUCMERG)).thenReturn("2");
		when(sample1.getString(XML_GRANULOCYTMERG)).thenReturn("2");
		when(sample1.getString(XML_MONOCYTMERG)).thenReturn("2");
		when(sample1.getString(XML_MICROBIOOM)).thenReturn("2");

		Entity sample2 = mock(Entity.class);
		when(sample2.getString(XML_ID)).thenReturn("9000");
		when(sample2.getString(XML_IDAA)).thenReturn("100");
		when(sample2.getString(XML_DEELBIOBANKS)).thenReturn("100");
		when(sample2.getString(XML_GENDER)).thenReturn("1");
		when(sample2.getString(XML_BIRTHDATE)).thenReturn("2010-01-01T00:00:00+02:00");
		when(sample2.getString(XML_INCLUSIE)).thenReturn("2011-01-01T00:00:00+02:00");
		when(sample2.getString(XML_BLOED)).thenReturn("2");
		when(sample2.getString(XML_BLOEDPLASMA)).thenReturn("2");
		when(sample2.getString(XML_BLOEDSERUM)).thenReturn("2");
		when(sample2.getString(XML_DNA)).thenReturn("1");
		when(sample2.getString(XML_RNA)).thenReturn("2");
		when(sample2.getString(XML_WEEFSELSOORT)).thenReturn("2");
		when(sample2.getString(XML_GASTROINTMUC)).thenReturn("2");
		when(sample2.getString(XML_URINE)).thenReturn("2");
		when(sample2.getString(XML_LIQUOR)).thenReturn("2");
		when(sample2.getString(XML_FECES)).thenReturn("2");
		when(sample2.getString(XML_CELLBEENMERG)).thenReturn("2");
		when(sample2.getString(XML_DNABEENMERG)).thenReturn("2");
		when(sample2.getString(XML_RNABEENMERG)).thenReturn("1");
		when(sample2.getString(XML_SPEEKSEL)).thenReturn("2");
		when(sample2.getString(XML_MONONUCLBLOED)).thenReturn("2");
		when(sample2.getString(XML_MONONUCMERG)).thenReturn("2");
		when(sample2.getString(XML_GRANULOCYTMERG)).thenReturn("2");
		when(sample2.getString(XML_MONOCYTMERG)).thenReturn("1");
		when(sample2.getString(XML_MICROBIOOM)).thenReturn("2");

		Entity sample3 = mock(Entity.class);
		when(sample3.getString(XML_ID)).thenReturn("9000");
		when(sample3.getString(XML_IDAA)).thenReturn("8");
		when(sample3.getString(XML_DEELBIOBANKS)).thenReturn("0");
		when(sample3.getString(XML_GENDER)).thenReturn("3");
		when(sample3.getString(XML_BIRTHDATE)).thenReturn("2050-01-01T00:00:00+02:00");
		when(sample3.getString(XML_INCLUSIE)).thenReturn("2268-01-01T00:00:00+02:00"); // will fail age sanity check
		when(sample3.getString(XML_BLOED)).thenReturn("2");
		when(sample3.getString(XML_BLOEDPLASMA)).thenReturn("2");
		when(sample3.getString(XML_BLOEDSERUM)).thenReturn("2");
		when(sample3.getString(XML_DNA)).thenReturn("2");
		when(sample3.getString(XML_RNA)).thenReturn("2");
		when(sample3.getString(XML_GASTROINTMUC)).thenReturn("2");
		when(sample3.getString(XML_URINE)).thenReturn("1");
		when(sample3.getString(XML_LIQUOR)).thenReturn("2");
		when(sample3.getString(XML_FECES)).thenReturn("1");
		when(sample3.getString(XML_CELLBEENMERG)).thenReturn("2");
		when(sample3.getString(XML_DNABEENMERG)).thenReturn("2");
		when(sample3.getString(XML_RNABEENMERG)).thenReturn("2");
		when(sample3.getString(XML_SPEEKSEL)).thenReturn("1");
		when(sample3.getString(XML_EXOOMCHIP)).thenReturn("1");
		when(sample3.getString(XML_MONONUCLBLOED)).thenReturn("2");
		when(sample3.getString(XML_MONONUCMERG)).thenReturn("2");
		when(sample3.getString(XML_GRANULOCYTMERG)).thenReturn("2");
		when(sample3.getString(XML_MONOCYTMERG)).thenReturn("2");
		when(sample3.getString(XML_MICROBIOOM)).thenReturn("2");

		Entity sample4 = mock(Entity.class);
		when(sample4.getString(XML_ID)).thenReturn("1");
		when(sample4.getString(XML_IDAA)).thenReturn("1");

		radboudSampleMap.addSample(sample1);
		radboudSampleMap.addSample(sample2);
		radboudSampleMap.addSample(sample3);
		radboudSampleMap.addSample(sample4);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testGetDataCategories() throws Exception
	{
		Entity biobank = mock(Entity.class);
		when(biobank.getString(any(String.class))).thenReturn("2");

		when(biobank.getString(XML_ID)).thenReturn("9000");
		when(biobank.getString(XML_IDAA)).thenReturn("100");
		when(biobank.getString(XML_BEELDEN)).thenReturn("1");
		radboudSampleMap.getDataCategories(biobank);
		verify(dataService, atLeastOnce()).findAll(eq(REF_DATA_CATEGORY_TYPES), streamCaptor.capture());
		assertEquals(streamCaptor.getValue().collect(toSet()), newHashSet("BIOLOGICAL_SAMPLES", "IMAGING_DATA"));

		when(biobank.getString(XML_ID)).thenReturn("9000");
		when(biobank.getString(XML_IDAA)).thenReturn("8");
		when(biobank.getString(XML_BEELDEN)).thenReturn("2");
		when(biobank.getString(XML_BEHANDEL)).thenReturn("1");
		radboudSampleMap.getDataCategories(biobank);
		verify(dataService, atLeastOnce()).findAll(eq(REF_DATA_CATEGORY_TYPES), streamCaptor.capture());
		assertEquals(streamCaptor.getValue().collect(toSet()), newHashSet("MEDICAL_RECORDS"));

		when(biobank.getString(XML_ID)).thenReturn("1");
		when(biobank.getString(XML_IDAA)).thenReturn("1");
		when(biobank.getString(XML_BEHANDEL)).thenReturn("2");
		radboudSampleMap.getDataCategories(biobank);
		verify(dataService, atLeastOnce()).findAll(eq(REF_DATA_CATEGORY_TYPES), streamCaptor.capture());
		assertEquals(streamCaptor.getValue().collect(toSet()), newHashSet("NAV"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testGetMaterials() throws Exception
	{
		radboudSampleMap.getMaterials("9000_100");
		verify(dataService, atLeastOnce()).findAll(eq(REF_MATERIAL_TYPES), streamCaptor.capture());
		assertEquals(streamCaptor.getValue().collect(toSet()), newHashSet("DNA", "TISSUE_FROZEN", "OTHER", "MICRO_RNA"));

		radboudSampleMap.getMaterials("9000_8");
		verify(dataService, atLeastOnce()).findAll(eq(REF_MATERIAL_TYPES), streamCaptor.capture());
		assertEquals(streamCaptor.getValue().collect(toSet()), newHashSet("SALIVA", "URINE", "FECES"));

		radboudSampleMap.getMaterials("1_1");
		verify(dataService, atLeastOnce()).findAll(eq(REF_MATERIAL_TYPES), streamCaptor.capture());
		assertEquals(streamCaptor.getValue().collect(toSet()), newHashSet("NAV"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testGetOmics() throws Exception
	{
		radboudSampleMap.getOmics("9000_100");
		verify(dataService, atLeastOnce()).findAll(eq(REF_EXP_DATA_TYPES), streamCaptor.capture());
		assertEquals(streamCaptor.getValue().collect(toSet()), newHashSet("GENOMICS"));

		radboudSampleMap.getOmics("9000_8");
		verify(dataService, atLeastOnce()).findAll(eq(REF_EXP_DATA_TYPES), streamCaptor.capture());
		assertEquals(streamCaptor.getValue().collect(toSet()), newHashSet("GENOMICS"));

		radboudSampleMap.getOmics("1_1");
		verify(dataService, atLeastOnce()).findAll(eq(REF_EXP_DATA_TYPES), streamCaptor.capture());
		assertEquals(streamCaptor.getValue().collect(toSet()), newHashSet("NAV"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testGetSex() throws Exception
	{
		radboudSampleMap.getSex("9000_100");
		verify(dataService, atLeastOnce()).findAll(eq(REF_GENDER_TYPES), streamCaptor.capture());
		assertEquals(streamCaptor.getValue().collect(toSet()), newHashSet("MALE", "FEMALE"));

		radboudSampleMap.getSex("9000_8");
		verify(dataService, atLeastOnce()).findAll(eq(REF_GENDER_TYPES), streamCaptor.capture());
		assertEquals(streamCaptor.getValue().collect(toSet()), newHashSet("UNKNOWN"));

		radboudSampleMap.getSex("1_1");
		verify(dataService, atLeastOnce()).findAll(eq(REF_GENDER_TYPES), streamCaptor.capture());
		assertEquals(streamCaptor.getValue().collect(toSet()), newHashSet("NAV"));
	}

	@Test
	public void testGetAgeMin() throws Exception
	{
		assertEquals(radboudSampleMap.getAgeMin("9000_100"), Integer.valueOf(1));
		assertEquals(radboudSampleMap.getAgeMin("9000_8"), null);
		assertEquals(radboudSampleMap.getAgeMin("1_1"), null);
	}

	@Test
	public void testGetAgeMax() throws Exception
	{
		assertEquals(radboudSampleMap.getAgeMax("9000_100"), Integer.valueOf(40));
		assertEquals(radboudSampleMap.getAgeMax("9000_8"), null);
		assertEquals(radboudSampleMap.getAgeMax("1_1"), null);
	}

	@Test
	public void testGetSize() throws Exception
	{
		assertEquals(radboudSampleMap.getSize("9000_100"), 2);
		assertEquals(radboudSampleMap.getSize("9000_8"), 1);
		assertEquals(radboudSampleMap.getSize("1_1"), 1);
	}

}