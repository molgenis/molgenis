package org.molgenis.data.mapper.algorithm;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.DATE;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class AlgorithmServiceImplTest
{
	private AlgorithmService algorithmService;
	private DataService dataService;

	@BeforeTest
	public void beforeTest()
	{
		dataService = mock(DataService.class);
		algorithmService = new AlgorithmServiceImpl(dataService);
	}

	@Test
	public void testGetSourceAttributeNames()
	{
		assertEquals(algorithmService.getSourceAttributeNames("$('id')"), Collections.singletonList("id"));
	}

	@Test
	public void testGetSourceAttributeNamesNoQuotes()
	{
		assertEquals(algorithmService.getSourceAttributeNames("$(id)"), Collections.singletonList("id"));
	}

	@Test
	public void testGetAgeScript() throws ParseException
	{
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("LL");
		entityMetaData.addAttribute("id").setDataType(INT).setIdAttribute(true);
		entityMetaData.addAttribute("dob").setDataType(DATE);
		Entity source = new MapEntity(entityMetaData);
		source.set("id", 1);
		source.set("dob", new SimpleDateFormat("dd-MM-yyyy").parse("28-08-1973"));

		DefaultAttributeMetaData targetAttributeMetaData = new DefaultAttributeMetaData("age");
		targetAttributeMetaData.setDataType(INT);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping
				.setAlgorithm("Math.floor((new Date('02/12/2015') - $('dob').value())/(365.2425 * 24 * 60 * 60 * 1000))");
		Object result = algorithmService.apply(attributeMapping, source, entityMetaData);
		assertEquals(result, 41);
	}

	@Test
	public void testDate() throws ParseException
	{
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("LL");
		entityMetaData.addAttribute("id").setDataType(MolgenisFieldTypes.INT).setIdAttribute(true);
		entityMetaData.addAttribute("dob").setDataType(MolgenisFieldTypes.DATE);
		Entity source = new MapEntity(entityMetaData);
		source.set("id", 1);
		source.set("dob", new SimpleDateFormat("dd-MM-yyyy").parse("13-05-2015"));

		DefaultAttributeMetaData targetAttributeMetaData = new DefaultAttributeMetaData("bob");
		targetAttributeMetaData.setDataType(org.molgenis.MolgenisFieldTypes.DATE);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping.setAlgorithm("$('dob').value()");
		Object result = algorithmService.apply(attributeMapping, source, entityMetaData);
		assertEquals(result.toString(), "Wed May 13 00:00:00 CEST 2015");
	}

	@Test
	public void testGetXrefScript() throws ParseException
	{
		// xref entities
		DefaultEntityMetaData entityMetaDataXref = new DefaultEntityMetaData("xrefEntity1");
		entityMetaDataXref.addAttribute("id").setDataType(INT).setIdAttribute(true);
		entityMetaDataXref.addAttribute("field1").setDataType(STRING);
		Entity xref1a = new MapEntity(entityMetaDataXref);
		xref1a.set("id", "1");
		xref1a.set("field1", "Test");

		DefaultEntityMetaData entityMetaDataXref2 = new DefaultEntityMetaData("xrefEntity2");
		entityMetaDataXref2.addAttribute("id").setDataType(INT).setIdAttribute(true);
		entityMetaDataXref2.addAttribute("field1").setDataType(STRING);
		Entity xref2a = new MapEntity(entityMetaDataXref2);
		xref2a.set("id", "2");
		xref2a.set("field2", "Test");

		// source Entity
		DefaultEntityMetaData entityMetaDataSource = new DefaultEntityMetaData("Source");
		entityMetaDataSource.addAttribute("id").setDataType(INT).setIdAttribute(true);
		entityMetaDataSource.addAttribute("xref").setDataType(XREF);
		Entity source = new MapEntity(entityMetaDataSource);
		source.set("id", "1");
		source.set("xref", xref2a);

		DefaultAttributeMetaData targetAttributeMetaData = new DefaultAttributeMetaData("field1");
		targetAttributeMetaData.setDataType(XREF);
		targetAttributeMetaData.setRefEntity(entityMetaDataXref);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping.setAlgorithm("$('xref').map({'1':'2', '2':'1'}).value();");
		when(dataService.findOne("xrefEntity1", "1")).thenReturn(xref1a);
		Entity result = (Entity) algorithmService.apply(attributeMapping, source, entityMetaDataSource);
		assertEquals(result.get("field1"), xref2a.get("field2"));
	}

	@Test
	public void testApplyMref() throws ParseException
	{
		String refEntityName = "refEntity";
		String refEntityIdAttrName = "id";
		String refEntityLabelAttrName = "label";

		String refEntityId0 = "id0";
		String refEntityId1 = "id1";

		String sourceEntityName = "source";
		String sourceEntityAttrName = "mref-source";
		String targetEntityAttrName = "mref-target";

		// ref entities
		DefaultEntityMetaData refEntityMeta = new DefaultEntityMetaData(refEntityName);
		refEntityMeta.addAttribute(refEntityIdAttrName).setDataType(STRING).setIdAttribute(true);
		refEntityMeta.addAttribute(refEntityLabelAttrName).setDataType(STRING).setLabelAttribute(true);

		Entity refEntity0 = new MapEntity(refEntityMeta);
		refEntity0.set(refEntityIdAttrName, refEntityId0);
		refEntity0.set(refEntityLabelAttrName, "label0");

		Entity refEntity1 = new MapEntity(refEntityMeta);
		refEntity1.set(refEntityIdAttrName, refEntityId1);
		refEntity1.set(refEntityLabelAttrName, "label1");

		// mapping
		DefaultAttributeMetaData targetAttributeMetaData = new DefaultAttributeMetaData(targetEntityAttrName);
		targetAttributeMetaData.setDataType(MREF).setNillable(false).setRefEntity(refEntityMeta);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping.setAlgorithm("$('" + sourceEntityAttrName + "').value()");
		when(dataService.findAll(refEntityName, Arrays.asList(refEntityId0, refEntityId1))).thenReturn(
				Arrays.asList(refEntity0, refEntity1));

		// source Entity
		DefaultEntityMetaData entityMetaDataSource = new DefaultEntityMetaData(sourceEntityName);
		entityMetaDataSource.addAttribute(refEntityIdAttrName).setDataType(INT).setIdAttribute(true).setAuto(true);
		entityMetaDataSource.addAttribute(sourceEntityAttrName).setDataType(MREF).setNillable(false)
				.setRefEntity(refEntityMeta);
		Entity source = new MapEntity(entityMetaDataSource);
		source.set(sourceEntityAttrName, Arrays.asList(refEntity0, refEntity1));

		Object result = algorithmService.apply(attributeMapping, source, entityMetaDataSource);
		assertEquals(result, Arrays.asList(refEntity0, refEntity1));
	}

	@Test
	public void testApplyMrefNillable() throws ParseException
	{
		String refEntityName = "refEntity";
		String refEntityIdAttrName = "id";
		String refEntityLabelAttrName = "label";

		String sourceEntityName = "source";
		String sourceEntityAttrName = "mref-source";
		String targetEntityAttrName = "mref-target";

		// ref entities
		DefaultEntityMetaData refEntityMeta = new DefaultEntityMetaData(refEntityName);
		refEntityMeta.addAttribute(refEntityIdAttrName).setDataType(STRING).setIdAttribute(true);
		refEntityMeta.addAttribute(refEntityLabelAttrName).setDataType(STRING).setLabelAttribute(true);

		// mapping
		DefaultAttributeMetaData targetAttributeMetaData = new DefaultAttributeMetaData(targetEntityAttrName);
		targetAttributeMetaData.setDataType(MREF).setNillable(true).setRefEntity(refEntityMeta);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping.setAlgorithm("$('" + sourceEntityAttrName + "').value()");

		// source Entity
		DefaultEntityMetaData entityMetaDataSource = new DefaultEntityMetaData(sourceEntityName);
		entityMetaDataSource.addAttribute(refEntityIdAttrName).setDataType(INT).setIdAttribute(true).setAuto(true);
		entityMetaDataSource.addAttribute(sourceEntityAttrName).setDataType(MREF).setNillable(true)
				.setRefEntity(refEntityMeta);
		Entity source = new MapEntity(entityMetaDataSource);
		source.set(sourceEntityAttrName, null);

		Object result = algorithmService.apply(attributeMapping, source, entityMetaDataSource);
		assertNull(result);
	}
}
