package org.molgenis.data.meta.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class TagFactory extends AbstractSystemEntityFactory<Tag, TagMetadata, String>
{
	TagFactory(TagMetadata tagMetadata, EntityPopulator entityPopulator)
	{
		super(Tag.class, tagMetadata, entityPopulator);
	}
}
