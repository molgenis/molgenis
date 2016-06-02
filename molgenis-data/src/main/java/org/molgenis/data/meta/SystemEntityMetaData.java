package org.molgenis.data.meta;

/**
 * Created by Dennis on 4/22/2016.
 */
public interface SystemEntityMetaData extends EntityMetaData
{
	void bootstrap(EntityMetaDataMetaData entityMetaDataMetaData);

	@Deprecated
	void addAttribute(AttributeMetaData attr, EntityMetaDataImpl.AttributeRole... attrTypes);

	AttributeMetaData addAttribute(String name, AttributeRole... attrTypes);

	AttributeMetaData addAttribute(String name, AttributeMetaData parentAttr, AttributeRole... attrTypes);
}
