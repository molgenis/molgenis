package org.molgenis.app.promise.mapper;

import autovalue.shaded.com.google.common.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.molgenis.app.promise.mapper.RadboudDiseaseMap.*;
import static org.molgenis.app.promise.mapper.RadboudMapper.XML_ID;
import static org.molgenis.app.promise.mapper.RadboudMapper.XML_IDAA;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.REF_DISEASE_TYPES;
import static org.testng.Assert.assertEquals;

public class RadboudDiseaseMapTest
{
	private RadboudDiseaseMap radboudDiseaseMap;

	private Entity diseaseType1 = mock(Entity.class);
	private Entity diseaseType2 = mock(Entity.class);
	private Entity diseaseType3 = mock(Entity.class);
	private Entity diseaseTypeNAV = mock(Entity.class);

	@BeforeMethod
	@SuppressWarnings("unchecked")
	public void beforeMethod()
	{
		DataService dataService = mock(DataService.class);
		radboudDiseaseMap = new RadboudDiseaseMap(dataService);

		when(dataService.findOne(REF_DISEASE_TYPES, URN_MIRIAM_ICD_PREFIX + "C81-C96")).thenReturn(diseaseType1);
		when(dataService.findOne(REF_DISEASE_TYPES, URN_MIRIAM_ICD_PREFIX + "E11")).thenReturn(diseaseType2);
		when(dataService.findOne(REF_DISEASE_TYPES, URN_MIRIAM_ICD_PREFIX + "F06.7")).thenReturn(diseaseType3);
		when(dataService.findOne(REF_DISEASE_TYPES, URN_MIRIAM_ICD_PREFIX + "XXX")).thenReturn(null);
		when(dataService.findOne(REF_DISEASE_TYPES, "NAV")).thenReturn(diseaseTypeNAV);

		Entity disease1 = mock(Entity.class);
		when(disease1.getString(XML_ID)).thenReturn("9000");
		when(disease1.getString(XML_IDAA)).thenReturn("4");
		when(disease1.getString(XML_IDAABB)).thenReturn("1");
		when(disease1.getString(XML_CODENAME)).thenReturn("ICD-10");
		when(disease1.getString(XML_CODEVERSION)).thenReturn("2015");
		when(disease1.getString(XML_CODEINDEX)).thenReturn("C81-C96");
		when(disease1.getString(XML_CODEDESCEN)).thenReturn(
				"Malignant neoplasms, stated or presumed to be primary, of lymphoid, haematopoietic and related tissue");

		Entity disease2 = mock(Entity.class);
		when(disease2.getString(XML_ID)).thenReturn("9000");
		when(disease2.getString(XML_IDAA)).thenReturn("4");
		when(disease2.getString(XML_IDAABB)).thenReturn("1");
		when(disease2.getString(XML_CODENAME)).thenReturn("ICD-10");
		when(disease2.getString(XML_CODEVERSION)).thenReturn("2015");
		when(disease2.getString(XML_CODEINDEX)).thenReturn("E11");
		when(disease2.getString(XML_CODEDESCEN)).thenReturn("Type 2 diabetes mellitus");

		Entity disease3 = mock(Entity.class);
		when(disease3.getString(XML_ID)).thenReturn("9000");
		when(disease3.getString(XML_IDAA)).thenReturn("8");
		when(disease3.getString(XML_IDAABB)).thenReturn("1");
		when(disease3.getString(XML_CODENAME)).thenReturn("ICD-10");
		when(disease3.getString(XML_CODEVERSION)).thenReturn("2015");
		when(disease3.getString(XML_CODEINDEX)).thenReturn("F06.7");
		when(disease3.getString(XML_CODEDESCEN)).thenReturn("Mild cognitive disorder");

		Entity disease4 = mock(Entity.class);
		when(disease4.getString(XML_IDAA)).thenReturn("1");
		when(disease4.getString(XML_CODENAME)).thenReturn("XXX");

		radboudDiseaseMap.addDisease(disease1);
		radboudDiseaseMap.addDisease(disease2);
		radboudDiseaseMap.addDisease(disease3);
		radboudDiseaseMap.addDisease(disease4);
	}

	@Test
	public void getMultipleDiseaseTypes()
	{
		assertEquals(radboudDiseaseMap.getDiseaseTypes("4"), Lists.newArrayList(diseaseType1, diseaseType2));
	}

	@Test
	public void getDiseaseTypes()
	{
		assertEquals(radboudDiseaseMap.getDiseaseTypes("8"), Lists.newArrayList(diseaseType3));
	}

	@Test
	public void getDiseaseTypesNAV()
	{
		assertEquals(radboudDiseaseMap.getDiseaseTypes("3"), Lists.newArrayList(diseaseTypeNAV));
		assertEquals(radboudDiseaseMap.getDiseaseTypes("1"), Lists.newArrayList(diseaseTypeNAV));
	}
}