package org.molgenis.data.populate;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;

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
		Attribute idAttr = entity.getEntityMetaData().getIdAttribute();
		if (idAttr != null && idAttr.isAuto() && entity.getIdValue() == null && (idAttr.getDataType() == STRING))
		{
			entity.set(idAttr.getName(), idGenerator.generateId());
		}
	}

	private static void generateAutoDateOrDateTime(Iterable<? extends Entity> entities,
			Iterable<Attribute> attrs)
	{
		// get auto date and datetime attributes
		Iterable<Attribute> autoAttrs = stream(attrs.spliterator(), false).filter(attr ->
		{
			if (attr.isAuto())
			{
				MolgenisFieldTypes.AttributeType type = attr.getDataType();
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
			for (Attribute attr : autoAttrs)
			{
				MolgenisFieldTypes.AttributeType type = attr.getDataType();
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
