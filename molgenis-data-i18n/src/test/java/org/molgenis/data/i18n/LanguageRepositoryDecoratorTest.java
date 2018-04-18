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
import static org.molgenis.i18n.LanguageService.LANGUAGE_CODE_EN;
import static org.molgenis.i18n.LanguageService.LANGUAGE_CODE_NL;
import static org.testng.Assert.assertEquals;

public class LanguageRepositoryDecoratorTest extends AbstractMockitoTest
{
	private static final String MESSAGE_ADD_NOT_ALLOWED = "Adding languages is not allowed";
	private static final String MESSAGE_DELETE_NOT_ALLOWED = "Deleting languages is not allowed";

	@Mock
	private Repository<Language> delegateRepository;

	private LanguageRepositoryDecorator languageRepositoryDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		languageRepositoryDecorator = new LanguageRepositoryDecorator(delegateRepository);
	}

	@Test
	public void testAddExistingLanguage()
	{
		Language language = getMockLanguage(LANGUAGE_CODE_NL);
		languageRepositoryDecorator.add(Stream.of(language));
		verify(delegateRepository).add(language);
	}

	@SuppressWarnings("deprecation")
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
		verify(delegateRepository).add(language0);
		verify(delegateRepository).add(language1);
	}

	@SuppressWarnings("deprecation")
	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = MESSAGE_ADD_NOT_ALLOWED)
	public void testAddStreamUnknownLanguage()
	{
		Language language = mock(Language.class);
		when(language.getCode()).thenReturn("unknownLanguage");
		languageRepositoryDecorator.add(Stream.of(language));
	}

	@SuppressWarnings("deprecation")
	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = MESSAGE_DELETE_NOT_ALLOWED)
	public void testDelete()
	{
		languageRepositoryDecorator.delete(mock(Language.class));
	}

	@SuppressWarnings("deprecation")
	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = MESSAGE_DELETE_NOT_ALLOWED)
	public void testDeleteById()
	{
		Language language = mock(Language.class);
		when(delegateRepository.findOneById(LANGUAGE_CODE_NL)).thenReturn(language);
		languageRepositoryDecorator.deleteById(LANGUAGE_CODE_NL);
	}

	@SuppressWarnings("deprecation")
	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = MESSAGE_DELETE_NOT_ALLOWED)
	public void testDeleteStream()
	{
		languageRepositoryDecorator.delete(Stream.of(mock(Language.class)));
	}

	private Language getMockLanguage(String languageCode)
	{
		Language language = mock(Language.class);
		doReturn(languageCode).when(language).getCode();
		return language;
	}
}
