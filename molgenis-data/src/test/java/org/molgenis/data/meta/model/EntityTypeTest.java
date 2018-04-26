package org.molgenis.data.meta.model;

import com.google.common.collect.Lists;
import org.molgenis.data.MolgenisDataException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityTypeMetadata.LABEL;
import static org.testng.Assert.*;

public class EntityTypeTest
{
	@BeforeMethod
	public void beforeMethod()
	{
		// Setup for single level compound
		Attribute randomAttribute = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
		when(randomAttribute.getName()).thenReturn("randomAttribute");

		Attribute attributePart = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
		when(attributePart.getName()).thenReturn("attributePart");
		Iterable<Attribute> attributeParts = newArrayList(attributePart);

		Attribute compoundAttribute = when(mock(Attribute.class).getDataType()).thenReturn(COMPOUND).getMock();
		when(compoundAttribute.getName()).thenReturn("compoundAttribute");
		when(compoundAttribute.getChildren()).thenReturn(attributeParts);

		// Setup for nested compound test
		Attribute nestedAttributePart = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
		Iterable<Attribute> nestedCompoundAttributeParts = newArrayList(nestedAttributePart);

		Attribute nestedCompoundPart = when(mock(Attribute.class).getDataType()).thenReturn(COMPOUND).getMock();
		when(nestedCompoundPart.getChildren()).thenReturn(nestedCompoundAttributeParts);
		Iterable<Attribute> nestedAttributeParts = newArrayList(nestedCompoundPart, attributePart);

		Attribute nestedCompoundParent = when(mock(Attribute.class).getDataType()).thenReturn(COMPOUND).getMock();
		when(nestedCompoundParent.getChildren()).thenReturn(nestedAttributeParts);
	}

	@Test
	public void addSequenceNumberNull()
	{
		Attribute attr1 = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
		Attribute attr2 = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
		Attribute attr3 = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
		Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();

		when(attr1.getSequenceNumber()).thenReturn(0);
		when(attr2.getSequenceNumber()).thenReturn(1);
		when(attr3.getSequenceNumber()).thenReturn(2);
		when(attribute.getSequenceNumber()).thenReturn(null);

		EntityType.addSequenceNumber(attribute, Lists.newArrayList(attr1, attr2, attr3));
		verify(attribute).setSequenceNumber(3);
	}

	@Test
	public void addSequenceNumberNullAndOtherNull()
	{
		Attribute attr1 = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
		Attribute attr2 = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
		Attribute attr3 = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
		Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();

		when(attr1.getSequenceNumber()).thenReturn(null);
		when(attr2.getSequenceNumber()).thenReturn(null);
		when(attr3.getSequenceNumber()).thenReturn(null);
		when(attribute.getSequenceNumber()).thenReturn(null);

		EntityType.addSequenceNumber(attribute, Lists.newArrayList(attr1, attr2, attr3));
		verify(attribute).setSequenceNumber(0);
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
		String simpleName = "label";
		assertNull(entityType.getLabel());
		assertNull(entityType.getString(LABEL));
	}

	@Test
	public void newInstanceShallowCopy()
	{
		EntityType entityTypeMeta = createEntityTypeMeta();

		Package package_ = mock(Package.class);
		when(package_.getId()).thenReturn("myPackage");

		EntityType extendsEntityType = mock(EntityType.class);

		Attribute attrId = when(mock(Attribute.class).getName()).thenReturn("id").getMock();
		when(attrId.isIdAttribute()).thenReturn(true);
		when(attrId.getLookupAttributeIndex()).thenReturn(0);
		Attribute attrLabel = when(mock(Attribute.class).getName()).thenReturn("label").getMock();
		when(attrLabel.isLabelAttribute()).thenReturn(true);
		when(attrLabel.getLookupAttributeIndex()).thenReturn(1);
		Attribute attrCompoundPart = when(mock(Attribute.class).getName()).thenReturn("compoundPart").getMock();
		when(attrCompoundPart.getLookupAttributeIndex()).thenReturn(null);
		Attribute attrCompound = when(mock(Attribute.class).getName()).thenReturn("compound").getMock();
		when(attrCompound.getLookupAttributeIndex()).thenReturn(null);
		when(attrCompound.getChildren()).thenReturn(singletonList(attrCompoundPart));
		Tag tag0 = mock(Tag.class);
		Tag tag1 = mock(Tag.class);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getEntityType()).thenReturn(entityTypeMeta);
		when(entityType.getId()).thenReturn("myPackage_myEntity");
		when(entityType.getPackage()).thenReturn(package_);
		when(entityType.getLabel()).thenReturn("label");
		when(entityType.getDescription()).thenReturn("description");

