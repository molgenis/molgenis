package org.molgenis.data.meta;

import org.molgenis.data.AbstractEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TagFactory extends AbstractEntityFactory<Tag, TagMetaData, String>
{
	@Autowired
	TagFactory(TagMetaData tagMetaData)
	{
		super(Tag.class, tagMetaData, String.class);
	}
}
