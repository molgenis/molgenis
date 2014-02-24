package org.molgenis.data.annotation;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: charbonb Date: 21/02/14 Time: 11:24 To change this template use File | Settings |
 * File Templates.
 */
public abstract class AbstractRepositoryAnnotator implements RepositoryAnnotator,
		ApplicationListener<ContextRefreshedEvent>
{
	@Autowired
	AnnotationService annotatorService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		annotatorService.addAnnotator(this);
	}

	@Override
	public boolean canAnnotate(EntityMetaData repoMetaData)
	{
		boolean canAnnotate = true;
		Iterable<AttributeMetaData> annotatorAttributes = getInputMetaData().getAttributes();
		for (AttributeMetaData annotatorAttribute : annotatorAttributes)
		{
			// one of the needed attributes not present? we can not annotate
			if (repoMetaData.getAttribute(annotatorAttribute.getName()) == null)
			{
				canAnnotate = false;
				break;
			}
			
			// one of the needed attributes not of the correct type? we can not annotate
			if (!repoMetaData.getAttribute(annotatorAttribute.getName()).getDataType()
					.equals(annotatorAttribute.getDataType()))
			{
				canAnnotate = false;
				break;
			}
		}
		return canAnnotate;
	}

	@Override
	@Transactional
	public Iterator<Entity> annotate(final Iterator<Entity> source)
	{
		return new Iterator<Entity>()
		{
			final List<Entity> results = annotateEntity(source.next());
			int current = 0;
			int size = results.size();

			@Override
			public boolean hasNext()
			{
				return current < size;
			}

			@Override
			public Entity next()
			{
				Entity result = results.get(current);
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

	public abstract List<Entity> annotateEntity(Entity entity);
}
