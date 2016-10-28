package org.molgenis.data.validation.meta;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.validation.MolgenisValidationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class AttributeRepositoryValidationDecoratorTest
{
	private AttributeRepositoryValidationDecorator attributeRepoValidationDecorator;
	private Repository<Attribute> decoratedRepo;
	private AttributeValidator attributeValidator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		//noinspection unchecked
		decoratedRepo = mock(Repository.class);
		attributeValidator = mock(AttributeValidator.class);
		attributeRepoValidationDecorator = new AttributeRepositoryValidationDecorator(decoratedRepo,
				attributeValidator);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void attributeRepositoryValidationDecorator()
	{
		new AttributeRepositoryValidationDecorator(null, null);
	}

	@Test
	public void delegate()
	{
		assertEquals(attributeRepoValidationDecorator.delegate(), decoratedRepo);
	}

	@Test
	public void updateAttributeValid()
	{
		Attribute attribute = mock(Attribute.class);
		doNothing().when(attributeValidator).validateUpdate(attribute);
		attributeRepoValidationDecorator.update(attribute);
		verify(attributeValidator, times(1)).validateUpdate(attribute);
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void updateEntityInvalid() throws Exception
	{
		Attribute attribute = mock(Attribute.class);
		doThrow(mock(MolgenisValidationException.class)).when(attributeValidator).validateUpdate(attribute);
		attributeRepoValidationDecorator.update(attribute);
		verify(attributeValidator, times(1)).validateUpdate(attribute);
	}

	@Test
	public void updateEntityStreamValid()
	{
		Attribute attribute0 = mock(Attribute.class);
		Attribute attribute1 = mock(Attribute.class);
		doNothing().when(attributeValidator).validateUpdate(attribute0);
		doNothing().when(attributeValidator).validateUpdate(attribute1);
		attributeRepoValidationDecorator.update(Stream.of(attribute0, attribute1));
		//noinspection unchecked
		ArgumentCaptor<Stream<Attribute>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(decoratedRepo).update(captor.capture());
		captor.getValue().count(); // process all entities in stream
		verify(attributeValidator, times(2)).validateUpdate(any(Attribute.class));
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void updateEntityStreamInvalid()
	{
		Attribute attribute0 = mock(Attribute.class);
		Attribute attribute1 = mock(Attribute.class);
		doNothing().when(attributeValidator).validateUpdate(attribute0);
		doThrow(mock(MolgenisValidationException.class)).when(attributeValidator).validateUpdate(attribute1);
		attributeRepoValidationDecorator.update(Stream.of(attribute0, attribute1));
		//noinspection unchecked
		ArgumentCaptor<Stream<Attribute>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(decoratedRepo).update(captor.capture());
		captor.getValue().count(); // process all entities in stream
		verify(attributeValidator, times(1)).validateUpdate(any(Attribute.class));
	}

	@Test
	public void addEntityValid()
	{
		Attribute attribute = mock(Attribute.class);
		doNothing().when(attributeValidator).validateAdd(attribute);
		attributeRepoValidationDecorator.add(attribute);
		verify(attributeValidator, times(1)).validateAdd(attribute);
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void addEntityInvalid()
	{
		Attribute attribute = mock(Attribute.class);
		doThrow(mock(MolgenisValidationException.class)).when(attributeValidator).validateAdd(attribute);
		attributeRepoValidationDecorator.add(attribute);
		verify(attributeValidator, times(1)).validateAdd(attribute);
	}

	@Test
	public void addEntityStreamValid()
	{
		Attribute attribute0 = mock(Attribute.class);
		Attribute attribute1 = mock(Attribute.class);
		doNothing().when(attributeValidator).validateAdd(attribute0);
		doNothing().when(attributeValidator).validateAdd(attribute1);
		attributeRepoValidationDecorator.add(Stream.of(attribute0, attribute1));
		//noinspection unchecked
		ArgumentCaptor<Stream<Attribute>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(decoratedRepo).add(captor.capture());
		captor.getValue().count(); // process all entities in stream
		verify(attributeValidator, times(2)).validateAdd(any(Attribute.class));
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void addEntityStreamInvalid()
	{
		Attribute attribute0 = mock(Attribute.class);
		Attribute attribute1 = mock(Attribute.class);
		doNothing().when(attributeValidator).validateAdd(attribute0);
		doThrow(mock(MolgenisValidationException.class)).when(attributeValidator).validateAdd(attribute1);
		attributeRepoValidationDecorator.add(Stream.of(attribute0, attribute1));
		//noinspection unchecked
		ArgumentCaptor<Stream<Attribute>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(decoratedRepo).add(captor.capture());
		captor.getValue().count(); // process all entities in stream
		verify(attributeValidator, times(1)).validateAdd(any(Attribute.class));
	}
}