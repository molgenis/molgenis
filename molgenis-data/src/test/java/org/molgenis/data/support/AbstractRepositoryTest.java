package org.molgenis.data.support;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class AbstractRepositoryTest
{
	private AbstractRepository abstractRepository;
	private EntityType entityType;

	@BeforeTest
	public void beforeTest()
	{
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("id").getMock();
		entityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		abstractRepository = Mockito.spy(new AbstractRepository()
		{

			@Override
			public Iterator<Entity> iterator()
			{
				return null;
			}

			public EntityType getEntityType()
			{
				return entityType;
			}

			@Override
			public Set<RepositoryCapability> getCapabilities()
			{
				return Collections.emptySet();
			}
		});
	}

	@BeforeMethod
	public void beforeMethod()
	{
		Mockito.reset(abstractRepository);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void addStream()
	{
		abstractRepository.add(Stream.empty());
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void deleteStream()
	{
		abstractRepository.delete(Stream.empty());
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void updateStream()
	{
		abstractRepository.update(Stream.empty());
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void findOneObjectFetch()
	{
		abstractRepository.findOneById(Integer.valueOf(0), new Fetch());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void findAllStream()
	{
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = when(mock(Entity.class).getIdValue()).thenReturn(id0).getMock();
		Entity entity1 = when(mock(Entity.class).getIdValue()).thenReturn(id1).getMock();
		Stream<Object> entityIds = Stream.of(id0, id1);

		Mockito.doReturn(Stream.of(entity0, entity1)).when(abstractRepository).findAll(Matchers.any(Query.class));

		Stream<Entity> expectedEntities = abstractRepository.findAll(entityIds);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void findAllStreamFetch()
	{
		Fetch fetch = new Fetch();
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = when(mock(Entity.class).getIdValue()).thenReturn(id0).getMock();
		Entity entity1 = when(mock(Entity.class).getIdValue()).thenReturn(id1).getMock();
		Stream<Object> entityIds = Stream.of(id0, id1);

		Mockito.doReturn(Stream.of(entity0, entity1)).when(abstractRepository).findAll(Matchers.any(Query.class));

		Stream<Entity> expectedEntities = abstractRepository.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	//	// Note: streamFetch cannot be tested because mocking default methods is not supported by Mockito
}
