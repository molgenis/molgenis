package org.molgenis.data.validation.meta;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.validation.MolgenisValidationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class EntityTypeRepositoryValidationDecoratorTest
{
	private EntityTypeRepositoryValidationDecorator entityMetaRepoValidationDecorator;
	private Repository<EntityMetaData> decoratedRepo;
	private EntityMetaDataValidator entityMetaDataValidator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		//noinspection unchecked
		decoratedRepo = mock(Repository.class);
		entityMetaDataValidator = mock(EntityMetaDataValidator.class);
		entityMetaRepoValidationDecorator = new EntityTypeRepositoryValidationDecorator(decoratedRepo,
				entityMetaDataValidator);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void EntityMetaDataRepositoryValidationDecorator()
	{
		new EntityTypeRepositoryValidationDecorator(null, null);
	}

	@Test
	public void delegate()
	{
		assertEquals(entityMetaRepoValidationDecorator.delegate(), decoratedRepo);
	}

	@Test
	public void updateEntityValid()
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		doNothing().when(entityMetaDataValidator).validate(entityMeta);
		entityMetaRepoValidationDecorator.update(entityMeta);
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void updateEntityInvalid() throws Exception
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		doThrow(mock(MolgenisValidationException.class)).when(entityMetaDataValidator).validate(entityMeta);
		entityMetaRepoValidationDecorator.update(entityMeta);
	}

	@Test
	public void updateEntityStreamValid()
	{
		EntityMetaData entityMeta0 = mock(EntityMetaData.class);
		EntityMetaData entityMeta1 = mock(EntityMetaData.class);
		doNothing().when(entityMetaDataValidator).validate(entityMeta0);
		doNothing().when(entityMetaDataValidator).validate(entityMeta1);
		entityMetaRepoValidationDecorator.update(Stream.of(entityMeta0, entityMeta1));
		//noinspection unchecked
		ArgumentCaptor<Stream<EntityMetaData>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(decoratedRepo).update(captor.capture());
		captor.getValue().count(); // process all entities in stream
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void updateEntityStreamInvalid()
	{
		EntityMetaData entityMeta0 = mock(EntityMetaData.class);
		EntityMetaData entityMeta1 = mock(EntityMetaData.class);
		doNothing().when(entityMetaDataValidator).validate(entityMeta0);
		doThrow(mock(MolgenisValidationException.class)).when(entityMetaDataValidator).validate(entityMeta1);
		entityMetaRepoValidationDecorator.update(Stream.of(entityMeta0, entityMeta1));
		//noinspection unchecked
		ArgumentCaptor<Stream<EntityMetaData>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(decoratedRepo).update(captor.capture());
		captor.getValue().count(); // process all entities in stream
	}

	@Test
	public void addEntityValid()
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		doNothing().when(entityMetaDataValidator).validate(entityMeta);
		entityMetaRepoValidationDecorator.add(entityMeta);
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void addEntityInvalid()
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		doThrow(mock(MolgenisValidationException.class)).when(entityMetaDataValidator).validate(entityMeta);
		entityMetaRepoValidationDecorator.add(entityMeta);
	}

	@Test
	public void addEntityStreamValid()
	{
		EntityMetaData entityMeta0 = mock(EntityMetaData.class);
		EntityMetaData entityMeta1 = mock(EntityMetaData.class);
		doNothing().when(entityMetaDataValidator).validate(entityMeta0);
		doNothing().when(entityMetaDataValidator).validate(entityMeta1);
		entityMetaRepoValidationDecorator.add(Stream.of(entityMeta0, entityMeta1));
		//noinspection unchecked
		ArgumentCaptor<Stream<EntityMetaData>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(decoratedRepo).add(captor.capture());
		captor.getValue().count(); // process all entities in stream
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void addEntityStreamInvalid()
	{
		EntityMetaData entityMeta0 = mock(EntityMetaData.class);
		EntityMetaData entityMeta1 = mock(EntityMetaData.class);
		doNothing().when(entityMetaDataValidator).validate(entityMeta0);
		doThrow(mock(MolgenisValidationException.class)).when(entityMetaDataValidator).validate(entityMeta1);
		entityMetaRepoValidationDecorator.add(Stream.of(entityMeta0, entityMeta1));
		//noinspection unchecked
		ArgumentCaptor<Stream<EntityMetaData>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(decoratedRepo).add(captor.capture());
		captor.getValue().count(); // process all entities in stream
	}
}