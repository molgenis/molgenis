package org.molgenis.data.annotation.core.exception;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

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
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			String language = languageService.getCurrentUserLanguageCode();
			List<String> requiredAttributeNames = requiredAttributes.stream()
																	.map(attr -> attr.getLabel(language))
																	.collect(Collectors.toList());
			return MessageFormat.format(format, failedEntity.getIdValue(), entityNumber,
					StringUtils.join(requiredAttributeNames, ","), annotatorName, cause);
		}).orElse(super.getLocalizedMessage());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		throw new UnsupportedOperationException();
	}

}
