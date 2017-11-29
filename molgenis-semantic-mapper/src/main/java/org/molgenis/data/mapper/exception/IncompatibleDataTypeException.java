package org.molgenis.data.mapper.exception;

import org.molgenis.data.meta.model.Attribute;

import java.text.MessageFormat;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class IncompatibleDataTypeException extends IncompatibleTargetException
{
	private static final String ERROR_CODE = "M05";
	private final transient Attribute mappingTargetAttribute;
	private final transient Attribute targetRepositoryAttribute;

	public IncompatibleDataTypeException(Attribute mappingTargetAttribute, Attribute targetRepositoryAttribute)
	{
		super(ERROR_CODE);
		this.mappingTargetAttribute = requireNonNull(mappingTargetAttribute);
		this.targetRepositoryAttribute = requireNonNull(targetRepositoryAttribute);
	}

	public Attribute getMappingTargetAttribute()
	{
		return mappingTargetAttribute;
	}

	public Attribute getTargetRepositoryAttribute()
	{
		return targetRepositoryAttribute;
	}

	@Override
	public String getMessage()
	{
		return format("name:%s type:%s, targetName:%s targetType:%s", mappingTargetAttribute.getName(),
				mappingTargetAttribute.getDataType(), targetRepositoryAttribute.getName(),
				targetRepositoryAttribute.getDataType());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			String language = languageService.getCurrentUserLanguageCode();
			return MessageFormat.format(format, mappingTargetAttribute.getLabel(language),
					mappingTargetAttribute.getDataType(), targetRepositoryAttribute.getLabel(language),
					targetRepositoryAttribute.getDataType());
		}).orElseGet(super::getLocalizedMessage);
	}
}
