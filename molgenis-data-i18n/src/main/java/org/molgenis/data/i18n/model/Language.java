package org.molgenis.data.i18n.model;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
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
	 * @param entityType language meta data
	 */
	public Language(EntityType entityType)
	{
		super(entityType);
	}

	/**
	 * Constructs a language with the given type code and meta data
	 *
	 * @param code       language code
	 * @param entityType language meta data
	 */
	public Language(String code, EntityType entityType)
	{
		super(entityType);
		setCode(code);
	}

	public String getCode()
	{
		return getString(LanguageMetadata.CODE);
	}

	public Language setCode(String code)
	{
		set(LanguageMetadata.CODE, code);
		return this;
	}

	public String getName()
	{
		return getString(LanguageMetadata.NAME);
	}

	public Language setName(String name)
	{
		set(LanguageMetadata.NAME, name);
		return this;
	}

	public boolean isActive()
	{
		return getBoolean(LanguageMetadata.ACTIVE);
	}

	public Language setActive(boolean active)
	{
		set(LanguageMetadata.ACTIVE, active);
		return this;
	}
}
