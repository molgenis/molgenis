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
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.*;
import static org.testng.Assert.*;

public class EntityMetaDataTest
{
	private EntityMetaData entityMetaData;
	private AttributeMetaData randomAttribute;
	private AttributeMetaData compoundAttribute;
	private AttributeMetaData attributePart;

	private EntityMetaData nestedEntityMetaData;
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
		entityMetaData = new EntityMetaData(entity);

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
		nestedEntityMetaData = new EntityMetaData(nestedEntity);
	}

	@Test
	public void setSimpleNameNoNameNoLabel()
	{
		EntityMetaData entityMeta = new EntityMetaData(createEntityMetaMeta());
		String simpleName = "simpleName";
		entityMeta.setSimpleName(simpleName);
		assertEquals(entityMeta.getSimpleName(), simpleName);
		assertEquals(entityMeta.getString(SIMPLE_NAME), simpleName);
		assertEquals(entityMeta.getName(), simpleName);
		assertEquals(entityMeta.getString(FULL_NAME), simpleName);
		assertEquals(entityMeta.getLabel(), simpleName);
		assertEquals(entityMeta.getString(LABEL), simpleName);
	}

	@Test
	public void setSimpleNameExistingNameExistingLabel()
	{
		EntityMetaData entityMeta = new EntityMetaData(createEntityMetaMeta());
		String label = "label";
		String simpleName = "simpleName";
		entityMeta.setName("name");
		entityMeta.setLabel(label);
		entityMeta.setSimpleName(simpleName);
		assertEquals(entityMeta.getSimpleName(), simpleName);
		assertEquals(entityMeta.getString(SIMPLE_NAME), simpleName);
		assertEquals(entityMeta.getName(), simpleName);
		assertEquals(entityMeta.getString(FULL_NAME), simpleName);
		assertEquals(entityMeta.getLabel(), label);
		assertEquals(entityMeta.getString(LABEL), label);
	}

	@Test
	public void setLabel()
	{
		EntityMetaData entityMeta = new EntityMetaData(createEntityMetaMeta());
		String label = "label";
		entityMeta.setLabel(label);
		assertEquals(entityMeta.getLabel(), label);
		assertEquals(entityMeta.getString(LABEL), label);
	}

	@Test
	public void setLabelNull()
	{
		EntityMetaData entityMeta = new EntityMetaData(createEntityMetaMeta());
		String simpleName = "simpleName";
		entityMeta.setSimpleName(simpleName);
		entityMeta.setLabel(null);
		assertEquals(entityMeta.getLabel(), simpleName);
		assertEquals(entityMeta.getString(LABEL), simpleName);
	}

	@Test
	public void getCompoundOrderedAttributesCorrectOrderTest()
	{
		LinkedHashSet<AttributeMetaData> expectedAttributes = newLinkedHashSet();
		expectedAttributes.add(attributePart);
		expectedAttributes.add(compoundAttribute);
		expectedAttributes.add(randomAttribute);

		LinkedHashSet<AttributeMetaData> actualAttributes = entityMetaData.getCompoundOrderedAttributes();
		assertEquals(actualAttributes, expectedAttributes);
	}

	@Test
	public void getCompoundOrderedAttributesIncorrectOrderTest()
	{
		LinkedHashSet<AttributeMetaData> expectedAttributes = newLinkedHashSet();
		expectedAttributes.add(compoundAttribute);
		expectedAttributes.add(randomAttribute);
		expectedAttributes.add(attributePart);

		LinkedHashSet<AttributeMetaData> actualAttributes = entityMetaData.getCompoundOrderedAttributes();

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

		LinkedHashSet<AttributeMetaData> actualAttributes = nestedEntityMetaData.getCompoundOrderedAttributes();
		assertEquals(actualAttributes, expectedAttributes);
	}

	@Test
	public void newInstanceShallowCopy()
	{
		EntityMetaData entityMetaMeta = createEntityMetaMeta();

		Package package_ = mock(Package.class);
		when(package_.getName()).thenReturn("myPackage");

		EntityMetaData extendsEntityMeta = mock(EntityMetaData.class);

		AttributeMetaData attrId = mock(AttributeMetaData.class);
		AttributeMetaData attrLabel = mock(AttributeMetaData.class);
		AttributeMetaData attrCompound = mock(AttributeMetaData.class);

		Tag tag0 = mock(Tag.class);
		Tag tag1 = mock(Tag.class);

		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(entityMeta.getEntityMetaData()).thenReturn(entityMetaMeta);
		when(entityMeta.getSimpleName()).thenReturn("myEntity");
		when(entityMeta.getName()).thenReturn("myPackage_myEntity");
		when(entityMeta.getPackage()).thenReturn(package_);
		when(entityMeta.getLabel()).thenReturn("label");
		when(entityMeta.getDescription()).thenReturn("description");

		when(entityMeta.getOwnAttributes()).thenReturn(asList(attrId, attrLabel, attrCompound));
		when(entityMeta.getOwnIdAttribute()).thenReturn(attrId);
		when(entityMeta.getOwnLabelAttribute()).thenReturn(attrLabel);
		when(entityMeta.getOwnLookupAttributes()).thenReturn(asList(attrId, attrLabel));
		when(entityMeta.isAbstract()).thenReturn(false);
		when(entityMeta.getExtends()).thenReturn(extendsEntityMeta);
		when(entityMeta.getTags()).thenReturn(asList(tag0, tag1));
		when(entityMeta.getBackend()).thenReturn("backend");

		EntityMetaData entityMetaCopy = EntityMetaData
				.newInstance(entityMeta, EntityMetaData.AttributeCopyMode.SHALLOW_COPY_ATTRS);
		assertSame(entityMetaCopy.getEntityMetaData(), entityMetaMeta);
		assertEquals(entityMetaCopy.getSimpleName(), "myEntity");
		assertEquals(entityMetaCopy.getName(), "myPackage_myEntity");
		assertSame(entityMetaCopy.getPackage(), package_);
		assertEquals(entityMetaCopy.getLabel(), "label");
		assertEquals(entityMetaCopy.getDescription(), "description");

		List<AttributeMetaData> ownAttrsCopy = newArrayList(entityMeta.getOwnAttributes());
		assertEquals(ownAttrsCopy.size(), 3);
		assertSame(ownAttrsCopy.get(0), attrId);
		assertSame(ownAttrsCopy.get(1), attrLabel);
		assertSame(ownAttrsCopy.get(2), attrCompound);
		assertSame(entityMetaCopy.getIdAttribute(), attrId);
		assertSame(entityMetaCopy.getLabelAttribute(), attrLabel);

		List<AttributeMetaData> ownLookAttrsCopy = newArrayList(entityMetaCopy.getOwnLookupAttributes());
		assertEquals(ownLookAttrsCopy.size(), 2);
		assertSame(ownLookAttrsCopy.get(0), attrId);
		assertSame(ownLookAttrsCopy.get(1), attrLabel);

		assertEquals(entityMetaCopy.isAbstract(), false);
		assertSame(entityMetaCopy.getExtends(), extendsEntityMeta);

		List<Tag> tagsCopy = newArrayList(entityMetaCopy.getTags());
		assertEquals(tagsCopy.size(), 2);
		assertSame(tagsCopy.get(0), tag0);
		assertSame(tagsCopy.get(1), tag1);
		assertEquals(entityMetaCopy.getBackend(), "backend");
	}

	private static EntityMetaData createEntityMetaMeta()
	{
		EntityMetaData entityMetaMeta = mock(EntityMetaData.class);
		AttributeMetaData strAttr = when(mock(AttributeMetaData.class).getDataType()).thenReturn(STRING).getMock();
		AttributeMetaData boolAttr = when(mock(AttributeMetaData.class).getDataType()).thenReturn(BOOL).getMock();
		AttributeMetaData xrefAttr = when(mock(AttributeMetaData.class).getDataType()).thenReturn(XREF).getMock();
		AttributeMetaData mrefAttr = when(mock(AttributeMetaData.class).getDataType()).thenReturn(MREF).getMock();
		when(entityMetaMeta.getAttribute(EntityMetaDataMetaData.FULL_NAME)).thenReturn(strAttr);
		when(entityMetaMeta.getAttribute(EntityMetaDataMetaData.SIMPLE_NAME)).thenReturn(strAttr);
		when(entityMetaMeta.getAttribute(EntityMetaDataMetaData.PACKAGE)).thenReturn(xrefAttr);
		when(entityMetaMeta.getAttribute(LABEL)).thenReturn(strAttr);
		when(entityMetaMeta.getAttribute(EntityMetaDataMetaData.DESCRIPTION)).thenReturn(strAttr);
		when(entityMetaMeta.getAttribute(EntityMetaDataMetaData.ATTRIBUTES)).thenReturn(mrefAttr);
		when(entityMetaMeta.getAttribute(EntityMetaDataMetaData.ID_ATTRIBUTE)).thenReturn(xrefAttr);
		when(entityMetaMeta.getAttribute(EntityMetaDataMetaData.LABEL_ATTRIBUTE)).thenReturn(xrefAttr);
		when(entityMetaMeta.getAttribute(EntityMetaDataMetaData.LOOKUP_ATTRIBUTES)).thenReturn(mrefAttr);
		when(entityMetaMeta.getAttribute(EntityMetaDataMetaData.ABSTRACT)).thenReturn(boolAttr);
		when(entityMetaMeta.getAttribute(EntityMetaDataMetaData.EXTENDS)).thenReturn(xrefAttr);
		when(entityMetaMeta.getAttribute(EntityMetaDataMetaData.TAGS)).thenReturn(mrefAttr);
		when(entityMetaMeta.getAttribute(EntityMetaDataMetaData.BACKEND)).thenReturn(strAttr);
		return entityMetaMeta;
	}
}
