package org.molgenis.data.annotation;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractRepositoryEntityAnnotator extends AbstractRepositoryAnnotator
{
	@Override
	@Transactional
	@RunAsSystem
	public Iterator<Entity> annotate(final Iterable<Entity> sourceIterable)
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
							results = annotateEntity(sourceEntity);
						}
						catch (IOException e)
						{
							throw new RuntimeException(e);
						}
						catch (InterruptedException e)
						{
							throw new RuntimeException(e);
						}

						size = results.size();
					}
					current = 0;
				}
				if (results.size() > 0)
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

	public abstract List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException;

}
