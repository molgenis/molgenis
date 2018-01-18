package org.molgenis.data.annotation.core.exception;

import org.molgenis.data.meta.model.Attribute;

import java.util.stream.Collectors;

import static org.molgenis.data.vcf.utils.VcfWriterUtils.VARIANT;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
@Deprecated
public class UiAnnotationException extends AnnotationException
{
	public UiAnnotationException(AnnotationException ae)
	{
		super(ae);
	}

	@Override
	public String getMessage()
	{
		String message = "Annotation failed while running annotator " + getAnnotatorName() + " on ";

		if (getFailedEntity() == null)
		{
			message += " unknown entity.";
		}
		else
		{
			message += " entity with [" + concatAttributeNameValue(getFailedEntity().getEntityType().getIdAttribute())
					+ ", " + getRequiredAttributes().stream()
													.map(this::concatAttributeNameValue)
													.collect(Collectors.joining(", ")) + "]";
		}

		message += " Cause: " + super.getCause();

		return message;
	}

	private String concatAttributeNameValue(Attribute attribute)
	{
		String value;
		if (attribute.getName().equals(VARIANT))
		{
			value = getFailedEntity().getEntity(VARIANT).getIdValue().toString();
		}
		else
		{
			value = getFailedEntity().get(attribute.getName()).toString();
		}
		return attribute.getName() + "=" + value;
	}
}
