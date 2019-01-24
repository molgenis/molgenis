package org.molgenis.data.i18n;

import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.i18n.LanguageService.LANGUAGE_CODE_EN;
import static org.molgenis.i18n.LanguageService.LANGUAGE_CODE_NL;
import static org.testng.Assert.assertEquals;

import java.util.stream.Stream;
import org.mockito.Mock;
import org.molgenis.data.Repository;
import org.molgenis.data.i18n.model.Language;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LanguageRepositoryDecoratorTest extends AbstractMockitoTest {
  @Mock private Repository<Language> delegateRepository;
  private LanguageRepositoryDecorator languageRepositoryDecorator;

  @BeforeMethod
  public void setUpBeforeMethod() {
    languageRepositoryDecorator = new LanguageRepositoryDecorator(delegateRepository);
  }

  @Test
  public void testAddExistingLanguage() {
    Language language = getMockLanguage(LANGUAGE_CODE_NL);
    languageRepositoryDecorator.add(language);
    verify(delegateRepository).add(language);
  }

  @Test(expectedExceptions = LanguageModificationException.class)
  public void testAddUnknownLanguage() {
    Language language = getMockLanguage("unknownLanguage");
    languageRepositoryDecorator.add(language);
  }

  @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
  @Test
  public void testAddStreamExistingLanguages() {
    Language language0 = getMockLanguage(LANGUAGE_CODE_EN);
    Language language1 = getMockLanguage(LANGUAGE_CODE_NL);
    doAnswer(
            invocation -> {
              ((Stream<Language>) invocation.getArguments()[0]).collect(toList());
              return 2;
            })
        .when(delegateRepository)
        .add(any(Stream.class));
    assertEquals(
        languageRepositoryDecorator.add(Stream.of(language0, language1)), Integer.valueOf(2));
  }

  @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
  @Test(expectedExceptions = LanguageModificationException.class)
  public void testAddStreamUnknownLanguage() {
    Language language = getMockLanguage("unknownLanguage");
    doAnswer(
            invocation -> {
              ((Stream<Language>) invocation.getArguments()[0]).collect(toList());
              return 1;
            })
        .when(delegateRepository)
        .add(any(Stream.class));
    languageRepositoryDecorator.add(Stream.of(language));
  }

  @Test(expectedExceptions = LanguageModificationException.class)
  public void testDelete() {
    languageRepositoryDecorator.delete(mock(Language.class));
  }

  @Test(expectedExceptions = LanguageModificationException.class)
  public void testDeleteById() {
    Language language = mock(Language.class);
    when(delegateRepository.findOneById(LANGUAGE_CODE_NL)).thenReturn(language);
    languageRepositoryDecorator.deleteById(LANGUAGE_CODE_NL);
  }

  @Test(expectedExceptions = LanguageModificationException.class)
  public void testDeleteStream() {
    languageRepositoryDecorator.delete(Stream.of(mock(Language.class)));
  }

  private Language getMockLanguage(String languageCode) {
    Language language = mock(Language.class);
    doReturn(languageCode).when(language).getCode();
    return language;
  }
}
