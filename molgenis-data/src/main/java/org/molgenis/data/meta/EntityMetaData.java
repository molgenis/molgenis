package org.molgenis.data.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.semantic.Tag;

/**
 * Created by Dennis on 4/22/2016.
 */
public interface EntityMetaData extends Entity
{
	String getName();

	EntityMetaData setName(String fullName);

	String getSimpleName();

	EntityMetaData setSimpleName(String simpleName);

	String getLabel();

	String getLabel(String languageCode);

	EntityMetaData setLabel(String label);

	EntityMetaData setLabel(String languageCode, String label);

	String getDescription();

	String getDescription(String languageCode);

	EntityMetaData setDescription(String description);

	EntityMetaData setDescription(String languageCode, String description);

	String getBackend();

	EntityMetaData setBackend(String backend);

	Package getPackage();

	EntityMetaData setPackage(Package package_);

	AttributeMetaData getIdAttribute();

	AttributeMetaData getOwnIdAttribute();

	EntityMetaData setIdAttribute(AttributeMetaData idAttr);

	AttributeMetaData getLabelAttribute();

	AttributeMetaData getLabelAttribute(String languageCode);

	AttributeMetaData getOwnLabelAttribute();

	AttributeMetaData getOwnLabelAttribute(String languageCode);

	EntityMetaData setLabelAttribute(AttributeMetaData labelAttr);

	AttributeMetaData getLookupAttribute(String lookupAttrName);

	Iterable<AttributeMetaData> getLookupAttributes();

	Iterable<AttributeMetaData> getOwnLookupAttributes();

	EntityMetaData setLookupAttributes(Iterable<AttributeMetaData> lookupAttrs);

	boolean isAbstract();

	EntityMetaData setAbstract(boolean abstract_);

	EntityMetaData getExtends();

	EntityMetaData setExtends(EntityMetaData extends_);

	Iterable<AttributeMetaData> getOwnAttributes();

	EntityMetaData setOwnAttributes(Iterable<AttributeMetaData> attrs);

	Iterable<AttributeMetaData> getAttributes();

	Iterable<AttributeMetaData> getAtomicAttributes();

	AttributeMetaData getAttribute(String attrName);

	void addAttribute(AttributeMetaData attr, AttributeRole... attrTypes);

	AttributeMetaData addAttribute(String name, AttributeRole... attrTypes);

	void addAttributes(Iterable<AttributeMetaData> attrs);

	boolean hasAttributeWithExpression();

	void removeAttribute(AttributeMetaData attr);

	void addLookupAttribute(AttributeMetaData lookupAttr);

	Iterable<Tag> getTags();

	EntityMetaData setTags(Iterable<Tag> tags);

	void addTag(Tag tag);

	void removeTag(Tag tag);

	Iterable<AttributeMetaData> getOwnAtomicAttributes();

	enum AttributeRole
	{
		ROLE_ID, ROLE_LABEL, ROLE_LOOKUP
	}
}
