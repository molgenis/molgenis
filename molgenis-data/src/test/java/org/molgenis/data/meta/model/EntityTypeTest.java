package org.molgenis.data.meta.model;

import org.molgenis.data.Entity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.meta.model.EntityTypeMetadata.*;
import static org.testng.Assert.*;

public class EntityTypeTest
{
	private EntityType entityType;
	private AttributeMetaData randomAttribute;
	private AttributeMetaData compoundAttribute;
	private AttributeMetaData attributePart;

	private EntityType nestedEntityType;
	private AttributeMetaData nestedCompoundParent;
	private AttributeMetaData nestedCompoundPart;
	private AttributeMetaData nestedAttributePart;

	@BeforeMethod
	public void beforeMethod()
	{
		// Setup for single level compound
		randomAttribute = when(mock(AttributeMetaData.class).getDataType()).thenReturn(STRING).getMock();
		when(randomAttribute.getName()).thenReturn("randomAttribute");

		attributePart = when(mock(AttributeMetaData.class).getDataType()).thenReturn(STRING).getMock();
		when(attributePart.getName()).thenReturn("attributePart");
		Iterable<AttributeMetaData> attributeParts = newArrayList(attributePart);

		compoundAttribute = when(mock(AttributeMetaData.class).getDataType()).thenReturn(COMPOUND).getMock();
		when(compoundAttribute.getName()).thenReturn("compoundAttribute");
		when(compoundAttribute.getAttributeParts()).thenReturn(attributeParts);

		Iterable<AttributeMetaData> mockedAttributes = newArrayList(compoundAttribute, randomAttribute);

		Entity entity = when(mock(Entity.class).getEntities(ATTRIBUTES, AttributeMetaData.class))
				.thenReturn(mockedAttributes).getMock();
		entityType = new EntityType(entity);

		// Setup for nested compound test
		nestedAttributePart = when(mock(AttributeMetaData.class).getDataType()).thenReturn(STRING).getMock();
		Iterable<AttributeMetaData> nestedCompoundAttributeParts = newArrayList(nestedAttributePart);

		nestedCompoundPart = when(mock(AttributeMetaData.class).getDataType()).thenReturn(COMPOUND).getMock();
		when(nestedCompoundPart.getAttributeParts()).thenReturn(nestedCompoundAttributeParts);
		Iterable<AttributeMetaData> nestedAttributeParts = newArrayList(nestedCompoundPart, attributePart);

		nestedCompoundParent = when(mock(AttributeMetaData.class).getDataType()).thenReturn(COMPOUND).getMock();
		when(nestedCompoundParent.getAttributeParts()).thenReturn(nestedAttributeParts);

		Iterable<AttributeMetaData> mockedNestedAttributes = newArrayList(nestedCompoundParent, randomAttribute);

		Entity nestedEntity = when(mock(Entity.class).getEntities(ATTRIBUTES, AttributeMetaData.class))
				.thenReturn(mockedNestedAttributes).getMock();
		nestedEntityType = new EntityType(nestedEntity);
	}

	@Test
	public void setNameNoSimpleNameNoLabel()
	{
		EntityType entityType = new EntityType(createEntityTypeMeta());
		String name = "name";
		entityType.setName(name);
		assertEquals(entityType.getName(), name);
		assertEquals(entityType.getSimpleName(), name);
		assertEquals(entityType.getLabel(), name);
	}

	@Test
	public void setNameExistingSimpleNameExistingLabel()
	{
		EntityType entityType = new EntityType(createEntityTypeMeta());
		String label = "label";
		String simpleName = "simpleName";
		String name = "name";
		entityType.setLabel(label);
		entityType.setSimpleName(simpleName);
		entityType.setName(name);
		assertEquals(entityType.getName(), name);
		assertEquals(entityType.getSimpleName(), simpleName);
		assertEquals(entityType.getLabel(), label);
	}

	@Test
	public void setSimpleNameNoNameNoLabel()
	{
		EntityType entityType = new EntityType(createEntityTypeMeta());
		String simpleName = "simpleName";
		entityType.setSimpleName(simpleName);
		assertEquals(entityType.getSimpleName(), simpleName);
		assertEquals(entityType.getString(SIMPLE_NAME), simpleName);
		assertEquals(entityType.getName(), simpleName);
		assertEquals(entityType.getString(FULL_NAME), simpleName);
		assertEquals(entityType.getLabel(), simpleName);
		assertEquals(entityType.getString(LABEL), simpleName);
	}

