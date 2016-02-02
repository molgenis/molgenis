package org.molgenis.data.validation;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Range;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.testng.annotations.Test;

public class EntityAttributesValidatorTest
{
	@Test
	public void checkRangeMinOnly()
	{
		EntityAttributesValidator entityAttributesValidator = new EntityAttributesValidator();
		DefaultEntityMetaData meta = new DefaultEntityMetaData("entity");
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
		EntityAttributesValidator entityAttributesValidator = new EntityAttributesValidator();
		DefaultEntityMetaData meta = new DefaultEntityMetaData("entity");
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
		EntityAttributesValidator entityAttributesValidator = new EntityAttributesValidator();
		DefaultEntityMetaData meta = new DefaultEntityMetaData("entity");
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
		EntityAttributesValidator entityAttributesValidator = new EntityAttributesValidator();
		DefaultEntityMetaData meta = new DefaultEntityMetaData("entity");
		meta.addAttribute("id", ROLE_ID);
		meta.addAttribute("intrangemin").setDataType(MolgenisFieldTypes.INT).setRange(new Range(null, 1l));

		DefaultEntity entity = new DefaultEntity(meta, null);
		entity.set("id", "123");
		entity.set("intrangemin", 2l);
		Set<ConstraintViolation> constraints = entityAttributesValidator.validate(entity, meta);
		assertEquals(constraints.size(), 1);
	}
}
