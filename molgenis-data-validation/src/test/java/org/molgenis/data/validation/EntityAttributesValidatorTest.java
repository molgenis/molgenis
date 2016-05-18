package org.molgenis.data.validation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.Range;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.support.DefaultEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EntityAttributesValidatorTest
{
	private EntityAttributesValidator entityAttributesValidator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityAttributesValidator = new EntityAttributesValidator();
	}

	@Test
	public void checkRangeMinOnly()
	{
		EntityMetaData meta = new EntityMetaDataImpl("entity");
		meta.addAttribute("id", ROLE_ID);
		meta.addAttribute("intrangemin").setDataType(MolgenisFieldTypes.INT).setRange(new Range(1l, null));

		DefaultEntity entity = new DefaultEntity(meta, null);
		entity.set("id", "123");
		entity.set("intrangemin", 2l);
		Set<ConstraintViolation> constraints = entityAttributesValidator.validate(entity, meta);
		assertTrue(constraints.isEmpty());
	}

	@Test
	public void checkRangeMinOnlyInvalid()
	{
		EntityMetaData meta = new EntityMetaDataImpl("entity");
		meta.addAttribute("id", ROLE_ID);
		meta.addAttribute("intrangemin").setDataType(MolgenisFieldTypes.INT).setRange(new Range(1l, null));

		DefaultEntity entity = new DefaultEntity(meta, null);
		entity.set("id", "123");
		entity.set("intrangemin", -1l);
		Set<ConstraintViolation> constraints = entityAttributesValidator.validate(entity, meta);
		assertEquals(constraints.size(), 1);
	}

	@Test
	public void checkRangeMaxOnly()
	{
		EntityMetaData meta = new EntityMetaDataImpl("entity");
		meta.addAttribute("id", ROLE_ID);
		meta.addAttribute("intrangemin").setDataType(MolgenisFieldTypes.INT).setRange(new Range(null, 1l));

		DefaultEntity entity = new DefaultEntity(meta, null);
		entity.set("id", "123");
		entity.set("intrangemin", 0l);
		Set<ConstraintViolation> constraints = entityAttributesValidator.validate(entity, meta);
		assertTrue(constraints.isEmpty());
	}

	@Test
	public void checkRangeMaxOnlyInvalid()
	{
		EntityMetaData meta = new EntityMetaDataImpl("entity");
		meta.addAttribute("id", ROLE_ID);
		meta.addAttribute("intrangemin").setDataType(MolgenisFieldTypes.INT).setRange(new Range(null, 1l));

		DefaultEntity entity = new DefaultEntity(meta, null);
		entity.set("id", "123");
		entity.set("intrangemin", 2l);
		Set<ConstraintViolation> constraints = entityAttributesValidator.validate(entity, meta);
		assertEquals(constraints.size(), 1);
	}

	@Test
	public void checkXrefValid()
	{
		AttributeMetaData refIdAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("refId").getMock();
		when(refIdAttr.getDataType()).thenReturn(STRING);

		EntityMetaData refEntityMeta = mock(EntityMetaDataImpl.class);
		when(refEntityMeta.getName()).thenReturn("refEntity");
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		when(refEntityMeta.getAtomicAttributes()).thenReturn(Arrays.asList(refIdAttr));

		String idAttrName = "id";
		String xrefAttrName = "xref";
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(idAttrName).getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		AttributeMetaData xrefAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(xrefAttrName).getMock();
		when(xrefAttr.getDataType()).thenReturn(XREF);
		when(xrefAttr.getRefEntity()).thenReturn(refEntityMeta);

		EntityMetaData entityMeta = mock(EntityMetaDataImpl.class);
		when(entityMeta.getName()).thenReturn("entity");
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		when(entityMeta.getAtomicAttributes()).thenReturn(Arrays.asList(idAttr, xrefAttr));

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

		EntityMetaData refEntityMeta = mock(EntityMetaDataImpl.class);
		when(refEntityMeta.getName()).thenReturn("refEntity");
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		when(refEntityMeta.getAtomicAttributes()).thenReturn(Arrays.asList(refIdAttr));

		AttributeMetaData otherRefIdAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("otherRefId")
				.getMock();
		when(otherRefIdAttr.getDataType()).thenReturn(STRING);

		EntityMetaData otherRefEntityMeta = mock(EntityMetaDataImpl.class);
		when(otherRefEntityMeta.getName()).thenReturn("refEntity");
		when(otherRefEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		when(otherRefEntityMeta.getAtomicAttributes()).thenReturn(Arrays.asList(otherRefIdAttr));

		String idAttrName = "id";
		String xrefAttrName = "xref";
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(idAttrName).getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		AttributeMetaData xrefAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(xrefAttrName).getMock();
		when(xrefAttr.getDataType()).thenReturn(XREF);
		when(xrefAttr.getRefEntity()).thenReturn(refEntityMeta);

		EntityMetaData entityMeta = mock(EntityMetaDataImpl.class);
		when(entityMeta.getName()).thenReturn("entity");
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		when(entityMeta.getAtomicAttributes()).thenReturn(Arrays.asList(idAttr, xrefAttr));

		Entity refEntity0 = when(mock(Entity.class).getEntityMetaData()).thenReturn(otherRefEntityMeta).getMock(); // wrong
																													// meta
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

		EntityMetaData refEntityMeta = mock(EntityMetaDataImpl.class);
		when(refEntityMeta.getName()).thenReturn("refEntity");
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		when(refEntityMeta.getAtomicAttributes()).thenReturn(Arrays.asList(refIdAttr));

		String idAttrName = "id";
		String mrefAttrName = "mref";
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(idAttrName).getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		AttributeMetaData mrefAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(mrefAttrName).getMock();
		when(mrefAttr.getDataType()).thenReturn(MREF);
		when(mrefAttr.getRefEntity()).thenReturn(refEntityMeta);

		EntityMetaData entityMeta = mock(EntityMetaDataImpl.class);
		when(entityMeta.getName()).thenReturn("entity");
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		when(entityMeta.getAtomicAttributes()).thenReturn(Arrays.asList(idAttr, mrefAttr));

		Entity refEntity0 = when(mock(Entity.class).getEntityMetaData()).thenReturn(refEntityMeta).getMock();
		when(refEntity0.getIdValue()).thenReturn("refId0");

		Entity refEntity1 = when(mock(Entity.class).getEntityMetaData()).thenReturn(refEntityMeta).getMock();
		when(refEntity1.getIdValue()).thenReturn("refId1");

		Entity entity0 = when(mock(Entity.class).getEntityMetaData()).thenReturn(entityMeta).getMock();
		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntities(mrefAttrName)).thenReturn(Arrays.asList(refEntity0, refEntity1));

		Set<ConstraintViolation> constraints = entityAttributesValidator.validate(entity0, entity0.getEntityMetaData());
		assertEquals(constraints.size(), 0);
	}

	@Test
	public void checkMrefValidWrongType()
	{
		AttributeMetaData refIdAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("refId").getMock();
		when(refIdAttr.getDataType()).thenReturn(STRING);

		EntityMetaData refEntityMeta = mock(EntityMetaDataImpl.class);
		when(refEntityMeta.getName()).thenReturn("refEntity");
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		when(refEntityMeta.getAtomicAttributes()).thenReturn(Arrays.asList(refIdAttr));

		String idAttrName = "id";
		String mrefAttrName = "mref";
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(idAttrName).getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		AttributeMetaData mrefAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(mrefAttrName).getMock();
		when(mrefAttr.getDataType()).thenReturn(MREF);
		when(mrefAttr.getRefEntity()).thenReturn(refEntityMeta);

		EntityMetaData entityMeta = mock(EntityMetaDataImpl.class);
		when(entityMeta.getName()).thenReturn("entity");
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		when(entityMeta.getAtomicAttributes()).thenReturn(Arrays.asList(idAttr, mrefAttr));

		AttributeMetaData otherRefIdAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("otherRefId")
				.getMock();
		when(otherRefIdAttr.getDataType()).thenReturn(STRING);

		EntityMetaData otherRefEntityMeta = mock(EntityMetaDataImpl.class);
		when(otherRefEntityMeta.getName()).thenReturn("refEntity");
		when(otherRefEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		when(otherRefEntityMeta.getAtomicAttributes()).thenReturn(Arrays.asList(otherRefIdAttr));

		Entity refEntity0 = when(mock(Entity.class).getEntityMetaData()).thenReturn(otherRefEntityMeta).getMock();
		when(refEntity0.getIdValue()).thenReturn("refId0");

		Entity refEntity1 = when(mock(Entity.class).getEntityMetaData()).thenReturn(otherRefEntityMeta).getMock();
		when(refEntity1.getIdValue()).thenReturn("refId1");

		Entity entity0 = when(mock(Entity.class).getEntityMetaData()).thenReturn(entityMeta).getMock();
		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntities(mrefAttrName)).thenReturn(Arrays.asList(refEntity0, refEntity1));

		Set<ConstraintViolation> constraints = entityAttributesValidator.validate(entity0, entity0.getEntityMetaData());
		assertEquals(constraints.size(), 1);
	}

	@Test
	public void checkMrefNullValue()
	{
		AttributeMetaData refIdAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("refId").getMock();
		when(refIdAttr.getDataType()).thenReturn(STRING);

		EntityMetaData refEntityMeta = mock(EntityMetaDataImpl.class);
		when(refEntityMeta.getName()).thenReturn("refEntity");
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		when(refEntityMeta.getAtomicAttributes()).thenReturn(Arrays.asList(refIdAttr));

		String idAttrName = "id";
		String mrefAttrName = "mref";
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(idAttrName).getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		AttributeMetaData mrefAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(mrefAttrName).getMock();
		when(mrefAttr.getDataType()).thenReturn(MREF);
		when(mrefAttr.getRefEntity()).thenReturn(refEntityMeta);

		EntityMetaData entityMeta = mock(EntityMetaDataImpl.class);
		when(entityMeta.getName()).thenReturn("entity");
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		when(entityMeta.getAtomicAttributes()).thenReturn(Arrays.asList(idAttr, mrefAttr));

		Entity refEntity0 = when(mock(Entity.class).getEntityMetaData()).thenReturn(refEntityMeta).getMock();
		when(refEntity0.getIdValue()).thenReturn("refId0");

		Entity refEntity1 = when(mock(Entity.class).getEntityMetaData()).thenReturn(refEntityMeta).getMock();
		when(refEntity1.getIdValue()).thenReturn("refId1");

		Entity entity0 = when(mock(Entity.class).getEntityMetaData()).thenReturn(entityMeta).getMock();
		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntities(mrefAttrName)).thenReturn(Arrays.asList(refEntity0, null, refEntity1));

		Set<ConstraintViolation> constraints = entityAttributesValidator.validate(entity0, entity0.getEntityMetaData());
		assertEquals(constraints.size(), 1);
	}
}