	@Test
	public void setSimpleNameExistingNameExistingLabel()
	{
		EntityType entityType = new EntityType(createEntityTypeMeta());
		String label = "label";
		String simpleName = "simpleName";
		entityType.setName("name");
		entityType.setLabel(label);
		entityType.setSimpleName(simpleName);
		assertEquals(entityType.getSimpleName(), simpleName);
		assertEquals(entityType.getString(SIMPLE_NAME), simpleName);
		assertEquals(entityType.getName(), simpleName);
		assertEquals(entityType.getString(FULL_NAME), simpleName);
		assertEquals(entityType.getLabel(), label);
		assertEquals(entityType.getString(LABEL), label);
	}

	@Test
	public void setLabel()
	{
		EntityType entityType = new EntityType(createEntityTypeMeta());
		String label = "label";
		entityType.setLabel(label);
		assertEquals(entityType.getLabel(), label);
		assertEquals(entityType.getString(LABEL), label);
	}

	@Test
	public void setLabelNull()
	{
		EntityType entityType = new EntityType(createEntityTypeMeta());
		String simpleName = "simpleName";
		entityType.setSimpleName(simpleName);
		entityType.setLabel(null);
		assertEquals(entityType.getLabel(), simpleName);
		assertEquals(entityType.getString(LABEL), simpleName);
	}

	@Test
	public void getCompoundOrderedAttributesCorrectOrderTest()
	{
		LinkedHashSet<AttributeMetaData> expectedAttributes = newLinkedHashSet();
		expectedAttributes.add(attributePart);
		expectedAttributes.add(compoundAttribute);
		expectedAttributes.add(randomAttribute);

		LinkedHashSet<AttributeMetaData> actualAttributes = entityType.getCompoundOrderedAttributes();
		assertEquals(actualAttributes, expectedAttributes);
	}

	@Test
	public void getCompoundOrderedAttributesIncorrectOrderTest()
	{
		LinkedHashSet<AttributeMetaData> expectedAttributes = newLinkedHashSet();
		expectedAttributes.add(compoundAttribute);
		expectedAttributes.add(randomAttribute);
		expectedAttributes.add(attributePart);

		LinkedHashSet<AttributeMetaData> actualAttributes = entityType.getCompoundOrderedAttributes();

		Iterator<AttributeMetaData> expectedAttributesIterator = expectedAttributes.iterator();
		Iterator<AttributeMetaData> actualAttributesIterator = actualAttributes.iterator();
		while (expectedAttributesIterator.hasNext() && actualAttributesIterator.hasNext())
		{
			assertNotEquals(actualAttributesIterator.next(), expectedAttributesIterator.next());
		}
	}

	@Test
	public void getCompoundOrderedAttributesWithNestedCompoundsTest()
	{
		LinkedHashSet<AttributeMetaData> expectedAttributes = newLinkedHashSet();
		expectedAttributes.add(nestedAttributePart);
		expectedAttributes.add(nestedCompoundPart);
		expectedAttributes.add(attributePart);
		expectedAttributes.add(nestedCompoundParent);
		expectedAttributes.add(randomAttribute);

		LinkedHashSet<AttributeMetaData> actualAttributes = nestedEntityType.getCompoundOrderedAttributes();
		assertEquals(actualAttributes, expectedAttributes);
	}

	@Test
	public void newInstanceShallowCopy()
	{
		EntityType entityTypeMeta = createEntityTypeMeta();

		Package package_ = mock(Package.class);
		when(package_.getName()).thenReturn("myPackage");

		EntityType extendsEntityType = mock(EntityType.class);

		AttributeMetaData attrId = mock(AttributeMetaData.class);
		AttributeMetaData attrLabel = mock(AttributeMetaData.class);
		AttributeMetaData attrCompound = mock(AttributeMetaData.class);

		Tag tag0 = mock(Tag.class);
		Tag tag1 = mock(Tag.class);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getEntityType()).thenReturn(entityTypeMeta);
		when(entityType.getSimpleName()).thenReturn("myEntity");
		when(entityType.getName()).thenReturn("myPackage_myEntity");
		when(entityType.getPackage()).thenReturn(package_);
		when(entityType.getLabel()).thenReturn("label");
		when(entityType.getDescription()).thenReturn("description");

		when(entityType.getOwnAttributes()).thenReturn(asList(attrId, attrLabel, attrCompound));
		when(entityType.getOwnIdAttribute()).thenReturn(attrId);
		when(entityType.getOwnLabelAttribute()).thenReturn(attrLabel);
		when(entityType.getOwnLookupAttributes()).thenReturn(asList(attrId, attrLabel));
		when(entityType.isAbstract()).thenReturn(false);
		when(entityType.getExtends()).thenReturn(extendsEntityType);
		when(entityType.getTags()).thenReturn(asList(tag0, tag1));
		when(entityType.getBackend()).thenReturn("backend");

