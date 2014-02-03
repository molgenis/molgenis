package org.molgenis.data.validation;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Component
public class DefaultEntityValidator implements EntityValidator
{
	private final DataService dataService;

	@Autowired
	public DefaultEntityValidator(DataService dataService)
	{
		this.dataService = dataService;
	}

	@Override
	public void validate(Iterable<? extends Entity> entities, EntityMetaData meta) throws MolgenisValidationException
	{
		Set<ConstraintViolation> violations = checkUniques(entities, meta);
		if (!violations.isEmpty())
		{
			throw new MolgenisValidationException(violations);
		}
	}

	private Set<ConstraintViolation> checkUniques(Iterable<? extends Entity> entities, EntityMetaData meta)
	{
		Set<ConstraintViolation> violations = Sets.newLinkedHashSet();

		for (AttributeMetaData attr : meta.getAttributes())
		{
			if (attr.isUnique())
			{
				// Gather all attribute values
				List<Object> values = Lists.newArrayList();
				for (Entity entity : entities)
				{
					Object value = entity.get(attr.getName());
					values.add(value);
				}

				// Create 'in' query, should find nothing in the repo
				if (!values.isEmpty())
				{
					long count = dataService.count(meta.getName(), new QueryImpl().in(attr.getName(), values));
					if (count > 0)
					{
						// Go through the list to find the violators
						long found = 0;
						Iterator<? extends Entity> it = entities.iterator();

						while (it.hasNext() && (found < count))
						{
							Entity entity = it.next();

							Object value = entity.get(attr.getName());
							Entity existing = dataService.findOne(meta.getName(),
									new QueryImpl().eq(attr.getName(), value));

							if ((existing != null) && !equals(entity, existing))
							{
								String message = String.format(
										"The attribute %s of %s must be unique, but the value %s already exists.",
										attr.getName(), meta.getName(), value);
								violations.add(new ConstraintViolation(message, value, entity, attr, meta));

								found++;
							}
						}
					}
				}

			}
		}

		return violations;
	}

	// Check id two entities
	private boolean equals(Entity e1, Entity e2)
	{
		if ((e1.getIdValue() != null) && (e2.getIdValue() != null))
		{
			if (e1.getIdValue().equals(e2.getIdValue()))
			{
				return true;
			}
		}

		if (e1.getLabelAttributeNames().equals(e2.getLabelAttributeNames()))
		{
			List<Object> values1 = Lists.newArrayList();
			for (String attrName : e1.getLabelAttributeNames())
			{
				values1.add(e1.get(attrName));
			}

			List<Object> values2 = Lists.newArrayList();
			for (String attrName : e2.getLabelAttributeNames())
			{
				values2.add(e2.get(attrName));
			}

			if (values1.equals(values2))
			{
				return true;
			}
		}

		return false;
	}
}
