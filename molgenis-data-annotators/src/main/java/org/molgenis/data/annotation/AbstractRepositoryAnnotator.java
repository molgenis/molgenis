package org.molgenis.data.annotation;

import java.util.Iterator;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created with IntelliJ IDEA. User: charbonb Date: 21/02/14 Time: 11:24 To change this template use File | Settings |
 * File Templates.
 */
public abstract class AbstractRepositoryAnnotator implements RepositoryAnnotator
{
	@Override
	public String canAnnotate(EntityMetaData repoMetaData)
	{
		Iterable<AttributeMetaData> annotatorAttributes = getInputMetaData();
		for (AttributeMetaData annotatorAttribute : annotatorAttributes)
		{
			// one of the needed attributes not present? we can not annotate
			if (repoMetaData.getAttribute(annotatorAttribute.getName()) == null)
			{
				return "missing required attribute";
			}

			// one of the needed attributes not of the correct type? we can not annotate
			if (!repoMetaData.getAttribute(annotatorAttribute.getName()).getDataType()
					.equals(annotatorAttribute.getDataType()))
			{
				// allow type string when required attribute is text (for backward compatibility)
				if (!(repoMetaData.getAttribute(annotatorAttribute.getName()).getDataType()
						.equals(MolgenisFieldTypes.STRING) && annotatorAttribute.getDataType().equals(
						MolgenisFieldTypes.TEXT)))
				{
					return "a required attribute has the wrong datatype";
				}
			}

			// Are the runtime property files not available, or is a webservice down? we can not annotate
			if (!annotationDataExists())
			{
				return "annotation datasource unreachable";
			}
		}

		return "true";
	}

	/**
	 * Checks if folder and files that were set with a runtime property actually exist, or if a webservice can be
	 * reached
	 *
	 * @return boolean
	 */
	protected abstract boolean annotationDataExists();

	@Override
	@Transactional
	public Iterator<Entity> annotate(final Iterator<Entity> sourceIterable)
	{
		return this.annotate(new Iterable<Entity>()
		{
			@Override
			public Iterator<Entity> iterator()
			{
				return sourceIterable;
			}
		});
	}

	@Override
	public String getFullName()
	{
		return RepositoryAnnotator.ANNOTATOR_PREFIX + getSimpleName();
	}

	@Override
	public String getDescription()
	{
		String desc = "TODO";
		AnnotatorInfo annotatorInfo = getInfo();
		if (annotatorInfo != null) desc = annotatorInfo.getDescription();
		return desc;
	}

}
