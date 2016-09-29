package org.molgenis.data.i18n.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class I18nStringFactory extends AbstractSystemEntityFactory<I18nString, I18nStringMetaData, String>
{
	@Autowired
	I18nStringFactory(I18nStringMetaData i18nStringMeta, EntityPopulator entityPopulator)
	{
		super(I18nString.class, i18nStringMeta, entityPopulator);
	}
}
