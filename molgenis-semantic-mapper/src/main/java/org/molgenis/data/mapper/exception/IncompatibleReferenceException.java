package org.molgenis.data.mapper.exception;

import org.molgenis.data.meta.AttributeType;

import java.text.MessageFormat;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class IncompatibleReferenceException extends IncompatibleTargetException
{
	private static final String ERROR_CODE = "M06";
	private final String mappingTargetAttributeName;
	private final AttributeType mappingTargetAttributeType;
	private final String mappingTargetRefEntityName;
	private final String targetRepositoryAttributeName;
	private final AttributeType targetRepositoryAttributeType;
	private final String targetRepositoryRefEntityName;

	public IncompatibleReferenceException(String mappingTargetAttributeName, AttributeType mappingTargetAttributeType,
			String mappingTargetRefEntityName, String targetRepositoryAttributeName,
			AttributeType targetRepositoryAttributeType, String targetRepositoryRefEntityName)
	{
		super(ERROR_CODE);
		this.mappingTargetAttributeName = requireNonNull(mappingTargetAttributeName);
		this.mappingTargetAttributeType = requireNonNull(mappingTargetAttributeType);
		this.mappingTargetRefEntityName = requireNonNull(mappingTargetRefEntityName);
		this.targetRepositoryAttributeName = requireNonNull(targetRepositoryAttributeName);
		this.targetRepositoryAttributeType = requireNonNull(targetRepositoryAttributeType);
		this.targetRepositoryRefEntityName = requireNonNull(targetRepositoryRefEntityName);
	}

	public String getMappingTargetAttributeName()
	{
		return mappingTargetAttributeName;
	}

	public AttributeType getMappingTargetAttributeType()
	{
		return mappingTargetAttributeType;
	}

	public String getMappingTargetRefEntityName()
	{
		return mappingTargetRefEntityName;
	}

	public String getTargetRepositoryAttributeName()
	{
		return targetRepositoryAttributeName;
	}

	public AttributeType getTargetRepositoryAttributeType()
	{
		return targetRepositoryAttributeType;
	}

	public String getTargetRepositoryRefEntityName()
	{
		return targetRepositoryRefEntityName;
	}

	@Override
	public String getMessage()
	{
		return format("name:%s type:%s ref:%s, targetName:%s, targetType:%s, targetRef:%s", mappingTargetAttributeName,
				mappingTargetAttributeType, mappingTargetRefEntityName, targetRepositoryAttributeName,
				targetRepositoryAttributeType, targetRepositoryRefEntityName);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, mappingTargetAttributeName, mappingTargetAttributeType,
					mappingTargetRefEntityName, targetRepositoryAttributeName, targetRepositoryAttributeType,
					targetRepositoryRefEntityName);
		}).orElseGet(super::getLocalizedMessage);
	}
}