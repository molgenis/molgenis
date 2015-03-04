package org.molgenis.data.meta;

import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;

import com.google.common.collect.Lists;

public class MetaUtils
{
	public static List<AttributeMetaData> updateEntityMeta(MetaDataService metaDataService, EntityMetaData entityMeta,
			boolean sync)
	{
		EntityMetaData existingEntityMetaData = metaDataService.getEntityMetaData(entityMeta.getName());
		List<AttributeMetaData> addedAttributes = Lists.newArrayList();

		for (AttributeMetaData attr : existingEntityMetaData.getAttributes())
		{
			if (entityMeta.getAttribute(attr.getName()) == null)
			{
				throw new MolgenisDataException(
						"Removing of existing attributes is currently not supported. You tried to remove attribute ["
								+ attr.getName() + "]");
			}
		}

		for (AttributeMetaData attr : entityMeta.getAttributes())
		{
			AttributeMetaData currentAttribute = existingEntityMetaData.getAttribute(attr.getName());
			if (currentAttribute != null)
			{
				if (!currentAttribute.isSameAs(attr))
				{
					throw new MolgenisDataException(
							"Changing existing attributes is not currently supported. You tried to alter attribute ["
									+ attr.getName() + "] of entity [" + entityMeta.getName()
									+ "]. Only adding of new atrtibutes to existing entities is supported.");
				}
			}
			else if (!attr.isNillable())
			{
				throw new MolgenisDataException("Adding non-nillable attributes is not currently supported");
			}
			else
			{
				if (sync) metaDataService.addAttributeSync(entityMeta.getName(), attr);
				else metaDataService.addAttribute(entityMeta.getName(), attr);

				addedAttributes.add(attr);
			}
		}

		return addedAttributes;
	}
}
