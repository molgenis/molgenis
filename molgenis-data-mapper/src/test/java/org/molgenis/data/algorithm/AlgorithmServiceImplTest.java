package org.molgenis.data.algorithm;

import static org.testng.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.mapping.model.AttributeMapping;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class AlgorithmServiceImplTest
{
	private AlgorithmService algorithmService;

	@BeforeTest
	public void beforeTest()
	{
		algorithmService = new AlgorithmServiceImpl();
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
		entityMetaData.addAttribute("id").setDataType(MolgenisFieldTypes.INT).setIdAttribute(true);
		entityMetaData.addAttribute("dob").setDataType(MolgenisFieldTypes.DATE);
		Entity source = new MapEntity(entityMetaData);
		source.set("id", 1);
		source.set("dob", new SimpleDateFormat("dd-MM-yyyy").parse("28-08-1973"));

		DefaultAttributeMetaData targetAttributeMetaData = new DefaultAttributeMetaData("age");
		targetAttributeMetaData.setDataType(org.molgenis.MolgenisFieldTypes.INT);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping
				.setAlgorithm("Math.floor((new Date('02/12/2015') - $('dob'))/(365.2425 * 24 * 60 * 60 * 1000))");
		Object result = algorithmService.apply(attributeMapping, source);
		assertEquals(result, 41);
	}
}
