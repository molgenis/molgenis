package org.molgenis.data.support;

import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;

public class OneToManyUtils
{
	private OneToManyUtils()
	{
	}

	public static String getManyToOneAttrName(AttributeMetaData manyToOneAttr)
	{
		return manyToOneAttr.getRefEntity().getName() + '_' + manyToOneAttr.getName();
	}

	public static String getManyToOneAttrName(EntityMetaData entityMeta, AttributeMetaData oneToManyAttr)
	{
		return getManyToOneAttrName(entityMeta, oneToManyAttr.getName());
	}

	public static String getManyToOneAttrName(EntityMetaData entityMeta, String oneToManyAttrName)
	{
		return entityMeta.getName() + '_' + oneToManyAttrName;
	}

	public static String getManyToOneAttrLabel(EntityMetaData entityMeta, AttributeMetaData oneToManyAttr)
	{
		return entityMeta.getLabel() + " (" + oneToManyAttr.getLabel() + ')';
	}

	public static String getManyToOneAttrDescription(EntityMetaData entityMeta, AttributeMetaData oneToManyAttr)
	{
		return null; // TODO
	}
}