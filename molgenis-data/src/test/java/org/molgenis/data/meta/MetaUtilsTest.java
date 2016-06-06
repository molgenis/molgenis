package org.molgenis.data.meta;

public class MetaUtilsTest
{
	//	@Test
	//	public void toExistingAttributeMetaData()
	//	{
	//		EntityMetaData entityMetaData = new EntityMetaData("entityMetaData");
	//		AttributeMetaData attributeHeight = new AttributeMetaData("height_0");
	//		AttributeMetaData attributeWeight = new AttributeMetaData("weight_0");
	//		entityMetaData.addAttribute(attributeHeight);
	//		entityMetaData.addAttribute(attributeWeight);
	//
	//		MapEntity entity1 = new MapEntity(ImmutableMap.of(AttributeMetaDataMetaData.NAME, "height_0",
	//				AttributeMetaDataMetaData.LABEL, "height", AttributeMetaDataMetaData.DESCRIPTION,
	//				"this is a height measurement in m!"));
	//		Iterable<Entity> attributeMetaDataEntities = Arrays.<Entity> asList(entity1);
	//
	//		Iterable<AttributeMetaData> actual = MetaUtils.toExistingAttributeMetaData(entityMetaData,
	//				attributeMetaDataEntities);
	//		Iterable<AttributeMetaData> expected = Arrays.<AttributeMetaData> asList(attributeHeight);
	//		assertEquals(actual, expected);
	//	}
	//
	//	@Test(expectedExceptions =
	//	{ MolgenisDataAccessException.class })
	//	public void toExistingAttributeMetaData_MolgenisDataAccessException()
	//	{
	//		EntityMetaData entityMetaData = new EntityMetaData("entityMetaData");
	//		AttributeMetaData attributeHeight = new AttributeMetaData("height_0");
	//		AttributeMetaData attributeWeight = new AttributeMetaData("weight_0");
	//		entityMetaData.addAttribute(attributeHeight);
	//		entityMetaData.addAttribute(attributeWeight);
	//
	//		MapEntity entity1 = new MapEntity(ImmutableMap.of(AttributeMetaDataMetaData.NAME, "height_wrong_name",
	//				AttributeMetaDataMetaData.LABEL, "height", AttributeMetaDataMetaData.DESCRIPTION,
	//				"this is a height measurement in m!"));
	//		Iterable<Entity> attributeMetaDataEntities = Arrays.<Entity> asList(entity1);
	//		MetaUtils.toExistingAttributeMetaData(entityMetaData, attributeMetaDataEntities);
	//	}
}
