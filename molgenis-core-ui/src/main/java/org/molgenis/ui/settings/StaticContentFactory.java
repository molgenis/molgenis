package org.molgenis.ui.settings;

import org.molgenis.data.AbstractEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StaticContentFactory extends AbstractEntityFactory<StaticContent, StaticContentMeta, String>
{
	@Autowired
	public StaticContentFactory(StaticContentMeta staticContentMeta)
	{
		super(StaticContent.class, staticContentMeta, String.class);
	}
}
