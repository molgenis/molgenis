package org.molgenis.data.validation.meta;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.data.validation.constraint.EntityTypeValidationResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.EnumSet;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.molgenis.data.validation.constraint.EntityTypeConstraint.NAME;

public class EntityTypeRepositoryValidationDecoratorTest
{
	private EntityTypeRepositoryValidationDecorator entityTypeRepoValidationDecorator;
	private Repository<EntityType> delegateRepository;
	private EntityTypeValidator entityTypeValidator;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		delegateRepository = mock(Repository.class);
		entityTypeValidator = mock(EntityTypeValidator.class);
		entityTypeRepoValidationDecorator = new EntityTypeRepositoryValidationDecorator(delegateRepository,
				entityTypeValidator);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void EntityTypeRepositoryValidationDecorator()
	{
		new EntityTypeRepositoryValidationDecorator(null, null);
	}

	@Test
	public void updateEntityValid()
	{
		EntityType entityType = mock(EntityType.class);
		doReturn(EntityTypeValidationResult.create(entityType)).when(entityTypeValidator).validate(entityType);
		entityTypeRepoValidationDecorator.update(entityType);
	}

	@Test(expectedExceptions = ValidationException.class)
	public void updateEntityInvalid() throws Exception
	{
		EntityType entityType = mock(EntityType.class);
		doReturn(EntityTypeValidationResult.create(entityType, EnumSet.of(NAME))).when(entityTypeValidator)
																				 .validate(entityType);
		entityTypeRepoValidationDecorator.update(entityType);
	}

	@Test
	public void updateEntityStreamValid()
	{
		EntityType entityType0 = mock(EntityType.class);
		EntityType entityType1 = mock(EntityType.class);
		doReturn(EntityTypeValidationResult.create(entityType0)).when(entityTypeValidator).validate(entityType0);
		doReturn(EntityTypeValidationResult.create(entityType1)).when(entityTypeValidator).validate(entityType1);
		entityTypeRepoValidationDecorator.update(Stream.of(entityType0, entityType1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<EntityType>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).update(captor.capture());
		captor.getValue().count(); // process all entities in stream
	}

	@Test(expectedExceptions = ValidationException.class)
	public void updateEntityStreamInvalid()
	{
		EntityType entityType0 = mock(EntityType.class);
		EntityType entityType1 = mock(EntityType.class);
		doReturn(EntityTypeValidationResult.create(entityType0)).when(entityTypeValidator).validate(entityType0);
		doReturn(EntityTypeValidationResult.create(entityType1, EnumSet.of(NAME))).when(entityTypeValidator)
																				  .validate(entityType1);
		entityTypeRepoValidationDecorator.update(Stream.of(entityType0, entityType1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<EntityType>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).update(captor.capture());
		captor.getValue().count(); // process all entities in stream
	}

	@Test
	public void addEntityValid()
	{
		EntityType entityType = mock(EntityType.class);
		doReturn(EntityTypeValidationResult.create(entityType)).when(entityTypeValidator).validate(entityType);
		entityTypeRepoValidationDecorator.add(entityType);
	}

	@Test(expectedExceptions = ValidationException.class)
	public void addEntityInvalid()
	{
		EntityType entityType = mock(EntityType.class);
		doReturn(EntityTypeValidationResult.create(entityType, EnumSet.of(NAME))).when(entityTypeValidator)
																				 .validate(entityType);
		entityTypeRepoValidationDecorator.add(entityType);
	}

	@Test
	public void addEntityStreamValid()
	{
		EntityType entityType0 = mock(EntityType.class);
		EntityType entityType1 = mock(EntityType.class);
		doReturn(EntityTypeValidationResult.create(entityType0)).when(entityTypeValidator).validate(entityType0);
		doReturn(EntityTypeValidationResult.create(entityType1)).when(entityTypeValidator).validate(entityType1);
		entityTypeRepoValidationDecorator.add(Stream.of(entityType0, entityType1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<EntityType>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).add(captor.capture());
		captor.getValue().count(); // process all entities in stream
	}

	@Test(expectedExceptions = ValidationException.class)
	public void addEntityStreamInvalid()
	{
		EntityType entityType0 = mock(EntityType.class);
		EntityType entityType1 = mock(EntityType.class);
		doReturn(EntityTypeValidationResult.create(entityType0)).when(entityTypeValidator).validate(entityType0);
		doReturn(EntityTypeValidationResult.create(entityType1, EnumSet.of(NAME))).when(entityTypeValidator)
																				  .validate(entityType1);
		entityTypeRepoValidationDecorator.add(Stream.of(entityType0, entityType1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<EntityType>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).add(captor.capture());
		captor.getValue().count(); // process all entities in stream
	}
}