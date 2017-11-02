package org.molgenis.data.populate;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.AttributeType.*;

/**
 * Populate entity values for auto attributes
 */
@Component
public class AutoValuePopulator
{
	private final IdGenerator idGenerator;

	public AutoValuePopulator(IdGenerator idGenerator)
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
		generateAutoDateOrDateTime(singletonList(entity), entity.getEntityType().getAttributes());

		// auto id
		Attribute idAttr = entity.getEntityType().getIdAttribute();
		if (idAttr != null && idAttr.isAuto() && entity.getIdValue() == null && (idAttr.getDataType() == STRING))
		{
			entity.set(idAttr.getName(), idGenerator.generateId());
		}
	}

	private static void generateAutoDateOrDateTime(Iterable<? extends Entity> entities, Iterable<Attribute> attrs)
	{
		// get auto date and datetime attributes
		Iterable<Attribute> autoAttrs = stream(attrs.spliterator(), false).filter(attr ->
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
		for (Entity entity : entities)
		{
			for (Attribute attr : autoAttrs)
			{
				AttributeType type = attr.getDataType();
				switch (type)
				{
					case DATE:
						entity.set(attr.getName(), LocalDate.now(ZoneId.systemDefault()));
						break;
					case DATE_TIME:
						entity.set(attr.getName(), Instant.now());
						break;
					default:
						throw new UnexpectedEnumException(type);
				}
			}
		}
	}
}
