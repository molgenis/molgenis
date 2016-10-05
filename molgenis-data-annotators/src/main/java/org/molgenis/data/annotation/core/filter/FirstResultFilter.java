package org.molgenis.data.annotation.core.filter;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.core.entity.ResultFilter;
import org.molgenis.data.meta.model.Attribute;

import java.util.Collection;
import java.util.Collections;

/**
 * ResultFilter that just returns the first result
 */
public class FirstResultFilter implements ResultFilter
{

	@Override
	public Collection<Attribute> getRequiredAttributes()
	{
		return Collections.emptyList();
	}

	@Override
	public Optional<Entity> filterResults(Iterable<Entity> results, Entity annotatedEntity, boolean updateMode)
	{
		if (updateMode == true)
		{
			throw new MolgenisDataException("This annotator/filter does not support updating of values");
		}
		return FluentIterable.from(results).first();
	}

}
