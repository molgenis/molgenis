package org.molgenis.data.validation.meta;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.data.validation.constraint.TagValidationResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.EnumSet;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.validation.constraint.TagConstraint.UNKNOWN_RELATION_IRI;
import static org.testng.Assert.assertEquals;

public class TagRepositoryValidationDecoratorTest
{
	private TagRepositoryValidationDecorator tagRepositoryValidationDecorator;
	private Repository<Tag> delegateRepository;
	private TagValidator tagValidator;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		delegateRepository = mock(Repository.class);
		tagValidator = mock(TagValidator.class);
		tagRepositoryValidationDecorator = new TagRepositoryValidationDecorator(delegateRepository, tagValidator);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void tagRepositoryValidationDecorator()
	{
		new TagRepositoryValidationDecorator(null, null);
	}

	@Test
	public void testUpdateValid() throws Exception
	{
		Tag tag = mock(Tag.class);
		doReturn(TagValidationResult.create(tag)).when(tagValidator).validate(tag);
		tagRepositoryValidationDecorator.update(tag);
		verify(tagValidator).validate(tag);
		verify(delegateRepository).update(tag);
	}

	@Test(expectedExceptions = ValidationException.class)
	public void testUpdateInvalid() throws Exception
	{
		Tag tag = mock(Tag.class);
		doReturn(TagValidationResult.create(tag, EnumSet.of(UNKNOWN_RELATION_IRI))).when(tagValidator).validate(tag);
		tagRepositoryValidationDecorator.update(tag);
	}

	@Test
	public void testAddValid() throws Exception
	{
		Tag tag = mock(Tag.class);
		doReturn(TagValidationResult.create(tag)).when(tagValidator).validate(tag);
		tagRepositoryValidationDecorator.add(tag);
		verify(tagValidator).validate(tag);
		verify(delegateRepository).add(tag);
	}

	@Test(expectedExceptions = ValidationException.class)
	public void testAddInValid() throws Exception
	{
		Tag tag = mock(Tag.class);
		doReturn(TagValidationResult.create(tag, EnumSet.of(UNKNOWN_RELATION_IRI))).when(tagValidator).validate(tag);
		tagRepositoryValidationDecorator.add(tag);
	}

	@Test
	public void testUpdateStreamValid() throws Exception
	{
		Tag tag0 = mock(Tag.class);
		Tag tag1 = mock(Tag.class);
		doReturn(TagValidationResult.create(tag0)).when(tagValidator).validate(tag0);
		doReturn(TagValidationResult.create(tag0)).when(tagValidator).validate(tag1);
		tagRepositoryValidationDecorator.update(Stream.of(tag0, tag1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Tag>> tagCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).update(tagCaptor.capture());
		assertEquals(tagCaptor.getValue().collect(toList()), asList(tag0, tag1));
		verify(tagValidator).validate(tag0);
		verify(tagValidator).validate(tag1);
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Test(expectedExceptions = ValidationException.class)
	public void testUpdateStreamInvalid() throws Exception
	{
		Tag tag0 = mock(Tag.class);
		Tag tag1 = mock(Tag.class);
		doReturn(TagValidationResult.create(tag0)).when(tagValidator).validate(tag0);
		doReturn(TagValidationResult.create(tag1, EnumSet.of(UNKNOWN_RELATION_IRI))).when(tagValidator).validate(tag1);
		tagRepositoryValidationDecorator.update(Stream.of(tag0, tag1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Tag>> tagCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).update(tagCaptor.capture());
		tagCaptor.getValue().count(); // consume stream
	}

	@Test
	public void testAddStreamValid() throws Exception
	{
		Tag tag0 = mock(Tag.class);
		Tag tag1 = mock(Tag.class);
		doReturn(TagValidationResult.create(tag0)).when(tagValidator).validate(tag0);
		doReturn(TagValidationResult.create(tag1)).when(tagValidator).validate(tag1);
		tagRepositoryValidationDecorator.add(Stream.of(tag0, tag1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Tag>> tagCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).add(tagCaptor.capture());
		assertEquals(tagCaptor.getValue().collect(toList()), asList(tag0, tag1));
		verify(tagValidator).validate(tag0);
		verify(tagValidator).validate(tag1);
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Test(expectedExceptions = ValidationException.class)
	public void testAddStreamInvalid() throws Exception
	{
		Tag tag0 = mock(Tag.class);
		Tag tag1 = mock(Tag.class);
		doReturn(TagValidationResult.create(tag0)).when(tagValidator).validate(tag0);
		doReturn(TagValidationResult.create(tag1, EnumSet.of(UNKNOWN_RELATION_IRI))).when(tagValidator).validate(tag1);
		tagRepositoryValidationDecorator.add(Stream.of(tag0, tag1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Tag>> tagCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).add(tagCaptor.capture());
		tagCaptor.getValue().count(); // consume stream
	}
}