package org.molgenis.data.validation.meta;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.data.validation.meta.AttributeValidator.ValidationMode;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class AttributeRepositoryValidationDecoratorTest
{
	private AttributeRepositoryValidationDecorator attributeRepoValidationDecorator;
	private Repository<Attribute> decoratedRepo;
	private AttributeValidator attributeValidator;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
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
		doNothing().when(attributeValidator).validate(attribute, ValidationMode.UPDATE);
		attributeRepoValidationDecorator.update(attribute);
		verify(attributeValidator, times(1)).validate(attribute, ValidationMode.UPDATE);
		verify(decoratedRepo, times(1)).update(attribute);
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void updateEntityInvalid() throws Exception
	{
		Attribute attribute = mock(Attribute.class);
		doThrow(mock(MolgenisValidationException.class)).when(attributeValidator)
														.validate(attribute, ValidationMode.UPDATE);
		attributeRepoValidationDecorator.update(attribute);
		verify(attributeValidator, times(1)).validate(attribute, ValidationMode.UPDATE);
	}

	@Test
	public void updateEntityStreamValid()
	{
		Attribute attribute0 = mock(Attribute.class);
		Attribute attribute1 = mock(Attribute.class);
		doNothing().when(attributeValidator).validate(attribute0, ValidationMode.UPDATE);
		doNothing().when(attributeValidator).validate(attribute1, ValidationMode.UPDATE);
		attributeRepoValidationDecorator.update(Stream.of(attribute0, attribute1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Attribute>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo).update(captor.capture());
		captor.getValue().count(); // process all entities in stream
		verify(attributeValidator, times(2)).validate(any(Attribute.class), eq(ValidationMode.UPDATE));
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void updateEntityStreamInvalid()
	{
		Attribute attribute0 = mock(Attribute.class);
		Attribute attribute1 = mock(Attribute.class);
		doNothing().when(attributeValidator).validate(attribute0, ValidationMode.UPDATE);
		doThrow(mock(MolgenisValidationException.class)).when(attributeValidator)
														.validate(attribute1, ValidationMode.UPDATE);
		attributeRepoValidationDecorator.update(Stream.of(attribute0, attribute1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Attribute>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo).update(captor.capture());
		captor.getValue().count(); // process all entities in stream
		verify(attributeValidator, times(1)).validate(any(Attribute.class), ValidationMode.UPDATE);
	}

	@Test
	public void addEntityValid()
	{
		Attribute attribute = mock(Attribute.class);
		doNothing().when(attributeValidator).validate(attribute, ValidationMode.ADD);
		attributeRepoValidationDecorator.add(attribute);
		verify(attributeValidator, times(1)).validate(attribute, ValidationMode.ADD);
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void addEntityInvalid()
	{
		Attribute attribute = mock(Attribute.class);
		doThrow(mock(MolgenisValidationException.class)).when(attributeValidator)
														.validate(attribute, ValidationMode.ADD);
		attributeRepoValidationDecorator.add(attribute);
		verify(attributeValidator, times(1)).validate(attribute, ValidationMode.ADD);
	}

	@Test
	public void addEntityStreamValid()
	{
		Attribute attribute0 = mock(Attribute.class);
		Attribute attribute1 = mock(Attribute.class);
		doNothing().when(attributeValidator).validate(attribute0, ValidationMode.ADD);
		doNothing().when(attributeValidator).validate(attribute1, ValidationMode.ADD);
		attributeRepoValidationDecorator.add(Stream.of(attribute0, attribute1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Attribute>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo).add(captor.capture());
		captor.getValue().count(); // process all entities in stream
		verify(attributeValidator, times(2)).validate(any(Attribute.class), eq(ValidationMode.ADD));
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void addEntityStreamInvalid()
	{
		Attribute attribute0 = mock(Attribute.class);
		Attribute attribute1 = mock(Attribute.class);
		doNothing().when(attributeValidator).validate(attribute0, ValidationMode.ADD);
		doThrow(mock(MolgenisValidationException.class)).when(attributeValidator)
														.validate(attribute1, ValidationMode.ADD);
		attributeRepoValidationDecorator.add(Stream.of(attribute0, attribute1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Attribute>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo).add(captor.capture());
		captor.getValue().count(); // process all entities in stream
		verify(attributeValidator, times(1)).validate(any(Attribute.class), ValidationMode.ADD);
	}
}