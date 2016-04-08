package org.molgenis.data.annotation.filter;

import java.util.Collection;
import java.util.Collections;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.entity.ResultFilter;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

/**
 * ResultFilter that just returns the first result
 */
public class FirstResultFilter implements ResultFilter
{

	@Override
	public Collection<AttributeMetaData> getRequiredAttributes()
	{
		return Collections.emptyList();
	}
	
	@Override
	public Optional<Entity> filterResults(Iterable<Entity> results, Entity annotatedEntity, boolean updateMode)
	{
		if(updateMode == true)
		{
			throw new MolgenisDataException("This annotator/filter does not support updating of values");
		}
		return FluentIterable.from(results).first();
	}
}
