package org.molgenis.data.i18n;

import static java.lang.Integer.valueOf;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.util.i18n.LanguageService.LANGUAGE_CODE_EN;
import static org.molgenis.util.i18n.LanguageService.LANGUAGE_CODE_NL;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.Repository;
import org.molgenis.data.i18n.model.Language;
import org.molgenis.test.AbstractMockitoTest;

class LanguageRepositoryDecoratorTest extends AbstractMockitoTest {
  @Mock private Repository<Language> delegateRepository;
  private LanguageRepositoryDecorator languageRepositoryDecorator;

  @BeforeEach
  void setUpBeforeMethod() {
    languageRepositoryDecorator = new LanguageRepositoryDecorator(delegateRepository);
  }

  @Test
  void testAddExistingLanguage() {
    Language language = getMockLanguage(LANGUAGE_CODE_NL);
    languageRepositoryDecorator.add(language);
    verify(delegateRepository).add(language);
  }

  @Test
  void testAddUnknownLanguage() {
    Language language = getMockLanguage("unknownLanguage");
    assertThrows(
        LanguageModificationException.class, () -> languageRepositoryDecorator.add(language));
  }

  @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
  @Test
  void testAddStreamExistingLanguages() {
    Language language0 = getMockLanguage(LANGUAGE_CODE_EN);
    Language language1 = getMockLanguage(LANGUAGE_CODE_NL);
    doAnswer(
            invocation -> {
              ((Stream<Language>) invocation.getArguments()[0]).collect(toList());
              return 2;
            })
        .when(delegateRepository)
        .add(any(Stream.class));
    assertEquals(valueOf(2), languageRepositoryDecorator.add(of(language0, language1)));
  }

  @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
  @Test
  void testAddStreamUnknownLanguage() {
    Language language = getMockLanguage("unknownLanguage");
    doAnswer(
            invocation -> {
              ((Stream<Language>) invocation.getArguments()[0]).collect(toList());
              return 1;
            })
        .when(delegateRepository)
        .add(any(Stream.class));
    assertThrows(
        LanguageModificationException.class,
        () -> languageRepositoryDecorator.add(Stream.of(language)));
  }

  @Test
  void testDelete() {
    assertThrows(
        LanguageModificationException.class,
        () -> languageRepositoryDecorator.delete(mock(Language.class)));
  }

  @Test
  void testDeleteById() {
    Language language = mock(Language.class);
    when(delegateRepository.findOneById(LANGUAGE_CODE_NL)).thenReturn(language);
    assertThrows(
        LanguageModificationException.class,
        () -> languageRepositoryDecorator.deleteById(LANGUAGE_CODE_NL));
  }

  @Test
  void testDeleteStream() {
    assertThrows(
        LanguageModificationException.class,
        () -> languageRepositoryDecorator.delete(Stream.of(mock(Language.class))));
  }

  private Language getMockLanguage(String languageCode) {
    Language language = mock(Language.class);
    doReturn(languageCode).when(language).getCode();
    return language;
  }
}
