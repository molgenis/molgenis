package org.molgenis.data.annotation;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
		Iterable<AttributeMetaData> annotatorAttributes = getInputMetaData().getAttributes();
		for (AttributeMetaData annotatorAttribute : annotatorAttributes)
		{
			// one of the needed attributes not present? we can not annotate
			if (repoMetaData.getAttribute(annotatorAttribute.getName()) == null)
			{
				return false;
			}

			// one of the needed attributes not of the correct type? we can not annotate
			if (!repoMetaData.getAttribute(annotatorAttribute.getName()).getDataType()
					.equals(annotatorAttribute.getDataType()))
			{
				return false;
			}

			// Are the runtime property files not available, or is a webservice down? we can not annotate
			if (!annotationDataExists())
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks if folder and files that were set with a runtime property actually exist, or if a webservice can be
	 * reached
	 * 
	 * @return boolean
	 * */
	protected abstract boolean annotationDataExists();

	@Override
	@Transactional
	public Iterator<Entity> annotate(final Iterator<Entity> source)
	{
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

	public Entity getAnnotatedEntity(Entity entity, Map<String, Object> resultMap)
	{
		DefaultEntityMetaData resultEntityMetadata = new DefaultEntityMetaData(entity.getEntityMetaData());
		MapEntity resultEntity = new MapEntity(entity, resultEntityMetadata);
		for (AttributeMetaData attributeMetaData : getOutputMetaData().getAtomicAttributes())
		{
			resultEntityMetadata.addAttributeMetaData(attributeMetaData);
			resultEntity.set(attributeMetaData.getName(), resultMap.get(attributeMetaData.getName()));
		}
		return resultEntity;
	}
}
