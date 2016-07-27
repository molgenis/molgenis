package org.molgenis.data.meta;

import com.google.common.base.Objects;
import org.molgenis.data.Fetch;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.security.core.Permission;
import org.molgenis.util.EntityUtils;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.*;
import static org.molgenis.util.SecurityDecoratorUtils.validatePermission;

public class MetaUtils
{
	public static Fetch getEntityMetaDataFetch()
	{
		// TODO simplify fetch creation (in this case *all* attributes and expand xref/mrefs)
		return new Fetch().field(FULL_NAME).field(SIMPLE_NAME).field(PACKAGE).field(LABEL).field(DESCRIPTION)
				.field(ATTRIBUTES).field(ID_ATTRIBUTE).field(LABEL_ATTRIBUTE).field(LOOKUP_ATTRIBUTES).field(ABSTRACT)
				.field(EXTENDS).field(TAGS).field(BACKEND);
	}

	public static List<AttributeMetaData> updateEntityMeta(MetaDataService metaDataService, EntityMetaData entityMeta)
	{
		String backend = entityMeta.getBackend() != null ? entityMeta.getBackend() : metaDataService.getDefaultBackend()
				.getName();

		EntityMetaData existingEntityMetaData = metaDataService.getEntityMetaData(entityMeta.getName());
		if (!Objects.equal(backend, existingEntityMetaData.getBackend()))
		{
			throw new MolgenisDataException(
					"Changing the backend of an entity is not supported. You tried to change the backend of entity '"
							+ entityMeta.getName() + "' from '" + existingEntityMetaData.getBackend() + "' to '"
							+ backend + "'");
		}

		List<AttributeMetaData> addedAttributes = newArrayList();

		for (AttributeMetaData attr : existingEntityMetaData.getAttributes())
		{
			if (entityMeta.getAttribute(attr.getName()) == null)
			{
				throw new MolgenisDataException(
						"Removing of existing attributes is currently not supported. You tried to remove attribute ["
								+ attr.getName() + "] of entity [" + entityMeta.getName() + "]");
			}
		}

		for (AttributeMetaData attr : entityMeta.getAttributes())
		{
			AttributeMetaData currentAttribute = existingEntityMetaData.getAttribute(attr.getName());
			if (currentAttribute != null)
			{
				if (!EntityUtils.equals(currentAttribute, attr))
				{
					//FIXME This is no longer true, we are allowed to change existing attributes, it just needs to adhere to type conversion rules
					throw new MolgenisDataException(
							"Changing existing attributes is not currently supported. You tried to alter attribute ["
									+ attr.getName() + "] of entity [" + entityMeta.getName()
									+ "]. Only adding of new attributes is supported.");
				}
			}
			else if (!attr.isNillable())
			{
				// FIXME Needs checking, dont know if this is still true
				throw new MolgenisDataException(
						"Adding non-nillable attributes is not currently supported.  You tried to add non-nillable attribute ["
								+ attr.getName() + "] of entity [" + entityMeta.getName() + "].");
			}
			else
			{
				validatePermission(entityMeta.getName(), Permission.WRITEMETA);
				metaDataService.addAttribute(attr);
				addedAttributes.add(attr);
			}
		}

		return addedAttributes;
	}
}
