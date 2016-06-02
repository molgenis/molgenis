package org.molgenis.data.i18n;

import org.molgenis.data.AbstractEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LanguageFactory extends AbstractEntityFactory<Language, LanguageMetaData, String>
{
	@Autowired
	LanguageFactory(LanguageMetaData languageMetaData)
	{
		super(Language.class, languageMetaData, String.class);
	}

	/**
	 * Creates a language with the given code and name
	 *
	 * @param code language code, e.g. "en"
	 * @param name language name, e.g. "English"
	 * @return new language
	 */
	public Language create(String code, String name)
	{
		Language language = super.create(code);
		language.setName(name);
		return language;
	}
}
