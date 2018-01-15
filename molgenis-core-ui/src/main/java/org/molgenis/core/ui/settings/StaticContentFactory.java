package org.molgenis.core.ui.settings;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class StaticContentFactory extends AbstractSystemEntityFactory<StaticContent, StaticContentMeta, String>
{
	public StaticContentFactory(StaticContentMeta staticContentMeta, EntityPopulator entityPopulator)
	{
		super(StaticContent.class, staticContentMeta, entityPopulator);
	}
}
