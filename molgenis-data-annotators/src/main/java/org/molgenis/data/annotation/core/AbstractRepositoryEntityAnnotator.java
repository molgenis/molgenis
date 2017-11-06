package org.molgenis.data.annotation.core;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.exception.AnnotationException;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractRepositoryEntityAnnotator extends AbstractRepositoryAnnotator
{
	@Override
	@RunAsSystem
	public Iterator<Entity> annotate(final Iterable<Entity> sourceIterable)
	{ // default update mode is false
		return annotate(sourceIterable, false);
	}

	@Override
	@Transactional
	@RunAsSystem
	public Iterator<Entity> annotate(final Iterable<Entity> sourceIterable, boolean updateMode)
	{
		Iterator<Entity> source = sourceIterable.iterator();
		return new Iterator<Entity>()
		{
			int current = 0;
			int size = 0;
			List<Entity> results;
			Entity result;

			@Override
			public boolean hasNext()
			{
				return current < size || source.hasNext();
			}

			@Override
			public Entity next()
			{
				Entity sourceEntity = null;
				if (current >= size)
				{
					if (source.hasNext())
					{
						try
						{
							sourceEntity = source.next();
							results = annotateEntity(sourceEntity, updateMode);
						}
						catch (Exception e)
						{
							throw new AnnotationException(sourceEntity, current + 1, getRequiredAttributes(),
									getSimpleName(), e);
						}

						size = results.size();
					}
					current = 0;
				}
				if (!results.isEmpty())
				{
					result = results.get(current);
				}
				else
				{
					result = sourceEntity;
				}
				++current;
				return result;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	public abstract List<Entity> annotateEntity(Entity entity, boolean updateMode)
			throws IOException, InterruptedException;

}