		EntityType entityTypeCopy = EntityType
				.newInstance(entityType, EntityType.AttributeCopyMode.SHALLOW_COPY_ATTRS);
		assertSame(entityTypeCopy.getEntityType(), entityTypeMeta);
		assertEquals(entityTypeCopy.getSimpleName(), "myEntity");
		assertEquals(entityTypeCopy.getName(), "myPackage_myEntity");
		assertSame(entityTypeCopy.getPackage(), package_);
		assertEquals(entityTypeCopy.getLabel(), "label");
		assertEquals(entityTypeCopy.getDescription(), "description");

		List<AttributeMetaData> ownAttrsCopy = newArrayList(entityType.getOwnAttributes());
		assertEquals(ownAttrsCopy.size(), 3);
		assertSame(ownAttrsCopy.get(0), attrId);
		assertSame(ownAttrsCopy.get(1), attrLabel);
		assertSame(ownAttrsCopy.get(2), attrCompound);
		assertSame(entityTypeCopy.getIdAttribute(), attrId);
		assertSame(entityTypeCopy.getLabelAttribute(), attrLabel);

		List<AttributeMetaData> ownLookAttrsCopy = newArrayList(entityTypeCopy.getOwnLookupAttributes());
		assertEquals(ownLookAttrsCopy.size(), 2);
		assertSame(ownLookAttrsCopy.get(0), attrId);
		assertSame(ownLookAttrsCopy.get(1), attrLabel);

		assertEquals(entityTypeCopy.isAbstract(), false);
		assertSame(entityTypeCopy.getExtends(), extendsEntityType);

		List<Tag> tagsCopy = newArrayList(entityTypeCopy.getTags());
		assertEquals(tagsCopy.size(), 2);
		assertSame(tagsCopy.get(0), tag0);
		assertSame(tagsCopy.get(1), tag1);
		assertEquals(entityTypeCopy.getBackend(), "backend");
	}

	private static EntityType createEntityTypeMeta()
	{
		EntityType entityTypeMeta = mock(EntityType.class);
		AttributeMetaData strAttr = when(mock(AttributeMetaData.class).getDataType()).thenReturn(STRING).getMock();
		AttributeMetaData boolAttr = when(mock(AttributeMetaData.class).getDataType()).thenReturn(BOOL).getMock();
		AttributeMetaData xrefAttr = when(mock(AttributeMetaData.class).getDataType()).thenReturn(XREF).getMock();
		AttributeMetaData mrefAttr = when(mock(AttributeMetaData.class).getDataType()).thenReturn(MREF).getMock();
		when(entityTypeMeta.getAttribute(EntityTypeMetadata.FULL_NAME)).thenReturn(strAttr);
		when(entityTypeMeta.getAttribute(EntityTypeMetadata.SIMPLE_NAME)).thenReturn(strAttr);
		when(entityTypeMeta.getAttribute(EntityTypeMetadata.PACKAGE)).thenReturn(xrefAttr);
		when(entityTypeMeta.getAttribute(LABEL)).thenReturn(strAttr);
		when(entityTypeMeta.getAttribute(EntityTypeMetadata.DESCRIPTION)).thenReturn(strAttr);
		when(entityTypeMeta.getAttribute(EntityTypeMetadata.ATTRIBUTES)).thenReturn(mrefAttr);
		when(entityTypeMeta.getAttribute(EntityTypeMetadata.ID_ATTRIBUTE)).thenReturn(xrefAttr);
		when(entityTypeMeta.getAttribute(EntityTypeMetadata.LABEL_ATTRIBUTE)).thenReturn(xrefAttr);
		when(entityTypeMeta.getAttribute(EntityTypeMetadata.LOOKUP_ATTRIBUTES)).thenReturn(mrefAttr);
		when(entityTypeMeta.getAttribute(EntityTypeMetadata.ABSTRACT)).thenReturn(boolAttr);
		when(entityTypeMeta.getAttribute(EntityTypeMetadata.EXTENDS)).thenReturn(xrefAttr);
		when(entityTypeMeta.getAttribute(EntityTypeMetadata.TAGS)).thenReturn(mrefAttr);
		when(entityTypeMeta.getAttribute(EntityTypeMetadata.BACKEND)).thenReturn(strAttr);
		return entityTypeMeta;
	}
}
