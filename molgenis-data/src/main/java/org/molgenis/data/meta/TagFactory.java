package org.molgenis.data.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TagFactory extends AbstractSystemEntityFactory<Tag, TagMetaData, String>
{
	@Autowired
	TagFactory(TagMetaData tagMetaData)
	{
		super(Tag.class, tagMetaData);
	}
}
