package org.molgenis.data.semantic;

import org.molgenis.data.AttributeMetaData;

public interface TagService<ObjectType>
{
	// do this first!
	Iterable<Tag<AttributeMetaData, ObjectType>> getTagsForAttribute(AttributeMetaData attributeMetaData);

	void getTagsForEntity();

	void getTagsForPackage();

	// and this
	void addTag(AttributeMetaData attribute, ObjectType tags, Relation relation);

	// and this
	void removeTag(Tag<?, ObjectType> tag);
}
