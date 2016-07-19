package org.molgenis.data.i18n.model;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

/**
 * Language entity
 */
public class Language extends StaticEntity
{
	public Language(Entity entity)
	{
		super(entity);
	}

	/**
	 * Constructs a language with the given meta data
	 *
	 * @param entityMeta language meta data
	 */
	public Language(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	/**
	 * Constructs a language with the given type code and meta data
	 *
	 * @param code             language code
	 * @param entityMeta language meta data
	 */
	public Language(String code, EntityMetaData entityMeta)
	{
		super(entityMeta);
		setCode(code);
	}

	public String getCode()
	{
		return getString(LanguageMetaData.CODE);
	}

	public Language setCode(String code)
	{
		set(LanguageMetaData.CODE, code);
		return this;
	}

	public String getName()
	{
		return getString(LanguageMetaData.NAME);
	}

	public Language setName(String name)
	{
		set(LanguageMetaData.NAME, name);
		return this;
	}
}
