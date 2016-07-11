package org.molgenis.ui.settings;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StaticContentFactory extends AbstractSystemEntityFactory<StaticContent, StaticContentMeta, String>
{
	@Autowired
	public StaticContentFactory(StaticContentMeta staticContentMeta)
	{
		super(StaticContent.class, staticContentMeta);
	}
}
