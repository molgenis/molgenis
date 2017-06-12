package org.molgenis.data.annotation.core;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class AbstractRepositoryAnnotator implements RepositoryAnnotator
{
	@Override
	public String canAnnotate(EntityType repoMetaData)
	{
		Iterable<Attribute> annotatorAttributes = getRequiredAttributes();
		for (Attribute annotatorAttribute : annotatorAttributes)
		{
			// one of the needed attributes not present? we can not annotate
			if (repoMetaData.getAttribute(annotatorAttribute.getName()) == null)
			{
				return "missing required attribute";
			}

			// one of the needed attributes not of the correct type? we can not annotate
			if (repoMetaData.getAttribute(annotatorAttribute.getName()).getDataType()
					!= annotatorAttribute.getDataType())
			{
				// allow type string when required attribute is text (for backward compatibility)
				if (!(repoMetaData.getAttribute(annotatorAttribute.getName()).getDataType().equals(AttributeType.STRING)
						&& annotatorAttribute.getDataType().equals(AttributeType.TEXT)))
				{
					return "a required attribute has the wrong datatype";
				}
			}
			if (annotatorAttribute.getDataType().equals(AttributeType.XREF))
			{
				EntityType refEntity = repoMetaData.getAttribute(annotatorAttribute.getName()).getRefEntity();
				for (Attribute refAttribute : annotatorAttribute.getRefEntity().getAtomicAttributes())
				{
					if (refEntity.getAttribute(refAttribute.getName()) == null)
					{
						return "the required referenced entity [" + StreamSupport.stream(
								annotatorAttribute.getRefEntity().getAtomicAttributes().spliterator(), false)
																				 .map(Attribute::getName)
																				 .collect(Collectors.joining(", "))
								+ "] is missing a required attribute";
					}
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

	@Override
	public Iterator<Entity> annotate(final Iterator<Entity> sourceIterable)
	{
		return this.annotate(() -> sourceIterable);
	}

	@Override
	public String getFullName()
	{
		return ANNOTATOR_PREFIX + getSimpleName();
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
