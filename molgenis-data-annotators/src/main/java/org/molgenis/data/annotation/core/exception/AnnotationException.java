package org.molgenis.data.annotation.core.exception;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.i18n.CodedRuntimeException;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AnnotationException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN08";

	private final Integer entityNumber;
	private final transient Entity failedEntity;
	private final String annotatorName;
	private final transient List<Attribute> requiredAttributes;
	private final Throwable cause;

	public AnnotationException(Entity failedEntity, int lineNumber, List<Attribute> requiredAttributes, String annotatorName, Throwable cause)
	{
		super(ERROR_CODE, cause);
		this.failedEntity = Objects.requireNonNull(failedEntity);
		this.entityNumber = Objects.requireNonNull(lineNumber);
		this.requiredAttributes = Objects.requireNonNull(requiredAttributes);
		this.annotatorName = Objects.requireNonNull(annotatorName);
		this.cause = cause;
	}

	public Integer getEntityNumber()
	{
		return entityNumber;
	}

	public Entity getFailedEntity()
	{
		return failedEntity;
	}

	public List<Attribute> getRequiredAttributes()
	{
		return requiredAttributes;
	}

	public String getAnnotatorName()
	{
		return annotatorName;
	}

	@Override
	public String getMessage()
	{
		return String.format("failedEntity:%s entityNumber:%s requiredAttributes:%s annotatorName:%s, cause:%s",
				failedEntity.getIdValue(), entityNumber, requiredAttributes, annotatorName, cause);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		String languageCode = LocaleContextHolder.getLocale().getLanguage();
		// Annotation with annotator:''{3}'' failed for entity:''{0,label}'' on row: {1}, required attributes for this annotator:''{2}''
		String requiredAttributeNames = requiredAttributes.stream()
														  .map(attr -> attr.getLabel(languageCode))
														  .collect(Collectors.joining(","));
		return new Object[] { failedEntity, entityNumber, requiredAttributeNames, annotatorName, cause };
	}
}
