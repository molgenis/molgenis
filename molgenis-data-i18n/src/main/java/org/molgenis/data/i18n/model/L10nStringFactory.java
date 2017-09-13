package org.molgenis.data.i18n.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class L10nStringFactory extends AbstractSystemEntityFactory<L10nString, L10nStringMetaData, String>
{
	L10nStringFactory(L10nStringMetaData l10nStringMetaData, EntityPopulator entityPopulator)
	{
		super(L10nString.class, l10nStringMetaData, entityPopulator);
	}
}
