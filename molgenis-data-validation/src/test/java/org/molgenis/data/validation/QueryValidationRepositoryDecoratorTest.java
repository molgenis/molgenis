package org.molgenis.data.validation;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class QueryValidationRepositoryDecoratorTest
{
	private QueryValidationRepositoryDecorator<Entity> queryValidationRepositoryDecorator;
	private EntityType entityType;
	private Repository<Entity> delegateRepository;
	private QueryValidator queryValidator;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		delegateRepository = mock(Repository.class);
		entityType = mock(EntityType.class);
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		queryValidator = mock(QueryValidator.class);
		queryValidationRepositoryDecorator = new QueryValidationRepositoryDecorator<>(delegateRepository,
				queryValidator);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testQueryValidationRepositoryDecorator()
	{
		new QueryValidationRepositoryDecorator<>(null, null);
	}

	@Test
	public void testCountQueryValid()
	{
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		long count = 123L;
		when(delegateRepository.count(query)).thenReturn(count);
		assertEquals(count, queryValidationRepositoryDecorator.count(query));
		verify(queryValidator).validate(query, entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void testCountQueryInvalid()
	{
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		doThrow(mock(MolgenisValidationException.class)).when(queryValidator).validate(query, entityType);
		queryValidationRepositoryDecorator.count(query);
	}

	@Test
	public void testFindAllQueryValid()
	{
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		@SuppressWarnings("unchecked")
		Stream<Entity> entityStream = mock(Stream.class);
		when(delegateRepository.findAll(query)).thenReturn(entityStream);
		assertEquals(entityStream, queryValidationRepositoryDecorator.findAll(query));
		verify(queryValidator).validate(query, entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void testFindAllQueryInvalid()
	{
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		doThrow(mock(MolgenisValidationException.class)).when(queryValidator).validate(query, entityType);
		queryValidationRepositoryDecorator.findAll(query);
	}

	@Test
	public void testFindOneQueryValid()
	{
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		Entity entity = mock(Entity.class);
		when(delegateRepository.findOne(query)).thenReturn(entity);
		assertEquals(entity, queryValidationRepositoryDecorator.findOne(query));
		verify(queryValidator).validate(query, entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void testFindOneQueryInvalid()
	{
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		doThrow(mock(MolgenisValidationException.class)).when(queryValidator).validate(query, entityType);
		queryValidationRepositoryDecorator.findOne(query);
	}
}