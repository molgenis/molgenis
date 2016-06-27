package org.molgenis.data.validation;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import org.molgenis.data.Entity;
import org.molgenis.data.Range;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.DynamicEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EntityAttributesValidatorTest
{
	private EntityAttributesValidator entityAttributesValidator;
	private EntityMetaData intRangeMinMeta;
	private EntityMetaData intRangeMaxMeta;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityAttributesValidator = new EntityAttributesValidator();

		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("id").getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		AttributeMetaData intRangeMinAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("intrangemin")
				.getMock();
		when(intRangeMinAttr.getDataType()).thenReturn(INT);
		when(intRangeMinAttr.getRange()).thenReturn(new Range(1l, null));
		AttributeMetaData intRangeMaxAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("intrangemin")
				.getMock();
		when(intRangeMaxAttr.getDataType()).thenReturn(INT);
		when(intRangeMaxAttr.getRange()).thenReturn(new Range(null, 1l));

		intRangeMinMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		when(intRangeMinMeta.getIdAttribute()).thenReturn(idAttr);
		when(intRangeMinMeta.getAttribute("id")).thenReturn(idAttr);
		when(intRangeMinMeta.getAttribute("intrangemin")).thenReturn(intRangeMinAttr);
		when(intRangeMinMeta.getAtomicAttributes()).thenReturn(asList(idAttr, intRangeMinAttr));

		intRangeMaxMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
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

	@Test
	public void checkXrefValid()
	{
		AttributeMetaData refIdAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("refId").getMock();
		when(refIdAttr.getDataType()).thenReturn(STRING);

		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getName()).thenReturn("refEntity");
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		when(refEntityMeta.getAtomicAttributes()).thenReturn(asList(refIdAttr));

		String idAttrName = "id";
		String xrefAttrName = "xref";
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(idAttrName).getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		AttributeMetaData xrefAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(xrefAttrName).getMock();
		when(xrefAttr.getDataType()).thenReturn(XREF);
		when(xrefAttr.getRefEntity()).thenReturn(refEntityMeta);

		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(entityMeta.getName()).thenReturn("entity");
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		when(entityMeta.getAtomicAttributes()).thenReturn(asList(idAttr, xrefAttr));

		Entity refEntity0 = when(mock(Entity.class).getEntityMetaData()).thenReturn(refEntityMeta).getMock();
		when(refEntity0.getIdValue()).thenReturn("refId0");

		Entity entity0 = when(mock(Entity.class).getEntityMetaData()).thenReturn(entityMeta).getMock();
		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(xrefAttrName)).thenReturn(refEntity0);

		Set<ConstraintViolation> constraints = entityAttributesValidator.validate(entity0, entity0.getEntityMetaData());
		assertEquals(constraints.size(), 0);
	}

	@Test
	public void checkXrefEntityWrongType()
	{
		AttributeMetaData refIdAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("refId").getMock();
		when(refIdAttr.getDataType()).thenReturn(STRING);

		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getName()).thenReturn("refEntity");
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		when(refEntityMeta.getAtomicAttributes()).thenReturn(asList(refIdAttr));

		AttributeMetaData otherRefIdAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("otherRefId")
				.getMock();
		when(otherRefIdAttr.getDataType()).thenReturn(STRING);

		EntityMetaData otherRefEntityMeta = mock(EntityMetaData.class);
		when(otherRefEntityMeta.getName()).thenReturn("otherRefEntity");
		when(otherRefEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		when(otherRefEntityMeta.getAtomicAttributes()).thenReturn(asList(otherRefIdAttr));

		String idAttrName = "id";
		String xrefAttrName = "xref";
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(idAttrName).getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		AttributeMetaData xrefAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(xrefAttrName).getMock();
		when(xrefAttr.getDataType()).thenReturn(XREF);
		when(xrefAttr.getRefEntity()).thenReturn(refEntityMeta);

		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(entityMeta.getName()).thenReturn("entity");
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		when(entityMeta.getAtomicAttributes()).thenReturn(asList(idAttr, xrefAttr));

		Entity refEntity0 = when(mock(Entity.class).getEntityMetaData()).thenReturn(otherRefEntityMeta)
				.getMock(); // wrong
		// intRangeMinMeta
		when(refEntity0.getIdValue()).thenReturn("otherRefId0");

		Entity entity0 = when(mock(Entity.class).getEntityMetaData()).thenReturn(entityMeta).getMock();
		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(xrefAttrName)).thenReturn(refEntity0);

		Set<ConstraintViolation> constraints = entityAttributesValidator.validate(entity0, entity0.getEntityMetaData());
		assertEquals(constraints.size(), 1);
	}

	@Test
	public void checkMrefValid()
	{
		AttributeMetaData refIdAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("refId").getMock();
		when(refIdAttr.getDataType()).thenReturn(STRING);

		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getName()).thenReturn("refEntity");
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		when(refEntityMeta.getAtomicAttributes()).thenReturn(asList(refIdAttr));

		String idAttrName = "id";
		String mrefAttrName = "mref";
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(idAttrName).getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		AttributeMetaData mrefAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(mrefAttrName).getMock();
		when(mrefAttr.getDataType()).thenReturn(MREF);
		when(mrefAttr.getRefEntity()).thenReturn(refEntityMeta);

		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(entityMeta.getName()).thenReturn("entity");
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		when(entityMeta.getAtomicAttributes()).thenReturn(asList(idAttr, mrefAttr));

		Entity refEntity0 = when(mock(Entity.class).getEntityMetaData()).thenReturn(refEntityMeta).getMock();
		when(refEntity0.getIdValue()).thenReturn("refId0");

		Entity refEntity1 = when(mock(Entity.class).getEntityMetaData()).thenReturn(refEntityMeta).getMock();
		when(refEntity1.getIdValue()).thenReturn("refId1");

		Entity entity0 = when(mock(Entity.class).getEntityMetaData()).thenReturn(entityMeta).getMock();
		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntities(mrefAttrName)).thenReturn(asList(refEntity0, refEntity1));

		Set<ConstraintViolation> constraints = entityAttributesValidator.validate(entity0, entity0.getEntityMetaData());
		assertEquals(constraints.size(), 0);
	}

	@Test
	public void checkMrefValidWrongType()
	{
		AttributeMetaData refIdAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("refId").getMock();
		when(refIdAttr.getDataType()).thenReturn(STRING);

		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getName()).thenReturn("refEntity");
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		when(refEntityMeta.getAtomicAttributes()).thenReturn(asList(refIdAttr));

		String idAttrName = "id";
		String mrefAttrName = "mref";
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(idAttrName).getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		AttributeMetaData mrefAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(mrefAttrName).getMock();
		when(mrefAttr.getDataType()).thenReturn(MREF);
		when(mrefAttr.getRefEntity()).thenReturn(refEntityMeta);

		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(entityMeta.getName()).thenReturn("entity");
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		when(entityMeta.getAtomicAttributes()).thenReturn(asList(idAttr, mrefAttr));

		AttributeMetaData otherRefIdAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("otherRefId")
				.getMock();
		when(otherRefIdAttr.getDataType()).thenReturn(STRING);

		EntityMetaData otherRefEntityMeta = mock(EntityMetaData.class);
		when(otherRefEntityMeta.getName()).thenReturn("otherRefEntity");
		when(otherRefEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		when(otherRefEntityMeta.getAtomicAttributes()).thenReturn(asList(otherRefIdAttr));

		Entity refEntity0 = when(mock(Entity.class).getEntityMetaData()).thenReturn(otherRefEntityMeta).getMock();
		when(refEntity0.getIdValue()).thenReturn("refId0");

		Entity refEntity1 = when(mock(Entity.class).getEntityMetaData()).thenReturn(otherRefEntityMeta).getMock();
		when(refEntity1.getIdValue()).thenReturn("refId1");

		Entity entity0 = when(mock(Entity.class).getEntityMetaData()).thenReturn(entityMeta).getMock();
		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntities(mrefAttrName)).thenReturn(asList(refEntity0, refEntity1));

		Set<ConstraintViolation> constraints = entityAttributesValidator.validate(entity0, entity0.getEntityMetaData());
		assertEquals(constraints.size(), 1);
	}

	@Test
	public void checkMrefNullValue()
	{
		AttributeMetaData refIdAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("refId").getMock();
		when(refIdAttr.getDataType()).thenReturn(STRING);

		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getName()).thenReturn("refEntity");
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		when(refEntityMeta.getAtomicAttributes()).thenReturn(asList(refIdAttr));

		String idAttrName = "id";
		String mrefAttrName = "mref";
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(idAttrName).getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		AttributeMetaData mrefAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(mrefAttrName).getMock();
		when(mrefAttr.getDataType()).thenReturn(MREF);
		when(mrefAttr.getRefEntity()).thenReturn(refEntityMeta);

		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(entityMeta.getName()).thenReturn("entity");
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		when(entityMeta.getAtomicAttributes()).thenReturn(asList(idAttr, mrefAttr));

		Entity refEntity0 = when(mock(Entity.class).getEntityMetaData()).thenReturn(refEntityMeta).getMock();
		when(refEntity0.getIdValue()).thenReturn("refId0");

		Entity refEntity1 = when(mock(Entity.class).getEntityMetaData()).thenReturn(refEntityMeta).getMock();
		when(refEntity1.getIdValue()).thenReturn("refId1");

		Entity entity0 = when(mock(Entity.class).getEntityMetaData()).thenReturn(entityMeta).getMock();
		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntities(mrefAttrName)).thenReturn(asList(refEntity0, null, refEntity1));

		Set<ConstraintViolation> constraints = entityAttributesValidator.validate(entity0, entity0.getEntityMetaData());
		assertEquals(constraints.size(), 1);
	}
}
