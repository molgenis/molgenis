package org.molgenis.data.mysql;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.testng.annotations.Test;

public class MysqlEntityValidatorTest
{
	private final MysqlEntityValidator validator = new MysqlEntityValidator(new DataServiceImpl(),
			new EntityAttributesValidator());

	@Test
	public void checkIdValues()
	{
		DefaultEntityMetaData em = new DefaultEntityMetaData("test");
		em.addAttribute("id").setNillable(false).setIdAttribute(true);

		List<Entity> entities = Arrays.<Entity> asList(new MapEntity("id", "a"), new MapEntity("id", "b"),
				new MapEntity("id", "c"));

		Set<ConstraintViolation> violations = validator.checkIdValues(entities, em, DatabaseAction.ADD);
		assertNotNull(violations);
		assertTrue(violations.isEmpty());

		entities = Arrays.<Entity> asList(new MapEntity("id", "a"), new MapEntity("id", "b"), new MapEntity("id", "a"),
				new MapEntity("id", "b"));

		violations = validator.checkIdValues(entities, em, DatabaseAction.ADD);
		assertNotNull(violations);
		assertEquals(violations.size(), 2);
	}

	@Test
	public void checkNotNull()
	{
		DefaultEntityMetaData em = new DefaultEntityMetaData("test");
		em.addAttribute("id").setNillable(false).setIdAttribute(true);

		List<Entity> entities = Arrays.<Entity> asList(new MapEntity("id", "a"), new MapEntity("id", "b"),
				new MapEntity("id", "c"));

		Set<ConstraintViolation> violations = validator.checkNotNull(entities, em);
		assertNotNull(violations);
		assertTrue(violations.isEmpty());

		entities = Arrays.<Entity> asList(new MapEntity("id", null), new MapEntity("id", null));

		violations = validator.checkNotNull(entities, em);
		assertNotNull(violations);
		assertEquals(violations.size(), 2);
	}
}
