package org.molgenis.data.validation.meta;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.data.validation.constraint.AttributeConstraintViolation;
import org.molgenis.data.validation.meta.AttributeValidator.ValidationMode;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static freemarker.template.utility.Collections12.singletonList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;

public class AttributeRepositoryValidationDecoratorTest
{
	private AttributeRepositoryValidationDecorator attributeRepoValidationDecorator;
	private Repository<Attribute> delegateRepository;
	private AttributeValidator attributeValidator;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		delegateRepository = mock(Repository.class);
		attributeValidator = mock(AttributeValidator.class);
		attributeRepoValidationDecorator = new AttributeRepositoryValidationDecorator(delegateRepository,
				attributeValidator);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void attributeRepositoryValidationDecorator()
	{
		new AttributeRepositoryValidationDecorator(null, null);
	}

	@Test
	public void updateAttributeValid()
	{
		Attribute attribute = mock(Attribute.class);
		doReturn(emptyList()).when(attributeValidator).validate(attribute, ValidationMode.UPDATE);
		attributeRepoValidationDecorator.update(attribute);
		verify(attributeValidator, times(1)).validate(attribute, ValidationMode.UPDATE);
		verify(delegateRepository, times(1)).update(attribute);
	}

	@Test(expectedExceptions = ValidationException.class)
	public void updateEntityInvalid() throws Exception
	{
		Attribute attribute = mock(Attribute.class);
		doReturn(singletonList(mock(AttributeConstraintViolation.class))).when(attributeValidator)
																		 .validate(attribute, ValidationMode.UPDATE);
		attributeRepoValidationDecorator.update(attribute);
		verify(attributeValidator, times(1)).validate(attribute, ValidationMode.UPDATE);
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Test
	public void updateEntityStreamValid()
	{
		Attribute attribute0 = mock(Attribute.class);
		Attribute attribute1 = mock(Attribute.class);
		doReturn(emptyList()).when(attributeValidator).validate(attribute0, ValidationMode.UPDATE);
		doReturn(emptyList()).when(attributeValidator).validate(attribute1, ValidationMode.UPDATE);
		attributeRepoValidationDecorator.update(Stream.of(attribute0, attribute1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Attribute>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).update(captor.capture());
		captor.getValue().count(); // process all entities in stream
		verify(attributeValidator, times(2)).validate(any(Attribute.class), eq(ValidationMode.UPDATE));
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Test(expectedExceptions = ValidationException.class)
	public void updateEntityStreamInvalid()
	{
		Attribute attribute0 = mock(Attribute.class);
		Attribute attribute1 = mock(Attribute.class);
		doReturn(emptyList()).when(attributeValidator).validate(attribute0, ValidationMode.UPDATE);
		doReturn(singletonList(mock(AttributeConstraintViolation.class))).when(attributeValidator)
																		 .validate(attribute1, ValidationMode.UPDATE);
		attributeRepoValidationDecorator.update(Stream.of(attribute0, attribute1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Attribute>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).update(captor.capture());
		captor.getValue().count(); // process all entities in stream
		verify(attributeValidator, times(1)).validate(any(Attribute.class), ValidationMode.UPDATE);
	}

	@Test
	public void addEntityValid()
	{
		Attribute attribute = mock(Attribute.class);
		doReturn(emptyList()).when(attributeValidator).validate(attribute, ValidationMode.ADD);
		attributeRepoValidationDecorator.add(attribute);
		verify(attributeValidator, times(1)).validate(attribute, ValidationMode.ADD);
	}

	@Test(expectedExceptions = ValidationException.class)
	public void addEntityInvalid()
	{
		Attribute attribute = mock(Attribute.class);
		doReturn(singletonList(mock(AttributeConstraintViolation.class))).when(attributeValidator)
																		 .validate(attribute, ValidationMode.ADD);
		attributeRepoValidationDecorator.add(attribute);
		verify(attributeValidator, times(1)).validate(attribute, ValidationMode.ADD);
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Test
	public void addEntityStreamValid()
	{
		Attribute attribute0 = mock(Attribute.class);
		Attribute attribute1 = mock(Attribute.class);
		doReturn(emptyList()).when(attributeValidator).validate(attribute0, ValidationMode.ADD);
		doReturn(emptyList()).when(attributeValidator).validate(attribute1, ValidationMode.ADD);
		attributeRepoValidationDecorator.add(Stream.of(attribute0, attribute1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Attribute>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).add(captor.capture());
		captor.getValue().count(); // process all entities in stream
		verify(attributeValidator, times(2)).validate(any(Attribute.class), eq(ValidationMode.ADD));
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Test(expectedExceptions = ValidationException.class)
	public void addEntityStreamInvalid()
	{
		Attribute attribute0 = mock(Attribute.class);
		Attribute attribute1 = mock(Attribute.class);
		doReturn(emptyList()).when(attributeValidator).validate(attribute0, ValidationMode.ADD);
		doReturn(singletonList(mock(AttributeConstraintViolation.class))).when(attributeValidator)
																		 .validate(attribute1, ValidationMode.ADD);
		attributeRepoValidationDecorator.add(Stream.of(attribute0, attribute1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Attribute>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).add(captor.capture());
		captor.getValue().count(); // process all entities in stream
		verify(attributeValidator, times(1)).validate(any(Attribute.class), ValidationMode.ADD);
	}
}