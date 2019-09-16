package org.molgenis.data.validation.meta;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.validation.MolgenisValidationException;

/** Created by Dennis on 11/24/2016. */
class TagRepositoryValidationDecoratorTest {

  private TagRepositoryValidationDecorator tagRepositoryValidationDecorator;
  private Repository<Tag> delegateRepository;
  private TagValidator tagValidator;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUpBeforeMethod() {
    delegateRepository = mock(Repository.class);
    tagValidator = mock(TagValidator.class);
    tagRepositoryValidationDecorator =
        new TagRepositoryValidationDecorator(delegateRepository, tagValidator);
  }

  @Test
  void tagRepositoryValidationDecorator() {
    assertThrows(
        NullPointerException.class, () -> new TagRepositoryValidationDecorator(null, null));
  }

  @Test
  void testUpdateValid() {
    Tag tag = mock(Tag.class);
    doNothing().when(tagValidator).validate(tag);
    tagRepositoryValidationDecorator.update(tag);
    verify(tagValidator).validate(tag);
    verify(delegateRepository).update(tag);
  }

  @SuppressWarnings("deprecation")
  @Test
  void testUpdateInvalid() {
    Tag tag = mock(Tag.class);
    doThrow(mock(MolgenisValidationException.class)).when(tagValidator).validate(tag);
    assertThrows(
        MolgenisValidationException.class, () -> tagRepositoryValidationDecorator.update(tag));
  }

  @Test
  void testAddValid() {
    Tag tag = mock(Tag.class);
    doNothing().when(tagValidator).validate(tag);
    tagRepositoryValidationDecorator.add(tag);
    verify(tagValidator).validate(tag);
    verify(delegateRepository).add(tag);
  }

  @SuppressWarnings("deprecation")
  @Test
  void testAddInValid() {
    Tag tag = mock(Tag.class);
    doThrow(mock(MolgenisValidationException.class)).when(tagValidator).validate(tag);
    assertThrows(
        MolgenisValidationException.class, () -> tagRepositoryValidationDecorator.add(tag));
  }

  @Test
  void testUpdateStreamValid() {
    Tag tag0 = mock(Tag.class);
    Tag tag1 = mock(Tag.class);
    doNothing().when(tagValidator).validate(tag0);
    doNothing().when(tagValidator).validate(tag1);
    tagRepositoryValidationDecorator.update(Stream.of(tag0, tag1));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Tag>> tagCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).update(tagCaptor.capture());
    assertEquals(asList(tag0, tag1), tagCaptor.getValue().collect(toList()));
    verify(tagValidator).validate(tag0);
    verify(tagValidator).validate(tag1);
  }

  @SuppressWarnings("deprecation")
  @Test
  void testUpdateStreamInvalid() {
    Tag tag0 = mock(Tag.class);
    Tag tag1 = mock(Tag.class);
    doNothing().when(tagValidator).validate(tag0);
    doThrow(mock(MolgenisValidationException.class)).when(tagValidator).validate(tag1);
    tagRepositoryValidationDecorator.update(Stream.of(tag0, tag1));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Tag>> tagCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).update(tagCaptor.capture());
    assertThrows(
        MolgenisValidationException.class, () -> tagCaptor.getValue().count()); // consume stream
  }

  @Test
  void testAddStreamValid() {
    Tag tag0 = mock(Tag.class);
    Tag tag1 = mock(Tag.class);
    doNothing().when(tagValidator).validate(tag0);
    doNothing().when(tagValidator).validate(tag1);
    tagRepositoryValidationDecorator.add(Stream.of(tag0, tag1));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Tag>> tagCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).add(tagCaptor.capture());
    assertEquals(asList(tag0, tag1), tagCaptor.getValue().collect(toList()));
    verify(tagValidator).validate(tag0);
    verify(tagValidator).validate(tag1);
  }

  @Test
  void testAddStreamInvalid() {
    Tag tag0 = mock(Tag.class);
    Tag tag1 = mock(Tag.class);
    doNothing().when(tagValidator).validate(tag0);
    doThrow(mock(MolgenisValidationException.class)).when(tagValidator).validate(tag1);
    tagRepositoryValidationDecorator.add(Stream.of(tag0, tag1));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Tag>> tagCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).add(tagCaptor.capture());
    assertThrows(
        MolgenisValidationException.class, () -> tagCaptor.getValue().count()); // consume stream
  }
}
