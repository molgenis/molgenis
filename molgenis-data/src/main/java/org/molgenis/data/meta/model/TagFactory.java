package org.molgenis.data.meta.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TagFactory extends AbstractSystemEntityFactory<Tag, TagMetaData, String>
{
	@Autowired
	TagFactory(TagMetaData tagMetaData, EntityPopulator entityPopulator)
	{
		super(Tag.class, tagMetaData, entityPopulator);
	}
}
