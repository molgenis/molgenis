package org.molgenis.data.validation.meta;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.validation.MolgenisValidationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class EntityTypeRepositoryValidationDecoratorTest
{
	private EntityTypeRepositoryValidationDecorator entityTypeRepoValidationDecorator;
	private Repository<EntityType> decoratedRepo;
	private EntityTypeValidator entityTypeValidator;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepo = mock(Repository.class);
		entityTypeValidator = mock(EntityTypeValidator.class);
		entityTypeRepoValidationDecorator = new EntityTypeRepositoryValidationDecorator(decoratedRepo,
				entityTypeValidator);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void EntityTypeRepositoryValidationDecorator()
	{
		new EntityTypeRepositoryValidationDecorator(null, null);
	}

	@Test
	public void delegate()
	{
		assertEquals(entityTypeRepoValidationDecorator.delegate(), decoratedRepo);
	}

	@Test
	public void updateEntityValid()
	{
		EntityType entityType = mock(EntityType.class);
		doNothing().when(entityTypeValidator).validate(entityType);
		entityTypeRepoValidationDecorator.update(entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void updateEntityInvalid() throws Exception
	{
		EntityType entityType = mock(EntityType.class);
		doThrow(mock(MolgenisValidationException.class)).when(entityTypeValidator).validate(entityType);
		entityTypeRepoValidationDecorator.update(entityType);
	}

	@Test
	public void updateEntityStreamValid()
	{
		EntityType entityType0 = mock(EntityType.class);
		EntityType entityType1 = mock(EntityType.class);
		doNothing().when(entityTypeValidator).validate(entityType0);
		doNothing().when(entityTypeValidator).validate(entityType1);
		entityTypeRepoValidationDecorator.update(Stream.of(entityType0, entityType1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<EntityType>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo).update(captor.capture());
		captor.getValue().count(); // process all entities in stream
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void updateEntityStreamInvalid()
	{
		EntityType entityType0 = mock(EntityType.class);
		EntityType entityType1 = mock(EntityType.class);
		doNothing().when(entityTypeValidator).validate(entityType0);
		doThrow(mock(MolgenisValidationException.class)).when(entityTypeValidator).validate(entityType1);
		entityTypeRepoValidationDecorator.update(Stream.of(entityType0, entityType1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<EntityType>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo).update(captor.capture());
		captor.getValue().count(); // process all entities in stream
	}

	@Test
	public void addEntityValid()
	{
		EntityType entityType = mock(EntityType.class);
		doNothing().when(entityTypeValidator).validate(entityType);
		entityTypeRepoValidationDecorator.add(entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void addEntityInvalid()
	{
		EntityType entityType = mock(EntityType.class);
		doThrow(mock(MolgenisValidationException.class)).when(entityTypeValidator).validate(entityType);
		entityTypeRepoValidationDecorator.add(entityType);
	}

	@Test
	public void addEntityStreamValid()
	{
		EntityType entityType0 = mock(EntityType.class);
		EntityType entityType1 = mock(EntityType.class);
		doNothing().when(entityTypeValidator).validate(entityType0);
		doNothing().when(entityTypeValidator).validate(entityType1);
		entityTypeRepoValidationDecorator.add(Stream.of(entityType0, entityType1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<EntityType>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo).add(captor.capture());
		captor.getValue().count(); // process all entities in stream
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void addEntityStreamInvalid()
	{
		EntityType entityType0 = mock(EntityType.class);
		EntityType entityType1 = mock(EntityType.class);
		doNothing().when(entityTypeValidator).validate(entityType0);
		doThrow(mock(MolgenisValidationException.class)).when(entityTypeValidator).validate(entityType1);
		entityTypeRepoValidationDecorator.add(Stream.of(entityType0, entityType1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<EntityType>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo).add(captor.capture());
		captor.getValue().count(); // process all entities in stream
	}
}