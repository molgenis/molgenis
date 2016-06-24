package org.molgenis.data.meta;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

public class MetaUtilsTest
{
	@Test
	public void toExistingAttributeMetaData()
	{
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("entityMetaData");
		AttributeMetaData attributeHeight = new DefaultAttributeMetaData("height_0");
		AttributeMetaData attributeWeight = new DefaultAttributeMetaData("weight_0");
		entityMetaData.addAttributeMetaData(attributeHeight);
		entityMetaData.addAttributeMetaData(attributeWeight);

		MapEntity entity1 = new MapEntity(ImmutableMap.of(AttributeMetaDataMetaData.NAME, "height_0",
				AttributeMetaDataMetaData.LABEL, "height", AttributeMetaDataMetaData.DESCRIPTION,
				"this is a height measurement in m!"));
		Iterable<Entity> attributeMetaDataEntities = Arrays.<Entity> asList(entity1);

		Iterable<AttributeMetaData> actual = MetaUtils.toExistingAttributeMetaData(entityMetaData,
				attributeMetaDataEntities);
		Iterable<AttributeMetaData> expected = Arrays.<AttributeMetaData> asList(attributeHeight);
		assertEquals(actual, expected);
	}

	@Test(expectedExceptions =
	{ MolgenisDataAccessException.class })
	public void toExistingAttributeMetaData_MolgenisDataAccessException()
	{
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("entityMetaData");
		AttributeMetaData attributeHeight = new DefaultAttributeMetaData("height_0");
		AttributeMetaData attributeWeight = new DefaultAttributeMetaData("weight_0");
		entityMetaData.addAttributeMetaData(attributeHeight);
		entityMetaData.addAttributeMetaData(attributeWeight);

		MapEntity entity1 = new MapEntity(ImmutableMap.of(AttributeMetaDataMetaData.NAME, "height_wrong_name",
				AttributeMetaDataMetaData.LABEL, "height", AttributeMetaDataMetaData.DESCRIPTION,
				"this is a height measurement in m!"));
		Iterable<Entity> attributeMetaDataEntities = Arrays.<Entity> asList(entity1);
		MetaUtils.toExistingAttributeMetaData(entityMetaData, attributeMetaDataEntities);
	}
}
