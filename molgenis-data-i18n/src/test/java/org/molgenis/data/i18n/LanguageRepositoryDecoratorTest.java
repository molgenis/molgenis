package org.molgenis.data.i18n;

import org.mockito.Mock;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.i18n.model.Language;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.molgenis.data.i18n.LanguageService.LANGUAGE_CODE_EN;
import static org.molgenis.data.i18n.LanguageService.LANGUAGE_CODE_NL;
import static org.testng.Assert.assertEquals;

public class LanguageRepositoryDecoratorTest extends AbstractMockitoTest
{
	private static final String MESSAGE_ADD_NOT_ALLOWED = "Adding languages is not allowed";
	private static final String MESSAGE_DELETE_NOT_ALLOWED = "Deleting languages is not allowed";

	@Mock
	private Repository<Language> decoratedRepo;
	@Mock
	private LanguageService languageService;

	private LanguageRepositoryDecorator languageRepositoryDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		languageRepositoryDecorator = new LanguageRepositoryDecorator(decoratedRepo, languageService);
	}

	@Test
	public void testAddExistingLanguage()
	{
		Language language = getMockLanguage(LANGUAGE_CODE_NL);
		languageRepositoryDecorator.add(Stream.of(language));
		verify(decoratedRepo).add(language);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = MESSAGE_ADD_NOT_ALLOWED)
	public void testAddUnknownLanguage()
	{
		Language language = getMockLanguage("unknownLanguage");
		languageRepositoryDecorator.add(Stream.of(language));
	}

	@Test
	public void testAddStreamExistingLanguages()
	{
		Language language0 = getMockLanguage(LANGUAGE_CODE_EN);
		Language language1 = getMockLanguage(LANGUAGE_CODE_NL);
		Integer count = languageRepositoryDecorator.add(Stream.of(language0, language1));
		assertEquals(count, Integer.valueOf(2));
		verify(decoratedRepo).add(language0);
		verify(decoratedRepo).add(language1);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = MESSAGE_ADD_NOT_ALLOWED)
	public void testAddStreamUnknownLanguage()
	{
		Language language = mock(Language.class);
		when(language.getCode()).thenReturn("unknownLanguage");
		languageRepositoryDecorator.add(Stream.of(language));
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = MESSAGE_DELETE_NOT_ALLOWED)
	public void testDelete()
	{
		languageRepositoryDecorator.delete(getMockLanguage(LANGUAGE_CODE_NL));
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = MESSAGE_DELETE_NOT_ALLOWED)
	public void testDeleteById()
	{
		Language language = getMockLanguage(LANGUAGE_CODE_NL);
		when(decoratedRepo.findOneById(LANGUAGE_CODE_NL)).thenReturn(language);
		languageRepositoryDecorator.deleteById(LANGUAGE_CODE_NL);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = MESSAGE_DELETE_NOT_ALLOWED)
	public void testDeleteStream()
	{
		languageRepositoryDecorator.delete(Stream.of(getMockLanguage(LANGUAGE_CODE_NL)));
	}

	private Language getMockLanguage(String languageCode)
	{
		Language language = mock(Language.class);
		when(language.getCode()).thenReturn(languageCode);
		return language;
	}
}
