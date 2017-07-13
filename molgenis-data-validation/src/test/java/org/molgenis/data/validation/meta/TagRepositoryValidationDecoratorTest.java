package org.molgenis.data.validation.meta;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.validation.MolgenisValidationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * Created by Dennis on 11/24/2016.
 */
public class TagRepositoryValidationDecoratorTest
{

	private TagRepositoryValidationDecorator tagRepositoryValidationDecorator;
	private Repository<Tag> decoratedRepo;
	private TagValidator tagValidator;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepo = mock(Repository.class);
		tagValidator = mock(TagValidator.class);
		tagRepositoryValidationDecorator = new TagRepositoryValidationDecorator(decoratedRepo, tagValidator);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void tagRepositoryValidationDecorator()
	{
		new TagRepositoryValidationDecorator(null, null);
	}

	@Test
	public void testDelegate() throws Exception
	{
		assertEquals(tagRepositoryValidationDecorator.delegate(), decoratedRepo);
	}

	@Test
	public void testUpdateValid() throws Exception
	{
		Tag tag = mock(Tag.class);
		doNothing().when(tagValidator).validate(tag);
		tagRepositoryValidationDecorator.update(tag);
		verify(tagValidator).validate(tag);
		verify(decoratedRepo).update(tag);
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void testUpdateInvalid() throws Exception
	{
		Tag tag = mock(Tag.class);
		doThrow(mock(MolgenisValidationException.class)).when(tagValidator).validate(tag);
		tagRepositoryValidationDecorator.update(tag);
	}

	@Test
	public void testAddValid() throws Exception
	{
		Tag tag = mock(Tag.class);
		doNothing().when(tagValidator).validate(tag);
		tagRepositoryValidationDecorator.add(tag);
		verify(tagValidator).validate(tag);
		verify(decoratedRepo).add(tag);
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void testAddInValid() throws Exception
	{
		Tag tag = mock(Tag.class);
		doThrow(mock(MolgenisValidationException.class)).when(tagValidator).validate(tag);
		tagRepositoryValidationDecorator.add(tag);
	}

	@Test
	public void testUpdateStreamValid() throws Exception
	{
		Tag tag0 = mock(Tag.class);
		Tag tag1 = mock(Tag.class);
		doNothing().when(tagValidator).validate(tag0);
		doNothing().when(tagValidator).validate(tag1);
		tagRepositoryValidationDecorator.update(Stream.of(tag0, tag1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Tag>> tagCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo).update(tagCaptor.capture());
		assertEquals(tagCaptor.getValue().collect(toList()), asList(tag0, tag1));
		verify(tagValidator).validate(tag0);
		verify(tagValidator).validate(tag1);
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void testUpdateStreamInvalid() throws Exception
	{
		Tag tag0 = mock(Tag.class);
		Tag tag1 = mock(Tag.class);
		doNothing().when(tagValidator).validate(tag0);
		doThrow(mock(MolgenisValidationException.class)).when(tagValidator).validate(tag1);
		tagRepositoryValidationDecorator.update(Stream.of(tag0, tag1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Tag>> tagCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo).update(tagCaptor.capture());
		tagCaptor.getValue().count(); // consume stream
	}

	@Test
	public void testAddStreamValid() throws Exception
	{
		Tag tag0 = mock(Tag.class);
		Tag tag1 = mock(Tag.class);
		doNothing().when(tagValidator).validate(tag0);
		doNothing().when(tagValidator).validate(tag1);
		tagRepositoryValidationDecorator.add(Stream.of(tag0, tag1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Tag>> tagCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo).add(tagCaptor.capture());
		assertEquals(tagCaptor.getValue().collect(toList()), asList(tag0, tag1));
		verify(tagValidator).validate(tag0);
		verify(tagValidator).validate(tag1);
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void testAddStreamInvalid() throws Exception
	{
		Tag tag0 = mock(Tag.class);
		Tag tag1 = mock(Tag.class);
		doNothing().when(tagValidator).validate(tag0);
		doThrow(mock(MolgenisValidationException.class)).when(tagValidator).validate(tag1);
		tagRepositoryValidationDecorator.add(Stream.of(tag0, tag1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Tag>> tagCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo).add(tagCaptor.capture());
		tagCaptor.getValue().count(); // consume stream
	}
}