package org.molgenis.data.validation;

import org.molgenis.data.Entity;
import org.molgenis.data.Range;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.DynamicEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class EntityAttributesValidatorTest
{
	private EntityAttributesValidator entityAttributesValidator;
	private EntityType intRangeMinMeta;
	private EntityType intRangeMaxMeta;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		ExpressionValidator expressionValidator = mock(ExpressionValidator.class);
		entityAttributesValidator = new EntityAttributesValidator(expressionValidator);

		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("id").getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		Attribute intRangeMinAttr = when(mock(Attribute.class).getName()).thenReturn("intrangemin").getMock();
		when(intRangeMinAttr.getDataType()).thenReturn(INT);
		when(intRangeMinAttr.getRange()).thenReturn(new Range(1l, null));
		Attribute intRangeMaxAttr = when(mock(Attribute.class).getName()).thenReturn("intrangemin").getMock();
		when(intRangeMaxAttr.getDataType()).thenReturn(INT);
		when(intRangeMaxAttr.getRange()).thenReturn(new Range(null, 1l));

		intRangeMinMeta = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		when(intRangeMinMeta.getIdAttribute()).thenReturn(idAttr);
		when(intRangeMinMeta.getAttribute("id")).thenReturn(idAttr);
		when(intRangeMinMeta.getAttribute("intrangemin")).thenReturn(intRangeMinAttr);
		when(intRangeMinMeta.getAtomicAttributes()).thenReturn(asList(idAttr, intRangeMinAttr));

		intRangeMaxMeta = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		when(intRangeMaxMeta.getIdAttribute()).thenReturn(idAttr);
		when(intRangeMaxMeta.getAttribute("id")).thenReturn(idAttr);
		when(intRangeMaxMeta.getAttribute("intrangemin")).thenReturn(intRangeMaxAttr);
		when(intRangeMaxMeta.getAtomicAttributes()).thenReturn(asList(idAttr, intRangeMaxAttr));
	}

	@Test
	public void checkRangeMinOnly()
	{
		Entity entity = new DynamicEntity(intRangeMinMeta);
		entity.set("id", "123");
		entity.set("intrangemin", 2);
		Set<ConstraintViolation> constraints = entityAttributesValidator.validate(entity, intRangeMinMeta);
		assertTrue(constraints.isEmpty());
	}

	@Test
	public void checkRangeMinOnlyInvalid()
	{
		Entity entity = new DynamicEntity(intRangeMinMeta);
		entity.set("id", "123");
		entity.set("intrangemin", -1);
		Set<ConstraintViolation> constraints = entityAttributesValidator.validate(entity, intRangeMinMeta);
		assertEquals(constraints.size(), 1);
	}

	@Test
	public void checkRangeMaxOnly()
	{
		Entity entity = new DynamicEntity(intRangeMaxMeta);
		entity.set("id", "123");
		entity.set("intrangemin", 0);
		Set<ConstraintViolation> constraints = entityAttributesValidator.validate(entity, intRangeMaxMeta);
		assertTrue(constraints.isEmpty());
	}

	@Test
	public void checkRangeMaxOnlyInvalid()
	{
		Entity entity = new DynamicEntity(intRangeMaxMeta);
		entity.set("id", "123");
		entity.set("intrangemin", 2);
		Set<ConstraintViolation> constraints = entityAttributesValidator.validate(entity, intRangeMaxMeta);
		assertEquals(constraints.size(), 1);
	}

	@DataProvider(name = "checkXrefValidProvider")
	public static Iterator<Object[]> checkXrefValidProvider()
	{
		return newArrayList(new Object[] { XREF }, new Object[] { CATEGORICAL }).iterator();
	}

	@Test(dataProvider = "checkXrefValidProvider")
	public void checkXrefValid(AttributeType attrType)
	{
		Attribute refIdAttr = when(mock(Attribute.class).getName()).thenReturn("refId").getMock();
		when(refIdAttr.getDataType()).thenReturn(STRING);

		EntityType refEntityType = mock(EntityType.class);
		when(refEntityType.getName()).thenReturn("refEntity");
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttr);
		when(refEntityType.getAtomicAttributes()).thenReturn(asList(refIdAttr));

		String idAttrName = "id";
		String xrefAttrName = "xref";
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn(idAttrName).getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		Attribute xrefAttr = when(mock(Attribute.class).getName()).thenReturn(xrefAttrName).getMock();
		when(xrefAttr.getDataType()).thenReturn(attrType);
		when(xrefAttr.getRefEntity()).thenReturn(refEntityType);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getName()).thenReturn("entity");
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(entityType.getAtomicAttributes()).thenReturn(asList(idAttr, xrefAttr));

		Entity refEntity0 = when(mock(Entity.class).getEntityType()).thenReturn(refEntityType).getMock();
		when(refEntity0.getIdValue()).thenReturn("refId0");

		Entity entity0 = when(mock(Entity.class).getEntityType()).thenReturn(entityType).getMock();
		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(xrefAttrName)).thenReturn(refEntity0);

		Set<ConstraintViolation> constraints = entityAttributesValidator.validate(entity0, entity0.getEntityType());
		assertEquals(constraints.size(), 0);
	}

	@Test(dataProvider = "checkXrefValidProvider")
	public void checkXrefEntityWrongType(AttributeType attrType)
	{
		Attribute refIdAttr = when(mock(Attribute.class).getName()).thenReturn("refId").getMock();
		when(refIdAttr.getDataType()).thenReturn(STRING);

		EntityType refEntityType = mock(EntityType.class);
		when(refEntityType.getName()).thenReturn("refEntity");
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttr);
		when(refEntityType.getAtomicAttributes()).thenReturn(asList(refIdAttr));

		Attribute otherRefIdAttr = when(mock(Attribute.class).getName()).thenReturn("otherRefId").getMock();
		when(otherRefIdAttr.getDataType()).thenReturn(STRING);

		EntityType otherRefEntityType = mock(EntityType.class);
		when(otherRefEntityType.getName()).thenReturn("otherRefEntity");
		when(otherRefEntityType.getIdAttribute()).thenReturn(refIdAttr);
		when(otherRefEntityType.getAtomicAttributes()).thenReturn(asList(otherRefIdAttr));

		String idAttrName = "id";
		String xrefAttrName = "xref";
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn(idAttrName).getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		Attribute xrefAttr = when(mock(Attribute.class).getName()).thenReturn(xrefAttrName).getMock();
		when(xrefAttr.getDataType()).thenReturn(attrType);
		when(xrefAttr.getRefEntity()).thenReturn(refEntityType);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getName()).thenReturn("entity");
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(entityType.getAtomicAttributes()).thenReturn(asList(idAttr, xrefAttr));

		Entity refEntity0 = when(mock(Entity.class).getEntityType()).thenReturn(otherRefEntityType).getMock(); // wrong
		// intRangeMinMeta
		when(refEntity0.getIdValue()).thenReturn("otherRefId0");

		Entity entity0 = when(mock(Entity.class).getEntityType()).thenReturn(entityType).getMock();
		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(xrefAttrName)).thenReturn(refEntity0);

		Set<ConstraintViolation> constraints = entityAttributesValidator.validate(entity0, entity0.getEntityType());
		assertEquals(constraints.size(), 1);
	}

	@DataProvider(name = "checkMrefValidProvider")
	public static Iterator<Object[]> checkMrefValidProvider()
	{
		return newArrayList(new Object[] { MREF }, new Object[] { ONE_TO_MANY }).iterator();
	}

	@Test(dataProvider = "checkMrefValidProvider")
	public void checkMrefValid(AttributeType attrType)
	{
		Attribute refIdAttr = when(mock(Attribute.class).getName()).thenReturn("refId").getMock();
		when(refIdAttr.getDataType()).thenReturn(STRING);

		EntityType refEntityType = mock(EntityType.class);
		when(refEntityType.getName()).thenReturn("refEntity");
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttr);
		when(refEntityType.getAtomicAttributes()).thenReturn(asList(refIdAttr));

		String idAttrName = "id";
		String mrefAttrName = "mref";
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn(idAttrName).getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		Attribute mrefAttr = when(mock(Attribute.class).getName()).thenReturn(mrefAttrName).getMock();
		when(mrefAttr.getDataType()).thenReturn(attrType);
		when(mrefAttr.getRefEntity()).thenReturn(refEntityType);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getName()).thenReturn("entity");
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(entityType.getAtomicAttributes()).thenReturn(asList(idAttr, mrefAttr));

		Entity refEntity0 = when(mock(Entity.class).getEntityType()).thenReturn(refEntityType).getMock();
		when(refEntity0.getIdValue()).thenReturn("refId0");

		Entity refEntity1 = when(mock(Entity.class).getEntityType()).thenReturn(refEntityType).getMock();
		when(refEntity1.getIdValue()).thenReturn("refId1");

		Entity entity0 = when(mock(Entity.class).getEntityType()).thenReturn(entityType).getMock();
		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntities(mrefAttrName)).thenReturn(asList(refEntity0, refEntity1));

		Set<ConstraintViolation> constraints = entityAttributesValidator.validate(entity0, entity0.getEntityType());
		assertEquals(constraints.size(), 0);
	}

	@Test(dataProvider = "checkMrefValidProvider")
	public void checkMrefValidWrongType(AttributeType attrType)
	{
		Attribute refIdAttr = when(mock(Attribute.class).getName()).thenReturn("refId").getMock();
		when(refIdAttr.getDataType()).thenReturn(STRING);

		EntityType refEntityType = mock(EntityType.class);
		when(refEntityType.getName()).thenReturn("refEntity");
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttr);
		when(refEntityType.getAtomicAttributes()).thenReturn(asList(refIdAttr));

		String idAttrName = "id";
		String mrefAttrName = "mref";
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn(idAttrName).getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		Attribute mrefAttr = when(mock(Attribute.class).getName()).thenReturn(mrefAttrName).getMock();
		when(mrefAttr.getDataType()).thenReturn(attrType);
		when(mrefAttr.getRefEntity()).thenReturn(refEntityType);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getName()).thenReturn("entity");
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(entityType.getAtomicAttributes()).thenReturn(asList(idAttr, mrefAttr));

		Attribute otherRefIdAttr = when(mock(Attribute.class).getName()).thenReturn("otherRefId").getMock();
		when(otherRefIdAttr.getDataType()).thenReturn(STRING);

		EntityType otherRefEntityType = mock(EntityType.class);
		when(otherRefEntityType.getName()).thenReturn("otherRefEntity");
		when(otherRefEntityType.getIdAttribute()).thenReturn(refIdAttr);
		when(otherRefEntityType.getAtomicAttributes()).thenReturn(asList(otherRefIdAttr));

		Entity refEntity0 = when(mock(Entity.class).getEntityType()).thenReturn(otherRefEntityType).getMock();
		when(refEntity0.getIdValue()).thenReturn("refId0");

		Entity refEntity1 = when(mock(Entity.class).getEntityType()).thenReturn(otherRefEntityType).getMock();
		when(refEntity1.getIdValue()).thenReturn("refId1");

		Entity entity0 = when(mock(Entity.class).getEntityType()).thenReturn(entityType).getMock();
		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntities(mrefAttrName)).thenReturn(asList(refEntity0, refEntity1));

		Set<ConstraintViolation> constraints = entityAttributesValidator.validate(entity0, entity0.getEntityType());
		assertEquals(constraints.size(), 1);
	}

	@Test(dataProvider = "checkMrefValidProvider")
	public void checkMrefNullValue(AttributeType attrType)
	{
		Attribute refIdAttr = when(mock(Attribute.class).getName()).thenReturn("refId").getMock();
		when(refIdAttr.getDataType()).thenReturn(STRING);

		EntityType refEntityType = mock(EntityType.class);
		when(refEntityType.getName()).thenReturn("refEntity");
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttr);
		when(refEntityType.getAtomicAttributes()).thenReturn(asList(refIdAttr));

		String idAttrName = "id";
		String mrefAttrName = "mref";
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn(idAttrName).getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		Attribute mrefAttr = when(mock(Attribute.class).getName()).thenReturn(mrefAttrName).getMock();
		when(mrefAttr.getDataType()).thenReturn(attrType);
		when(mrefAttr.getRefEntity()).thenReturn(refEntityType);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getName()).thenReturn("entity");
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(entityType.getAtomicAttributes()).thenReturn(asList(idAttr, mrefAttr));

		Entity refEntity0 = when(mock(Entity.class).getEntityType()).thenReturn(refEntityType).getMock();
		when(refEntity0.getIdValue()).thenReturn("refId0");

		Entity refEntity1 = when(mock(Entity.class).getEntityType()).thenReturn(refEntityType).getMock();
		when(refEntity1.getIdValue()).thenReturn("refId1");

		Entity entity0 = when(mock(Entity.class).getEntityType()).thenReturn(entityType).getMock();
		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntities(mrefAttrName)).thenReturn(asList(refEntity0, null, refEntity1));

		Set<ConstraintViolation> constraints = entityAttributesValidator.validate(entity0, entity0.getEntityType());
		assertEquals(constraints.size(), 1);
	}
}
