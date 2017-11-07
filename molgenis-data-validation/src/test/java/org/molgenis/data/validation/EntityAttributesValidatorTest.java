package org.molgenis.data.validation;

import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.molgenis.data.Entity;
import org.molgenis.data.Range;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class EntityAttributesValidatorTest extends AbstractMockitoTest
{
	@Mock
	private ExpressionValidator expressionValidator;

	private EntityAttributesValidator entityAttributesValidator;

	private EntityType intRangeMinMeta;
	private EntityType intRangeMaxMeta;

	public EntityAttributesValidatorTest()
	{
		super(Strictness.WARN);
	}

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		expressionValidator = mock(ExpressionValidator.class);
		entityAttributesValidator = new EntityAttributesValidator(expressionValidator);

		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("id").getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		Attribute intRangeMinAttr = when(mock(Attribute.class).getName()).thenReturn("intrangemin").getMock();
		when(intRangeMinAttr.getDataType()).thenReturn(INT);
		when(intRangeMinAttr.getRange()).thenReturn(new Range(1L, null));
		Attribute intRangeMaxAttr = when(mock(Attribute.class).getName()).thenReturn("intrangemin").getMock();
		when(intRangeMaxAttr.getDataType()).thenReturn(INT);
		when(intRangeMaxAttr.getRange()).thenReturn(new Range(null, 1L));

		intRangeMinMeta = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(intRangeMinMeta.getIdAttribute()).thenReturn(idAttr);
		when(intRangeMinMeta.getAttribute("id")).thenReturn(idAttr);
		when(intRangeMinMeta.getAttribute("intrangemin")).thenReturn(intRangeMinAttr);
		when(intRangeMinMeta.getAtomicAttributes()).thenReturn(asList(idAttr, intRangeMinAttr));

		intRangeMaxMeta = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
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
		when(refEntityType.getId()).thenReturn("refEntity");
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
		when(entityType.getId()).thenReturn("entity");
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
		when(refEntityType.getId()).thenReturn("refEntity");
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttr);
		when(refEntityType.getAtomicAttributes()).thenReturn(asList(refIdAttr));

		Attribute otherRefIdAttr = when(mock(Attribute.class).getName()).thenReturn("otherRefId").getMock();
		when(otherRefIdAttr.getDataType()).thenReturn(STRING);

		EntityType otherRefEntityType = mock(EntityType.class);
		when(otherRefEntityType.getId()).thenReturn("otherRefEntity");
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
		when(entityType.getId()).thenReturn("entity");
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
		when(refEntityType.getId()).thenReturn("refEntity");
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
		when(entityType.getId()).thenReturn("entity");
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
		when(refEntityType.getId()).thenReturn("refEntity");
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
		when(entityType.getId()).thenReturn("entity");
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(entityType.getAtomicAttributes()).thenReturn(asList(idAttr, mrefAttr));

		Attribute otherRefIdAttr = when(mock(Attribute.class).getName()).thenReturn("otherRefId").getMock();
		when(otherRefIdAttr.getDataType()).thenReturn(STRING);

		EntityType otherRefEntityType = mock(EntityType.class);
		when(otherRefEntityType.getId()).thenReturn("otherRefEntity");
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
		when(refEntityType.getId()).thenReturn("refEntity");
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
		when(entityType.getId()).thenReturn("entity");
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

	@Test
	public void validateNullableExpression()
	{
		String expression0 = "expression0";
		String expression1 = "expression1";
		String expression2 = "expression2";
		Attribute attr0 = createMockAttribute("attr0", STRING, expression0);
		Attribute attr1 = createMockAttribute("attr1", STRING, expression1);
		Attribute attr2 = createMockAttribute("attr2", MREF, expression2);
		Attribute attr3 = createMockAttribute("attr2", INT, null);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("entityType");
		when(entityType.getAtomicAttributes()).thenReturn(asList(attr0, attr1, attr2, attr3));
		when(entityType.toString()).thenReturn("entityType");

		Entity entity = mock(Entity.class);
		when(entity.getLabelValue()).thenReturn("lbl-entity");
		when(entity.getString("attr0")).thenReturn(null);
		when(entity.getString("attr1")).thenReturn(null);
		when(entity.getEntities("attr2")).thenReturn(emptyList());
		when(entity.getInt("attr3")).thenReturn(null);

		List<Boolean> expressionResults = asList(true, false, false);
		when(expressionValidator.resolveBooleanExpressions(asList(expression0, expression1, expression2),
				entity)).thenReturn(expressionResults);
		Set<ConstraintViolation> constraintViolations = entityAttributesValidator.validate(entity, entityType);
		Set<ConstraintViolation> expectedConstraintViolations = newHashSet(new ConstraintViolation(
						"Invalid [string] value [null] for attribute [lbl-attr1] of entity [lbl-entity] with type [entityType]. Offended nullable expression: expression1"),
				new ConstraintViolation(
						"Invalid [mref] value [[]] for attribute [lbl-attr2] of entity [lbl-entity] with type [entityType]. Offended nullable expression: expression2"));
		assertEquals(newHashSet(constraintViolations), expectedConstraintViolations);
	}

	private Attribute createMockAttribute(String attributeName, AttributeType attributeType, String expression)
	{
		Attribute attribute = mock(Attribute.class);
		when(attribute.getName()).thenReturn(attributeName);
		when(attribute.getLabel()).thenReturn("lbl-" + attributeName);
		when(attribute.getDataType()).thenReturn(attributeType);
		when(attribute.getNullableExpression()).thenReturn(expression);
		when(attribute.toString()).thenReturn(attributeName);
		return attribute;
	}
}
