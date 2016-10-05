package org.molgenis.ui.settings;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StaticContentFactory extends AbstractSystemEntityFactory<StaticContent, StaticContentMeta, String>
{
	@Autowired
	public StaticContentFactory(StaticContentMeta staticContentMeta, EntityPopulator entityPopulator)
	{
		super(StaticContent.class, staticContentMeta, entityPopulator);
	}
}