		when(entityType.getOwnAllAttributes()).thenReturn(asList(attrId, attrLabel, attrCompound, attrCompoundPart));
		when(entityType.getOwnIdAttribute()).thenReturn(attrId);
		when(entityType.getOwnLabelAttribute()).thenReturn(attrLabel);
		when(entityType.getOwnLookupAttributes()).thenReturn(asList(attrId, attrLabel));
		when(entityType.isAbstract()).thenReturn(false);
		when(entityType.getExtends()).thenReturn(extendsEntityType);
		when(entityType.getTags()).thenReturn(asList(tag0, tag1));
		when(entityType.getBackend()).thenReturn("backend");

		EntityType entityTypeCopy = EntityType.newInstance(entityType);
		assertSame(entityTypeCopy.getEntityType(), entityTypeMeta);
		assertEquals(entityTypeCopy.getId(), "myPackage_myEntity");
		assertSame(entityTypeCopy.getPackage(), package_);
		assertEquals(entityTypeCopy.getLabel(), "label");
		assertEquals(entityTypeCopy.getDescription(), "description");

		List<Attribute> ownAttrsCopy = newArrayList(entityTypeCopy.getOwnAllAttributes());
		assertEquals(ownAttrsCopy.size(), 4);
		assertSame(ownAttrsCopy.get(0), attrId);
		assertSame(ownAttrsCopy.get(1), attrLabel);
		assertSame(ownAttrsCopy.get(2), attrCompound);
		assertSame(ownAttrsCopy.get(3), attrCompoundPart);
		assertSame(entityTypeCopy.getIdAttribute(), attrId);
		assertSame(entityTypeCopy.getLabelAttribute(), attrLabel);

		List<Attribute> ownLookAttrsCopy = newArrayList(entityTypeCopy.getOwnLookupAttributes());

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

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Entity \\[myEntity\\] already contains attribute with name \\[attrName\\], duplicate attribute names are not allowed")
	public void addAttributeWithDuplicateName()
	{
		EntityType entityType = new EntityType(createEntityTypeMeta());
		entityType.setLabel("myEntity");
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn("attrName").getMock();
		Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn("attrName").getMock();
		entityType.addAttribute(attr0);
		entityType.addAttribute(attr1);
	}

	private static EntityType createEntityTypeMeta()
	{
		EntityType entityTypeMeta = mock(EntityType.class);
		Attribute strAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
		Attribute intAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
		Attribute boolAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
		Attribute xrefAttr = when(mock(Attribute.class).getDataType()).thenReturn(XREF).getMock();
		Attribute mrefAttr = when(mock(Attribute.class).getDataType()).thenReturn(MREF).getMock();
		when(entityTypeMeta.getAttribute(EntityTypeMetadata.ID)).thenReturn(strAttr);
		when(entityTypeMeta.getAttribute(EntityTypeMetadata.PACKAGE)).thenReturn(xrefAttr);
		when(entityTypeMeta.getAttribute(LABEL)).thenReturn(strAttr);
		when(entityTypeMeta.getAttribute(EntityTypeMetadata.DESCRIPTION)).thenReturn(strAttr);
		when(entityTypeMeta.getAttribute(EntityTypeMetadata.ATTRIBUTES)).thenReturn(mrefAttr);
		when(entityTypeMeta.getAttribute(EntityTypeMetadata.IS_ABSTRACT)).thenReturn(boolAttr);
		when(entityTypeMeta.getAttribute(EntityTypeMetadata.EXTENDS)).thenReturn(xrefAttr);
		when(entityTypeMeta.getAttribute(EntityTypeMetadata.TAGS)).thenReturn(mrefAttr);
		when(entityTypeMeta.getAttribute(EntityTypeMetadata.BACKEND)).thenReturn(strAttr);
		when(entityTypeMeta.getAttribute(EntityTypeMetadata.INDEXING_DEPTH)).thenReturn(intAttr);
		return entityTypeMeta;
	}
}