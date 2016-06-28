package org.molgenis.data.meta;

import com.google.common.collect.Lists;
import org.molgenis.data.Fetch;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.security.core.Permission;
import org.molgenis.util.EntityUtils;

import java.util.List;

import static org.molgenis.data.i18n.LanguageMetaData.DEFAULT_LANGUAGE_CODE;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.*;
import static org.molgenis.util.SecurityDecoratorUtils.validatePermission;

public class MetaUtils
{
	public static Fetch getEntityMetaDataFetch()
	{
		// TODO simplify fetch creation (in this case *all* attributes and expand xref/mrefs)
		return new Fetch().field(FULL_NAME).field(SIMPLE_NAME).field(PACKAGE).field(LABEL)
				.field(LABEL + '-' + DEFAULT_LANGUAGE_CODE).field(DESCRIPTION)
				.field(DESCRIPTION + '-' + DEFAULT_LANGUAGE_CODE).field(ATTRIBUTES).field(ID_ATTRIBUTE)
				.field(LABEL_ATTRIBUTE).field(LOOKUP_ATTRIBUTES).field(ABSTRACT).field(EXTENDS).field(TAGS)
				.field(BACKEND);
	}

	public static List<AttributeMetaData> updateEntityMeta(MetaDataService metaDataService, EntityMetaData entityMeta)
	{
		String backend = entityMeta.getBackend() != null ? entityMeta.getBackend() : metaDataService.getDefaultBackend()
				.getName();

		EntityMetaData existingEntityMetaData = metaDataService.getEntityMetaData(entityMeta.getName());
		if (!existingEntityMetaData.getBackend().equals(backend))
		{
			throw new MolgenisDataException(
					"Changing the backend of an entity is not supported. You tried to change the backend of entity '"
							+ entityMeta.getName() + "' from '" + existingEntityMetaData.getBackend() + "' to '"
							+ backend + "'");
		}

		List<AttributeMetaData> addedAttributes = Lists.newArrayList();

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
					throw new MolgenisDataException(
							"Changing existing attributes is not currently supported. You tried to alter attribute ["
									+ attr.getName() + "] of entity [" + entityMeta.getName()
									+ "]. Only adding of new attributes is supported.");
				}
			}
			else if (!attr.isNillable())
			{
				throw new MolgenisDataException(
						"Adding non-nillable attributes is not currently supported.  You tried to add non-nillable attribute ["
								+ attr.getName() + "] of entity [" + entityMeta.getName() + "].");
			}
			else
			{
				validatePermission(entityMeta.getName(), Permission.WRITEMETA);

				metaDataService.addAttribute(entityMeta.getName(), attr);

				addedAttributes.add(attr);
			}
		}

		return addedAttributes;
	}
}
