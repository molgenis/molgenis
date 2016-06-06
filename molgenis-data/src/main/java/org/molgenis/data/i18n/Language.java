package org.molgenis.data.i18n;

import static org.molgenis.data.i18n.LanguageMetaData.CODE;
import static org.molgenis.data.i18n.LanguageMetaData.LANGUAGE;
import static org.molgenis.data.i18n.LanguageMetaData.NAME;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.SystemEntity;

/**
 * Language entity
 */
public class Language extends SystemEntity
{
	/**
	 * Constructs a language based on the given entity
	 *
	 * @param entity decorated entity
	 */
	public Language(Entity entity)
	{
		super(entity, LANGUAGE);
	}

	/**
	 * Constructs a language with the given meta data
	 *
	 * @param languageMetaData language meta data
	 */
	public Language(LanguageMetaData languageMetaData)
	{
		super(languageMetaData);
	}

	/**
	 * Constructs a language with the given type code and meta data
	 *
	 * @param code             language code
	 * @param languageMetaData language meta data
	 */
	public Language(String code, LanguageMetaData languageMetaData)
	{
		super(languageMetaData);
		setCode(code);
	}

	public String getCode()
	{
		return getString(CODE);
	}

	public Language setCode(String code)
	{
		set(CODE, code);
		return this;
	}

	public String getName()
	{
		return getString(NAME);
	}

	public Language setName(String name)
	{
		set(NAME, name);
		return this;
	}
}
