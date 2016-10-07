package org.molgenis.data.populate;

import org.molgenis.AttributeType;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.AttributeType.*;

/**
 * Populate entity values for auto attributes
 */
@Component
public class EntityPopulator
{
	private final IdGenerator idGenerator;

	@Autowired
	public EntityPopulator(IdGenerator idGenerator)
	{
		this.idGenerator = requireNonNull(idGenerator);
	}

	/**
	 * Populates an entity with auto values
	 *
	 * @param entity populated entity
	 */
	public void populate(Entity entity)
	{
		// auto date
		generateAutoDateOrDateTime(singletonList(entity), entity.getEntityMetaData().getAttributes());

		// auto id
		AttributeMetaData idAttr = entity.getEntityMetaData().getIdAttribute();
		if (idAttr != null && idAttr.isAuto() && entity.getIdValue() == null && (idAttr.getDataType() == STRING))
		{
			entity.set(idAttr.getName(), idGenerator.generateId());
		}
	}

	private static void generateAutoDateOrDateTime(Iterable<? extends Entity> entities,
			Iterable<AttributeMetaData> attrs)
	{
		// get auto date and datetime attributes
		Iterable<AttributeMetaData> autoAttrs = stream(attrs.spliterator(), false).filter(attr ->
		{
			if (attr.isAuto())
			{
				AttributeType type = attr.getDataType();
				return type == DATE || type == DATE_TIME;
			}
			else
			{
				return false;
			}
		}).collect(toList());

		// set current date for auto date and datetime attributes
		Date dateNow = new Date();
		for (Entity entity : entities)
		{
			for (AttributeMetaData attr : autoAttrs)
			{
				AttributeType type = attr.getDataType();
				switch (type)
				{
					case DATE:
						entity.set(attr.getName(), dateNow);
						break;
					case DATE_TIME:
						entity.set(attr.getName(), dateNow);
						break;
					default:
						throw new RuntimeException(format("Unexpected data type [%s]", type.toString()));
				}
			}
		}
	}
}
