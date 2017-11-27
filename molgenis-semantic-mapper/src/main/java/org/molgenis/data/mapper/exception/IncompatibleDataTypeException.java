package org.molgenis.data.mapper.exception;

import org.molgenis.data.meta.AttributeType;

import java.text.MessageFormat;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class IncompatibleDataTypeException extends IncompatibleTargetException
{
	private static final String ERROR_CODE = "M05";
	private final String mappingTargetAttributeName;
	private final AttributeType mappingTargetAttributeType;
	private final String targetRepositoryAttributeName;
	private final AttributeType targetRepositoryAttributeType;

	public IncompatibleDataTypeException(String mappingTargetAttributeName, AttributeType mappingTargetAttributeType,
			String targetRepositoryAttributeName, AttributeType targetRepositoryAttributeType)
	{
		super(ERROR_CODE);
		this.mappingTargetAttributeName = requireNonNull(mappingTargetAttributeName);
		this.mappingTargetAttributeType = requireNonNull(mappingTargetAttributeType);
		this.targetRepositoryAttributeName = requireNonNull(targetRepositoryAttributeName);
		this.targetRepositoryAttributeType = requireNonNull(targetRepositoryAttributeType);
	}

	public String getMappingTargetAttributeName()
	{
		return mappingTargetAttributeName;
	}

	public AttributeType getMappingTargetAttributeType()
	{
		return mappingTargetAttributeType;
	}

	public String getTargetRepositoryAttributeName()
	{
		return targetRepositoryAttributeName;
	}

	public AttributeType getTargetRepositoryAttributeType()
	{
		return targetRepositoryAttributeType;
	}

	@Override
	public String getMessage()
	{
		return format("name:%s type:%s, targetName:%s targetType:%s", mappingTargetAttributeName,
				mappingTargetAttributeType, targetRepositoryAttributeName, targetRepositoryAttributeType);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, mappingTargetAttributeName, mappingTargetAttributeType,
					targetRepositoryAttributeName, targetRepositoryAttributeType);
		}).orElseGet(super::getLocalizedMessage);
	}
}
