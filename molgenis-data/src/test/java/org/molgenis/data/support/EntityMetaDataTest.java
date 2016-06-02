package org.molgenis.data.support;

public class EntityMetaDataTest
{
	// FIXME
	//	@Test
	//	public void getName()
	//	{
	//		Package package_ = new Package("packageName");
	//		EntityMetaData entityMeta = new EntityMetaDataImpl("entity", package_);
	//		assertEquals(entityMeta.getName(), "packageName" + Package.PACKAGE_SEPARATOR + "entity");
	//
	//		Package parentPackage = new Package("parent");
	//		package_.setParent(parentPackage);
	//		assertEquals(entityMeta.getName(),
	//				"parent" + Package.PACKAGE_SEPARATOR + "packageName" + Package.PACKAGE_SEPARATOR + "entity");
	//
	//		Package otherPackage = new Package("otherPackageName");
	//		entityMeta.setPackage(otherPackage);
	//		assertEquals(entityMeta.getName(), "otherPackageName" + Package.PACKAGE_SEPARATOR + "entity");
	//
	//		entityMeta.setPackage(null);
	//		assertEquals(entityMeta.getName(), "entity");
	//	}
	//
	//	@Test
	//	public void EntityMetaDataStringEntityMetaData()
	//	{
	//		EntityMetaData entityMeta = new EntityMetaDataImpl("entity");
	//		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
	//		when(attr0.getDataType()).thenReturn(STRING);
	//		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
	//		when(attr1.getDataType()).thenReturn(COMPOUND);
	//		AttributeMetaData attr1a = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1a").getMock();
	//		when(attr1a.getDataType()).thenReturn(STRING);
	//		AttributeMetaData attr1b = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1b").getMock();
	//		when(attr1b.getDataType()).thenReturn(STRING);
	//		when(attr1.getAttributeParts()).thenReturn(Arrays.asList(attr1a, attr1b));
	//		entityMeta.addAttribute(attr0);
	//		entityMeta.addAttribute(attr1);
	//
	//		EntityMetaData baseEntityMetaData = new EntityMetaDataImpl("baseEntity");
	//		AttributeMetaData baseAttr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("baseAttr0").getMock();
	//		when(baseAttr0.getDataType()).thenReturn(STRING);
	//		baseEntityMetaData.addAttribute(baseAttr0);
	//
	//		entityMeta.setExtends(baseEntityMetaData);
	//
	//		assertEntityMetaEquals(new EntityMetaDataImpl(entityMeta), entityMeta);
	//	}
	//
	//	@Test
	//	public void testCopyConstructorPreservesIdAttribute()
	//	{
	//		EntityMetaData emd = new EntityMetaDataImpl("name");
	//		emd.addAttribute("id", ROLE_ID);
	//
	//		EntityMetaData emdCopy = new EntityMetaDataImpl(emd);
	//		Assert.assertEquals(emdCopy.getIdAttribute().getName(), "id");
	//	}
	//
	//	@Test
	//	public void testCopyConstructorPreservesName()
	//	{
	//		EntityMetaData emd = new EntityMetaDataImpl("name");
	//		emd.setPackage(new Package("test_package"));
	//
	//		EntityMetaData emdCopy = new EntityMetaDataImpl(emd);
	//		assertEquals(emdCopy.getName(), "test_package_name");
	//		assertEquals(emdCopy.getSimpleName(), "name");
	//	}
	//
	//	// regression test for https://github.com/molgenis/molgenis/issues/3665
	//	@Test
	//	public void testExtendsEntityMetaDataMissingIdAttribute()
	//	{
	//		EntityMetaData extendsEntityMeta = new EntityMetaDataImpl("entity");
	//		extendsEntityMeta.addAttribute("attr");
	//
	//		EntityMetaData entityMeta = new EntityMetaDataImpl("entity");
	//		entityMeta.setExtends(extendsEntityMeta);
	//		AttributeMetaData idAttr = entityMeta.addAttribute("id", ROLE_ID);
	//		assertEquals(entityMeta.getIdAttribute(), idAttr);
	//	}
	//
	//	@Test
	//	public void EntityMetaDataEntityMetaData()
	//	{
	//		EntityMetaData entityMetaData = new EntityMetaDataImpl("entity");
	//		entityMetaData.setAbstract(true);
	//		entityMetaData.setDescription("description");
	//		entityMetaData.setLabel("label");
	//		entityMetaData.addAttribute("labelAttribute", ROLE_LABEL).setDescription("label attribute");
	//		entityMetaData.addAttribute("id", ROLE_ID).setDescription("id attribute");
	//		assertEquals(new EntityMetaDataImpl(entityMetaData), entityMetaData);
	//	}
	//
	//	@Test
	//	public void getAttributes()
	//	{
	//		EntityMetaData entityMeta = new EntityMetaDataImpl("entity");
	//		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
	//		when(attr0.getDataType()).thenReturn(STRING);
	//		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
	//		when(attr1.getDataType()).thenReturn(COMPOUND);
	//		AttributeMetaData attr1a = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1a").getMock();
	//		when(attr1a.getDataType()).thenReturn(STRING);
	//		AttributeMetaData attr1b = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1b").getMock();
	//		when(attr1b.getDataType()).thenReturn(STRING);
	//		when(attr1.getAttributeParts()).thenReturn(Arrays.asList(attr1a, attr1b));
	//		entityMeta.addAttribute(attr0);
	//		entityMeta.addAttribute(attr1);
	//
	//		assertEquals(Lists.newArrayList(entityMeta.getAttributes()), Arrays.asList(attr0, attr1));
	//	}
	//
	//	@Test
	//	public void getAttributesExtends()
	//	{
	//		EntityMetaData entityMeta = new EntityMetaDataImpl("entity");
	//		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
	//		when(attr0.getDataType()).thenReturn(STRING);
	//		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
	//		when(attr1.getDataType()).thenReturn(COMPOUND);
	//		AttributeMetaData attr1a = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1a").getMock();
	//		when(attr1a.getDataType()).thenReturn(STRING);
	//		AttributeMetaData attr1b = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1b").getMock();
	//		when(attr1b.getDataType()).thenReturn(STRING);
	//		when(attr1.getAttributeParts()).thenReturn(Arrays.asList(attr1a, attr1b));
	//		entityMeta.addAttribute(attr0);
	//		entityMeta.addAttribute(attr1);
	//
	//		EntityMetaData baseEntityMetaData = new EntityMetaDataImpl("baseEntity");
	//		AttributeMetaData baseAttr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("baseAttr0").getMock();
	//		when(baseAttr0.getDataType()).thenReturn(STRING);
	//		baseEntityMetaData.addAttribute(baseAttr0);
	//
	//		entityMeta.setExtends(baseEntityMetaData);
	//
	//		assertEquals(Lists.newArrayList(entityMeta.getAttributes()), Arrays.asList(baseAttr0, attr0, attr1));
	//	}
	//
	//	@Test
	//	public void getOwnAttributes()
	//	{
	//		EntityMetaData entityMeta = new EntityMetaDataImpl("entity");
	//		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
	//		when(attr0.getDataType()).thenReturn(STRING);
	//		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
	//		when(attr1.getDataType()).thenReturn(COMPOUND);
	//		AttributeMetaData attr1a = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1a").getMock();
	//		when(attr1a.getDataType()).thenReturn(STRING);
	//		AttributeMetaData attr1b = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1b").getMock();
	//		when(attr1b.getDataType()).thenReturn(STRING);
	//		when(attr1.getAttributeParts()).thenReturn(Arrays.asList(attr1a, attr1b));
	//		entityMeta.addAttribute(attr0);
	//		entityMeta.addAttribute(attr1);
	//
	//		assertEquals(Lists.newArrayList(entityMeta.getOwnAttributes()), Arrays.asList(attr0, attr1));
	//	}
	//
	//	@Test
	//	public void getOwnAttributesExtends()
	//	{
	//		EntityMetaData entityMeta = new EntityMetaDataImpl("entity");
	//		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
	//		when(attr0.getDataType()).thenReturn(STRING);
	//		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
	//		when(attr1.getDataType()).thenReturn(COMPOUND);
	//		AttributeMetaData attr1a = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1a").getMock();
	//		when(attr1a.getDataType()).thenReturn(STRING);
	//		AttributeMetaData attr1b = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1b").getMock();
	//		when(attr1b.getDataType()).thenReturn(STRING);
	//		when(attr1.getAttributeParts()).thenReturn(Arrays.asList(attr1a, attr1b));
	//		entityMeta.addAttribute(attr0);
	//		entityMeta.addAttribute(attr1);
	//
	//		EntityMetaData baseEntityMetaData = new EntityMetaDataImpl("baseEntity");
	//		AttributeMetaData baseAttr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("baseAttr0").getMock();
	//		when(baseAttr0.getDataType()).thenReturn(STRING);
	//		baseEntityMetaData.addAttribute(baseAttr0);
	//
	//		entityMeta.setExtends(baseEntityMetaData);
	//
	//		assertEquals(Lists.newArrayList(entityMeta.getOwnAttributes()), Arrays.asList(attr0, attr1));
	//	}
	//
	//	@Test
	//	public void hasAttributeWithExpressionTrue()
	//	{
	//		EntityMetaData entityMeta = new EntityMetaDataImpl("entity");
	//		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
	//		when(attr.getDataType()).thenReturn(STRING);
	//		AttributeMetaData attrWithExpression = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1")
	//				.getMock();
	//		when(attrWithExpression.getDataType()).thenReturn(STRING);
	//		when(attrWithExpression.getExpression()).thenReturn("expression");
	//		entityMeta.addAttribute(attr);
	//		entityMeta.addAttribute(attrWithExpression);
	//		assertTrue(entityMeta.hasAttributeWithExpression());
	//	}
	//
	//	@Test
	//	public void hasAttributeWithExpressionFalse()
	//	{
	//		EntityMetaData entityMeta = new EntityMetaDataImpl("entity");
	//		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
	//		when(attr.getDataType()).thenReturn(STRING);
	//		entityMeta.addAttribute(attr);
	//		assertFalse(entityMeta.hasAttributeWithExpression());
	//	}
	//
	//	@Test
	//	public void getAtomicAttributes()
	//	{
	//		EntityMetaData entityMeta = new EntityMetaDataImpl("entity");
	//		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
	//		when(attr0.getDataType()).thenReturn(STRING);
	//		entityMeta.addAttribute(attr0);
	//		assertEquals(Lists.newArrayList(entityMeta.getAtomicAttributes()), Arrays.asList(attr0));
	//	}
	//
	//	@Test
	//	public void getAtomicAttributesCompound()
	//	{
	//		EntityMetaData entityMeta = new EntityMetaDataImpl("entity");
	//		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
	//		when(attr0.getDataType()).thenReturn(STRING);
	//		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
	//		when(attr1.getDataType()).thenReturn(COMPOUND);
	//		AttributeMetaData attr1a = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1a").getMock();
	//		when(attr1a.getDataType()).thenReturn(STRING);
	//		AttributeMetaData attr1b = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1b").getMock();
	//		when(attr1b.getDataType()).thenReturn(STRING);
	//		when(attr1.getAttributeParts()).thenReturn(Arrays.asList(attr1a, attr1b));
	//		entityMeta.addAttribute(attr0);
	//		entityMeta.addAttribute(attr1);
	//
	//		assertEquals(Lists.newArrayList(entityMeta.getAtomicAttributes()), Arrays.asList(attr0, attr1a, attr1b));
	//	}
	//
	//	@Test
	//	public void getAtomicAttributesExtends()
	//	{
	//		EntityMetaData entityMeta = new EntityMetaDataImpl("entity");
	//		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
	//		when(attr0.getDataType()).thenReturn(STRING);
	//		entityMeta.addAttribute(attr0);
	//
	//		EntityMetaData baseEntityMetaData = new EntityMetaDataImpl("baseEntity");
	//		AttributeMetaData baseAttr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("baseAttr0").getMock();
	//		when(baseAttr0.getDataType()).thenReturn(STRING);
	//		baseEntityMetaData.addAttribute(baseAttr0);
	//
	//		entityMeta.setExtends(baseEntityMetaData);
	//
	//		assertEquals(Lists.newArrayList(entityMeta.getAtomicAttributes()), Arrays.asList(baseAttr0, attr0));
	//	}
	//
	//	@Test
	//	public void getOwnAtomicAttributes()
	//	{
	//		EntityMetaData entityMeta = new EntityMetaDataImpl("entity");
	//		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
	//		when(attr0.getDataType()).thenReturn(STRING);
	//		entityMeta.addAttribute(attr0);
	//		assertEquals(Lists.newArrayList(entityMeta.getOwnAtomicAttributes()), Arrays.asList(attr0));
	//	}
	//
	//	@Test
	//	public void getOwnAtomicAttributesCompound()
	//	{
	//		EntityMetaData entityMeta = new EntityMetaDataImpl("entity");
	//		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
	//		when(attr0.getDataType()).thenReturn(STRING);
	//		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
	//		when(attr1.getDataType()).thenReturn(COMPOUND);
	//		AttributeMetaData attr1a = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1a").getMock();
	//		when(attr1a.getDataType()).thenReturn(STRING);
	//		AttributeMetaData attr1b = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1b").getMock();
	//		when(attr1b.getDataType()).thenReturn(STRING);
	//		when(attr1.getAttributeParts()).thenReturn(Arrays.asList(attr1a, attr1b));
	//		entityMeta.addAttribute(attr0);
	//		entityMeta.addAttribute(attr1);
	//
	//		assertEquals(Lists.newArrayList(entityMeta.getOwnAtomicAttributes()), Arrays.asList(attr0, attr1a, attr1b));
	//	}
	//
	//	@Test
	//	public void getOwnAtomicAttributesExtends()
	//	{
	//		EntityMetaData entityMeta = new EntityMetaDataImpl("entity");
	//		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
	//		when(attr0.getDataType()).thenReturn(STRING);
	//		entityMeta.addAttribute(attr0);
	//
	//		EntityMetaData baseEntityMetaData = new EntityMetaDataImpl("baseEntity");
	//		AttributeMetaData baseAttr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("baseAttr0").getMock();
	//		when(baseAttr0.getDataType()).thenReturn(STRING);
	//		baseEntityMetaData.addAttribute(baseAttr0);
	//
	//		entityMeta.setExtends(baseEntityMetaData);
	//
	//		assertEquals(Lists.newArrayList(entityMeta.getOwnAtomicAttributes()), Arrays.asList(attr0));
	//	}
	//
	//	@Test
	//	public void addAttributeIdAttr()
	//	{
	//		EntityMetaData entityMeta = new EntityMetaDataImpl("entity");
	//		AttributeMetaData idAttr = entityMeta.addAttribute("idAttr", ROLE_ID);
	//		assertEquals(entityMeta.getIdAttribute(), idAttr);
	//	}
	//
	//	@Test
	//	public void addAttributeLabelAttr()
	//	{
	//		EntityMetaData entityMeta = new EntityMetaDataImpl("entity");
	//		AttributeMetaData labelAttr = entityMeta.addAttribute("labelAttr", ROLE_LABEL);
	//		assertEquals(entityMeta.getLabelAttribute(), labelAttr);
	//	}
	//
	//	@Test
	//	public void addAttributeLookupAttr()
	//	{
	//		EntityMetaData entityMeta = new EntityMetaDataImpl("entity");
	//		String lookupAttrName = "lookupAttr";
	//		AttributeMetaData lookupAttr = entityMeta.addAttribute(lookupAttrName, ROLE_LOOKUP);
	//		assertEquals(entityMeta.getLookupAttribute(lookupAttrName), lookupAttr);
	//	}
	//
	//	private void assertEntityMetaEquals(EntityMetaData actualEntityMeta, EntityMetaData expectedEntityMeta)
	//	{
	//		assertEquals(actualEntityMeta.getSimpleName(), expectedEntityMeta.getSimpleName());
	//		assertEquals(actualEntityMeta.getPackage(), expectedEntityMeta.getPackage());
	//		assertEquals(actualEntityMeta.getLabel(), expectedEntityMeta.getLabel());
	//		assertEquals(actualEntityMeta.isAbstract(), expectedEntityMeta.isAbstract());
	//		assertEquals(actualEntityMeta.getDescription(), expectedEntityMeta.getDescription());
	//		EntityMetaData actualExtends = actualEntityMeta.getExtends();
	//		EntityMetaData expectedExtends = expectedEntityMeta.getExtends();
	//		if (actualExtends != null && expectedExtends != null)
	//		{
	//			assertEntityMetaEquals(actualExtends, expectedExtends);
	//		}
	//		assertEquals(actualEntityMeta.getBackend(), expectedEntityMeta.getBackend());
	//		assertEquals(Lists.newArrayList(actualEntityMeta.getAtomicAttributes()),
	//				Lists.newArrayList(expectedEntityMeta.getAtomicAttributes()));
	//		assertEquals(Lists.newArrayList(actualEntityMeta.getOwnAtomicAttributes()),
	//				Lists.newArrayList(expectedEntityMeta.getOwnAtomicAttributes()));
	//		assertEquals(Lists.newArrayList(actualEntityMeta.getOwnAttributes()),
	//				Lists.newArrayList(expectedEntityMeta.getOwnAttributes()));
	//		assertEquals(actualEntityMeta.getIdAttribute(), expectedEntityMeta.getIdAttribute());
	//		assertEquals(actualEntityMeta.getLabelAttribute(), expectedEntityMeta.getLabelAttribute());
	//	}
}
